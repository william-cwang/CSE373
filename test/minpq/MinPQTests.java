package minpq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Random;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract class providing test cases for all {@link MinPQ} implementations.
 *
 * @see MinPQ
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MinPQTests {
    /**
     * Returns an empty {@link MinPQ}.
     *
     * @return an empty {@link MinPQ}
     */
    public abstract <E> MinPQ<E> createMinPQ();

    @Test
    public void wcagIndexAsPriority() throws FileNotFoundException {
        File inputFile = new File("data/wcag.tsv");
        MinPQ<String> reference = new DoubleMapMinPQ<>();
        MinPQ<String> testing = createMinPQ();
        Scanner scanner = new Scanner(inputFile);
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split("\t", 2);
            int index = Integer.parseInt(line[0].replace(".", ""));
            String title = line[1];
            reference.add(title, index);
            testing.add(title, index);
        }
        while (!reference.isEmpty()) {
            assertEquals(reference.removeMin(), testing.removeMin());
        }
        assertTrue(testing.isEmpty());
    }

    @Test
    public void randomPriorities() {
        int[] elements = new int[1000];
        for (int i = 0; i < elements.length; i = i + 1) {
            elements[i] = i;
        }
        Random random = new Random(373);
        int[] priorities = new int[elements.length];
        for (int i = 0; i < priorities.length; i = i + 1) {
            priorities[i] = random.nextInt(priorities.length);
        }

        MinPQ<Integer> reference = new DoubleMapMinPQ<>();
        MinPQ<Integer> testing = createMinPQ();
        for (int i = 0; i < elements.length; i = i + 1) {
            reference.add(elements[i], priorities[i]);
            testing.add(elements[i], priorities[i]);
        }

        for (int i = 0; i < elements.length; i = i+1) {
            int expected = reference.removeMin();
            int actual = testing.removeMin();

            if (expected != actual) {
                int expectedPriority = priorities[expected];
                int actualPriority = priorities[actual];
                assertEquals(expectedPriority, actualPriority);
            }
        }
    }

    @Test
    public void randomTestingInt() {
        MinPQ<Integer> reference = new DoubleMapMinPQ<>();
        MinPQ<Integer> testing = createMinPQ();

        int iterations = 10000;
        int maxElement = 1000;
        Random random = new Random();
        for (int i = 0; i < iterations; i += 1) {
            int element = random.nextInt(maxElement);
            double priority = random.nextDouble();
            reference.addOrChangePriority(element, priority);
            testing.addOrChangePriority(element, priority);
            assertEquals(reference.peekMin(), testing.peekMin());
            assertEquals(reference.size(), testing.size());
            for (int e = 0; e < maxElement; e += 1) {
                if (reference.contains(e)) {
                    assertTrue(testing.contains(e));
                    assertEquals(reference.getPriority(e), testing.getPriority(e));
                } else {
                    assertFalse(testing.contains(e));
                }
            }
        }
        for (int i = 0; i < iterations; i += 1) {
            boolean shouldRemoveMin = random.nextBoolean();
            if (shouldRemoveMin && !reference.isEmpty()) {
                assertEquals(reference.removeMin(), testing.removeMin());
            } else {
                int element = random.nextInt(maxElement);
                double priority = random.nextDouble();
                reference.addOrChangePriority(element, priority);
                testing.addOrChangePriority(element, priority);
            }
            if (!reference.isEmpty()) {
                assertEquals(reference.peekMin(), testing.peekMin());
                assertEquals(reference.size(), testing.size());
                for (int e = 0; e < maxElement; e += 1) {
                    if (reference.contains(e)) {
                        assertTrue(testing.contains(e));
                        assertEquals(reference.getPriority(e), testing.getPriority(e));
                    } else {
                        assertFalse(testing.contains(e));
                    }
                }
            } else {
                assertTrue(testing.isEmpty());
            }
        }
    }

    // Random Testing
    @Test
    public void randomTest() throws FileNotFoundException {
        File inputFile = new File("data/wcag.tsv");
        Scanner scanner = new Scanner(inputFile);
        ArrayList<String> tags = new ArrayList<>();
        ArrayList<Integer> priorities = new ArrayList<>();

        MinPQ<String> reference = new DoubleMapMinPQ<>();
        MinPQ<String> testing = new OptimizedHeapMinPQ<>();

        // Part one -- adding all of the tags to a list
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split("\t", 2);
            String title = line[1];

            tags.add(title);
        }

        // Part two -- randomly adding 10,000 tags to reference/testing PQs, adding priority values as necessary
        Random random = new Random(373);
        for (int i = 0; i < 10000; i++) {
            String element = tags.get(random.nextInt(tags.size()));

            if (!reference.contains(element)) reference.add(element, 1);
            else reference.changePriority(element, reference.getPriority(element) + 1);

            if (!testing.contains(element)) testing.add(element, 1);
            else testing.changePriority(element, testing.getPriority(element) + 1);

        }

        // Removing each element and making sure priorities are same
        // If multiple elements have same priorities, temporarily remove elements until we find a match,
        // then re-add them back.
        // We are testing both that the priorities of the current heap-min values (priorities) are the same, then
        // testing that the values removed are the same.
        // You can easily change the "3" to 5-10 or another arbitrary number.
        while (reference.size() > 3) {
            if (reference.peekMin().equals(testing.peekMin())) {
                assertEquals(reference.removeMin(), testing.removeMin());
            } else {
                Map<String, Double> removedElements = new HashMap<>();
                while (!reference.peekMin().equals(testing.peekMin())) {
                    assertEquals(reference.getPriority(reference.peekMin()), testing.getPriority(testing.peekMin()));
                    removedElements.put(testing.peekMin(), testing.getPriority(testing.peekMin()));
                    testing.removeMin();
                }
                assertEquals(reference.removeMin(), testing.removeMin());
                for (Map.Entry<String, Double> i : removedElements.entrySet()) {
                    testing.add(i.getKey(), i.getValue());
                }
            }
        }

        // Printing out the top # maximum values
        while (!reference.isEmpty()) {
            testing.removeMin();
            System.out.println(reference.getPriority((reference.peekMin())) + " Occurences: " +
                    reference.removeMin());
        }
    }

}
