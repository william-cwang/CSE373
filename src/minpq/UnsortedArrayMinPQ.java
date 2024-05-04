package minpq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Unsorted array (or {@link ArrayList}) implementation of the {@link MinPQ} interface.
 *
 * @param <E> the type of elements in this priority queue.
 * @see MinPQ
 */
public class UnsortedArrayMinPQ<E> implements MinPQ<E> {
    /**
     * {@link List} of {@link PriorityNode} objects representing the element-priority pairs in no specific order.
     */
    private final List<PriorityNode<E>> elements;

    /**
     * Constructs an empty instance.
     */
    public UnsortedArrayMinPQ() {
        elements = new ArrayList<>();
    }

    /**
     * Constructs an instance containing all the given elements and their priority values.
     *
     * @param elementsAndPriorities each element and its corresponding priority.
     */
    public UnsortedArrayMinPQ(Map<E, Double> elementsAndPriorities) {
        elements = new ArrayList<>(elementsAndPriorities.size());
        for (Map.Entry<E, Double> entry : elementsAndPriorities.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void add(E element, double priority) {
        if (contains(element)) {
            throw new IllegalArgumentException("Already contains " + element);
        }
        elements.add(new PriorityNode<>(element, priority));
    }

    @Override
    public boolean contains(E element) {
        for (PriorityNode<E> obj : elements) {
            if (obj.getElement().equals(element)) return true;
        }
        return false;
    }

    @Override
    public double getPriority(E element) {
        for (PriorityNode<E> obj : elements) {
            if (obj.getElement().equals(element)) return obj.getPriority();
        }
        throw new IllegalArgumentException("Element does not exist.");
    }

    @Override
    public E peekMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("PQ is empty");
        }
        PriorityNode<E> getMin = elements.getFirst();
        for (PriorityNode<E> element : elements) {
            if (element.getPriority() < getMin.getPriority()) getMin = element;
        }
        return getMin.getElement();
    }

    @Override
    public E removeMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("PQ is empty");
        }
        PriorityNode<E> getMin = elements.getFirst();
        for (PriorityNode<E> element : elements) {
            if (element.getPriority() < getMin.getPriority()) getMin = element;
        }
        elements.remove(getMin);
        return getMin.getElement();
    }

    @Override
    public void changePriority(E element, double priority) {
        if (!contains(element)) {
            throw new NoSuchElementException("PQ does not contain " + element);
        }
        for (PriorityNode<E> obj : elements) {
            if (obj.getElement().equals(element)) obj.setPriority(priority);
        }
    }

    @Override
    public int size() {
        return elements.size();
    }
}
