import minpq.HeapMinPQ;
import minpq.MinPQ;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Display the most commonly-reported WCAG recommendations.
 */
public class ReportAnalyzer {
    public static void main(String[] args) throws IOException {
        File inputFile = new File("data/wcag.tsv");
        Map<String, String> wcagDefinitions = new LinkedHashMap<>();
        Scanner scanner = new Scanner(inputFile);
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split("\t", 2);
            String index = "wcag" + line[0].replace(".", "");
            String title = line[1];
            wcagDefinitions.put(index, title);
        }

        Pattern re = Pattern.compile("wcag\\d{3,4}");
        List<String> wcagTags = Files.walk(Paths.get("data/reports"))
                .map(path -> {
                    try {
                        return Files.readString(path);
                    } catch (IOException e) {
                        return "";
                    }
                })
                .flatMap(contents -> re.matcher(contents).results())
                .map(MatchResult::group)
                .toList();

        // TODO: Display the most commonly-reported WCAG recommendations using MinPQ
        MinPQ<String> wcag = new HeapMinPQ<>();

        for (int i = 0; i < wcagTags.size(); i++) {
            if (wcag.contains(wcagTags.get(i))) {
                wcag.addOrChangePriority(wcagTags.get(i), wcag.getPriority(wcagTags.get(i)) - 1);
            } else {
                wcag.add(wcagTags.get(i), -1);
            }
        }

        System.out.println(wcagDefinitions.get(wcag.removeMin()));
        System.out.println(wcagDefinitions.get(wcag.removeMin()));
        System.out.println(wcagDefinitions.get(wcag.removeMin()));
    }
}