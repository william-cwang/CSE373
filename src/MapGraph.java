import autocomplete.Autocomplete;
import autocomplete.TreeSetAutocomplete;
import graphs.AStarGraph;
import graphs.Edge;
import graphs.shortestpaths.AStarSolver;
import minpq.DoubleMapMinPQ;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Point;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * {@link AStarGraph} of places as {@link Point} vertices and streets edges weighted by physical distance.
 *
 * @see AStarGraph
 * @see MapServer
 */
public class MapGraph implements AStarGraph<Point> {
    private final String osmPath;
    private final String accessPath;
    private final SpatialContext context;
    private final Map<Point, List<Edge<Point>>> neighbors;
    private final Map<Long, Point> byId;
    private final Map<String, List<Point>> byName;
    private final Autocomplete autocomplete;
    private final Map<Long, Double> accessScores;
    private static final Set<String> allowedHighwayTypes = Set.of(
            "motorway",
            "trunk",
            "primary",
            "secondary",
            "tertiary",
            "unclassified",
            "residential",
            "living_street",
            "motorway_link",
            "trunk_link",
            "primary_link",
            "secondary_link",
            "tertiary_link"
    );

    /**
     * Constructs a new map graph from the path to an OSM GZ file and a places TSV.
     *
     * @param osmPath    The path to a gzipped OSM (XML) file.
     * @param accessPath The path to a TSV file representing access scores for each OSM way.
     * @throws ParserConfigurationException if a parser cannot be created.
     * @throws SAXException                 for SAX errors.
     * @throws IOException                  if a file is not found or if the file is not gzipped.
     */
    public MapGraph(String osmPath, String accessPath, SpatialContext context)
            throws ParserConfigurationException, SAXException, IOException {
        this.osmPath = osmPath;
        this.accessPath = accessPath;
        this.context = context;

        // Parse the Project Sidewalk access scores
        accessScores = new HashMap<>();
        try (Scanner input = new Scanner(fileStream(accessPath))) {
            input.nextLine(); // Skip header
            while (input.hasNextLine()) {
                Scanner line = new Scanner(input.nextLine()).useDelimiter("\t");
                accessScores.put(line.nextLong(), line.nextDouble());
            }
        }

        // Parse the OpenStreetMap (OSM) data using the SAXParser XML tree walker.
        neighbors = new HashMap<>();
        byId = new HashMap<>();
        byName = new HashMap<>();
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(new GZIPInputStream(fileStream(osmPath)), new Handler());

        // Add reachable locations to the Autocomplete engine.
        autocomplete = new TreeSetAutocomplete();
        autocomplete.addAll(byName.keySet());
    }

