package minpq;

import java.util.ArrayList;

public class Test {

    public static void main (String[] args) {
        MinPQ<String> pq = new DoubleMapMinPQ<>();
        pq.add("1", 1.0);
        pq.add("2", 2.0);
        pq.add("3", 3.0);
        pq.add("4", 4.0);
        pq.add("5", 5.0);
        pq.add("6", 6.0);

        // Call methods to evaluate behavior.
        pq.changePriority("3", 0.0);
        pq.changePriority("1", 7.0);
        while (!pq.isEmpty()) {
            System.out.println(pq.removeMin());
        }

        MinPQ<String> pq2 = new OptimizedHeapMinPQ<>();
        pq2.add("1", 1.0);
        pq2.add("2", 2.0);
        pq2.add("3", 3.0);
        pq2.add("4", 4.0);
        pq2.add("5", 5.0);
        pq2.add("6", 6.0);

        // Call methods to evaluate behavior.
        pq2.changePriority("3", 0.0);
        pq2.changePriority("1", 7.0);
        pq2.removeMin();

//        while (!pq2.isEmpty()) {
//            System.out.println(pq2.removeMin());
//        }


    }
}
