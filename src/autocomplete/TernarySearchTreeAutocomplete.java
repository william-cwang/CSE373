package autocomplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Ternary search tree (TST) implementation of the {@link Autocomplete} interface.
 *
 * @see Autocomplete
 */
public class TernarySearchTreeAutocomplete implements Autocomplete {
    /**
     * The overall root of the tree: the first character of the first autocompletion term added to this tree.
     */
    private Node overallRoot;

    /**
     * Constructs an empty instance.
     */
    public TernarySearchTreeAutocomplete() {
        overallRoot = null;
    }

    @Override
    public void addAll(Collection<? extends CharSequence> terms) {
        if (terms == null) {
            throw new IllegalArgumentException("Empty collection terms for addAll method");
        }
        for (CharSequence term: terms) {
            overallRoot = addNode(overallRoot, term, 0);
        }
    }

    private Node addNode(Node curr, CharSequence term, int i) {
        char currChar = term.charAt(i);
        if (curr == null) {
            curr = new Node(currChar);
        }
        if (curr.data > currChar) {
            curr.left = addNode(curr.left, term, i);
        } else if (curr.data < currChar) {
            curr.right = addNode(curr.right, term, i);
        } else if (i < term.length() - 1) {
            curr.mid = addNode(curr.mid, term, i + 1);
        } else if (i == term.length() - 1) {
            curr.isTerm = true;
        }

        return curr;
    }

    @Override
    public List<CharSequence> allMatches(CharSequence prefix) {
        List<CharSequence> terms = new ArrayList<CharSequence>();
        if (prefix == null || prefix.isEmpty()) return terms;

        Node subTree = get(overallRoot, prefix, 0);

        if (subTree == null) return terms;
        if (subTree.isTerm) terms.add(prefix);
        collect(subTree.mid, prefix + "", terms);

        return terms;
    }

    private Node get(Node curr, CharSequence prefix, int i) {
        if (curr == null) return null;
        char currChar = prefix.charAt(i);

        if (curr.data < currChar) {
            curr = get(curr.right, prefix, i);
        } else if (curr.data > currChar) {
            curr = get(curr.left, prefix, i);
        } else if (i < prefix.length() - 1) {
            curr = get(curr.mid, prefix, i + 1);
        }

        return curr;

    }

    private void collect(Node curr, String currString, List<CharSequence> terms) {
        if (curr == null) return;
        if (curr.isTerm) {
            terms.add(currString + curr.data);
        }

        collect(curr.left, currString, terms);
        collect(curr.mid, currString + curr.data, terms);
        collect(curr.right, currString, terms);
    }

    /**
     * A search tree node representing a single character in an autocompletion term.
     */
    private static class Node {
        private final char data;
        private boolean isTerm;
        private Node left;
        private Node mid;
        private Node right;

        public Node(char data) {
            this.data = data;
            this.isTerm = false;
            this.left = null;
            this.mid = null;
            this.right = null;
        }
    }
}
