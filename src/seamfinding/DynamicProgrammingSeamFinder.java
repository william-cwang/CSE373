package seamfinding;

import seamfinding.energy.EnergyFunction;

import java.util.Collections;
import java.util.*;

/**
 * Dynamic programming implementation of the {@link SeamFinder} interface.
 *
 * @see SeamFinder
 */
public class DynamicProgrammingSeamFinder implements SeamFinder {

    @Override
    public List<Integer> findHorizontal(Picture picture, EnergyFunction f) {
        Double[][] arr = new Double[picture.width()][picture.height()];

        // Source Nodes
        for (int i = 0; i < picture.height(); i++) {
            arr[0][i] = f.apply(picture, 0, i);
        }

        // Intermediate Nodes
        for (int i = 1; i < picture.width(); i++) {
            // Top Border Node
            double topNodeMin = Math.min(arr[i - 1][0], arr[i - 1][1]);
            arr[i][0] = f.apply(picture, i, 0) + topNodeMin;

            // Middle Nodes
            for (int j = 1; j < picture.height() - 1; j++) {
                arr[i][j] = f.apply(picture, i, j) + Math.min(Math.min(arr[i - 1][j - 1], arr[i - 1][j]), arr[i - 1][j + 1]);
            }
            arr[i][picture.height() - 1] = f.apply(picture, i, picture.height() - 1) + Math.min(arr[i - 1][picture.height() - 2], arr[i - 1][picture.height() - 1]);
        }

        // Traverse the right-most part of array to find least element.
        int minSink = 0;
        for (int i = 0; i < picture.height(); i++) {
            if (arr[picture.width() - 1][i] < arr[picture.width() - 1][minSink]) minSink = i;
        }

        List<Integer> results = new ArrayList<>(List.of(minSink));
        for (int i = picture.width() - 1; i > 0; i--) {
            int lowerNeighbor = Math.max(0, minSink - 1);
            int upperNeighbor = Math.min(picture.height() - 1, minSink + 1);
            if (arr[i - 1][lowerNeighbor] > arr[i - 1][upperNeighbor]) lowerNeighbor = upperNeighbor;
            if (arr[i - 1][lowerNeighbor] < arr[i - 1][minSink]) minSink = lowerNeighbor;

            results.add(minSink);
        }

        Collections.reverse(results);
        return results;
    }
}