    /**
     * Returns an input stream from the contents of the file at the given path.
     *
     * @param path a file path.
     * @return an input stream with the contents of the specified file.
     */
    private static InputStream fileStream(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    /**
     * Returns the location closest to the given target location.
     *
     * @param target the target location.
     * @return the id of the location closest to the target.
     */
    public Point closest(Point target) {
        if (neighbors.containsKey(target)) {
            return target;
        }
        return Collections.min(neighbors.keySet(),
                Comparator.comparingDouble(p -> estimatedDistance(target, p)));
    }

    /**
     * Return the names of all locations that prefix-match the query string.
     *
     * @param prefix prefix string that could be any case with or without punctuation.
     * @return a list of full names of locations matching the prefix.
     */
    public List<CharSequence> getLocationsByPrefix(String prefix, Point center, int maxMatches) {
        List<CharSequence> matches = autocomplete.allMatches(prefix);
        Map<CharSequence, Double> elementsAndPriorities = new HashMap<>(matches.size());
        for (CharSequence match : matches) {
            elementsAndPriorities.put(match, estimatedDistance(center, byName.get(match).get(0)));
        }
        return new DoubleMapMinPQ<>(elementsAndPriorities).removeMin(maxMatches);
    }

    /**
     * Return all locations that match a valid location name.
     *
     * @param locationName a full name of a valid location.
     * @return a list of locations whose name matches the location name.
     */
    public List<Point> getLocations(String locationName) {
        return byName.getOrDefault(locationName, List.of());
    }

    /**
     * Returns a list of points representing the shortest path from the points closest to the start and goal.
     *
     * @param start the {@link Point} to start the shortest path.
     * @param goal  the {@link Point} to end the shortest path.
     * @return a list of points representing the shortest path from the points closest to the start and goal.
     */
    public List<Point> shortestPath(Point start, Point goal) {
        return new AStarSolver<>(this, closest(start), closest(goal)).solution();
    }

    @Override
    public List<Edge<Point>> neighbors(Point point) {
        return neighbors.computeIfAbsent(point, (p) -> List.of());
    }

    @Override
    public double estimatedDistance(Point start, Point end) {
        return context.calcDistance(start, end);
    }

    @Override
    public String toString() {
        return "MapGraph{" +
                "osmPath='" + osmPath + '\'' +
                ", accessPath='" + accessPath + '\'' +
                ", context='" + context + '\'' +
                '}';
    }

    /**
     * Adds an edge to this graph if it doesn't already exist using distance as the weight.
     *
     * @param from the originating point of the edge.
     * @param to the terminating point of the edge.
     * @param accessScore the access score for the edge where 0 is inaccessible and 1 is accessible.
     */
    private void addEdge(Point from, Point to, double accessScore) {
        if (!neighbors.containsKey(from)) {
            neighbors.put(from, new ArrayList<>());
        }
        neighbors.get(from).add(new Edge<>(from, to, estimatedDistance(from, to) / accessScore));
    }

    /**
     * Parses OSM XML files to construct a MapGraph.
     */
    private class Handler extends DefaultHandler {
        private String state;
        private long id;
        private String name;
        private boolean validWay;
        private Point location;
        private Queue<Point> path;

        Handler() {
            reset();
        }

        /**
         * Reset the handler state before processing a new way or node.
         */
        private void reset() {
            state = "";
            id = Long.MIN_VALUE;
            name = "";
            validWay = false;
            location = null;
            path = new ArrayDeque<>();
        }

        /**
         * Called at the beginning of an element.
         *
         * @param uri        The Namespace URI, or the empty string if the element has no Namespace URI or
         *                   if Namespace processing is not being performed.
         * @param localName  The local name (without prefix), or the empty string if Namespace
         *                   processing is not being performed.
         * @param qName      The qualified name (with prefix), or the empty string if qualified names are
         *                   not available. This tells us which element we're looking at.
         * @param attributes The attributes attached to the element. If there are no attributes, it
         *                   shall be an empty Attributes object.
         * @see Attributes
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equals("node")) {
                state = "node";
                id = Long.parseLong(attributes.getValue("id"));
                location = context.getShapeFactory().pointLatLon(
                        Double.parseDouble(attributes.getValue("lat")),
                        Double.parseDouble(attributes.getValue("lon"))
                );
            } else if (qName.equals("way")) {
                state = "way";
                id = Long.parseLong(attributes.getValue("id"));
            } else if (state.equals("way") && qName.equals("nd")) {
                long ref = Long.parseLong(attributes.getValue("ref"));
                path.add(byId.get(ref));
            } else if (state.equals("way") && qName.equals("tag")) {
                String k = attributes.getValue("k");
                String v = attributes.getValue("v");
                if (k.equals("highway")) {
                    validWay = allowedHighwayTypes.contains(v);
                }
            } else if (state.equals("node") && qName.equals("tag") && attributes.getValue("k").equals("name")) {
                name = attributes.getValue("v").strip();
                name = name.replaceAll("[“”]", "\"");
                name = name.replaceAll("[‘’]", "'");
            }
        }

        /**
         * Called at the end of an element.
         *
         * @param uri       The Namespace URI, or the empty string if the element has no Namespace URI or
         *                  if Namespace processing is not being performed.
         * @param localName The local name (without prefix), or the empty string if Namespace
         *                  processing is not being performed.
         * @param qName     The qualified name (with prefix), or the empty string if qualified names are
         *                  not available.
         */
        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equals("way")) {
                if (validWay && !path.isEmpty()) {
                    double accessScore;
                    if (accessScores.containsKey(id)) {
                        accessScore = accessScores.get(id);
                    } else {
                        accessScore = 1;
                    }
                    Point from = path.remove();
                    while (!path.isEmpty()) {
                        Point to = path.remove();
                        addEdge(from, to, accessScore);
                        addEdge(to, from, accessScore);
                        from = to;
                    }
                }
                reset();
            } else if (qName.equals("node")) {
                byId.put(id, location);
                if (!name.isBlank()) {
                    byName.putIfAbsent(name, new ArrayList<>());
                    byName.get(name).add(location);
                }
                reset();
            }
        }
    }
}
