package minpq;

import java.util.*;

/**
 * Optimized binary heap implementation of the {@link MinPQ} interface.
 *
 * @param <E> the type of elements in this priority queue.
 * @see MinPQ
 */
public class OptimizedHeapMinPQ<E> implements MinPQ<E> {
    /**
     * {@link List} of {@link PriorityNode} objects representing the heap of element-priority pairs.
     */
    private final List<PriorityNode<E>> elements;
    /**
     * {@link Map} of each element to its associated index in the {@code elements} heap.
     */
    private final Map<E, Integer> elementsToIndex;

    /**
     * Constructs an empty instance.
     */
    public OptimizedHeapMinPQ() {
        elements = new ArrayList<>();
        elementsToIndex = new HashMap<>();
    }

    /**
     * Constructs an instance containing all the given elements and their priority values.
     *
     * @param elementsAndPriorities each element and its corresponding priority.
     */
    public OptimizedHeapMinPQ(Map<E, Double> elementsAndPriorities) {
        elements = new ArrayList<>(elementsAndPriorities.size());
        elementsToIndex = new HashMap<>(elementsAndPriorities.size());

        elements.add(null);

        // Credit to (for the for loop):
        // https://stackoverflow.com/questions/46898/how-do-i-efficiently-iterate-over-each-entry-in-a-java-map
        for (Map.Entry<E, Double> i : elementsAndPriorities.entrySet()) {
            add(i.getKey(), i.getValue());
        }
    }

    @Override
    public void add(E element, double priority) {
        if (contains(element)) {
            throw new IllegalArgumentException("Already contains " + element);
        } else if (elements.isEmpty()) elements.add(null);

        elements.add(new PriorityNode<E>(element, priority));
        elementsToIndex.put(element, elements.size() - 1);
        swim(elements.size() - 1);
    }

    /*
    * Swims one element UP if it is less than its parent.
    * */
    private void swim(int index) {
        if (elements.get(index / 2) != null &&
                elements.get(index).getPriority() < elements.get(index / 2).getPriority()) {

            // SWAP elements
            swap(index, index / 2);

            // SWIM again
            swim(index / 2);
        }
    }

    private void sink(int index) {

        int leftChildIndex = index * 2;
        int rightChildIndex = index * 2 + 1;
        int minChildIndex = getMinChildIndex(leftChildIndex, rightChildIndex);
        if (minChildIndex > 0 && minChildIndex < elements.size() &&
                elements.get(index).getPriority() > elements.get(minChildIndex).getPriority()) {
            swap(index, minChildIndex);
            sink(minChildIndex);
        }

    }

    private int getMinChildIndex(int leftChildIndex, int rightChildIndex) {
        int minChildIndex = 0;

        if (leftChildIndex != 0 && rightChildIndex > elements.size() - 1) {
            minChildIndex = leftChildIndex;

            // Bug: isn't correctly identifying indexes
        } else if (leftChildIndex != 0 && rightChildIndex <= elements.size() - 1 &&
                elements.get(leftChildIndex).getPriority() < elements.get(rightChildIndex).getPriority()) {
            minChildIndex = leftChildIndex;
        } else if (leftChildIndex != 0 && rightChildIndex <= elements.size() - 1 &&
                elements.get(rightChildIndex) != null) {
            minChildIndex = rightChildIndex;
        }
        return minChildIndex;
    }

    // Arguments are from the perspective of the current node.
    private void swap(int from, int to) {
        PriorityNode<E> currentElement = elements.get(from);
        PriorityNode<E> swappedElement = elements.get(to);

        elements.set(to, currentElement);
        elements.set(from, swappedElement) ;

        elementsToIndex.put(currentElement.getElement(), to);
        elementsToIndex.put(swappedElement.getElement(), from);
    }

    @Override
    public boolean contains(E element) {
        return elementsToIndex.containsKey(element);
    }

    @Override
    public double getPriority(E element) {
        return elements.get(elementsToIndex.get(element)).getPriority();
    }

    @Override
    public E peekMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("PQ is empty");
        }
        return elements.get(1).getElement();
    }

    @Override
    public E removeMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("PQ is empty");
        }
        E elementRemove = peekMin();
        swap(1, size());
        elements.remove(size());
        elementsToIndex.remove(elementRemove);
        sink(1);
        return elementRemove;
    }

    @Override
    public void changePriority(E element, double priority) {
        if (!contains(element)) {
            throw new NoSuchElementException("PQ does not contain " + element);
        }
        int oldIndex = elementsToIndex.get(element);

        elements.get(oldIndex).setPriority(priority);

        swim(oldIndex);
        sink(oldIndex);

    }

    @Override
    public int size() {
        return elements.size() - 1;
    }
}
