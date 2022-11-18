package autocomplete;

import java.util.*;

/**
 * {@link TreeSet} implementation of the {@link Autocomplete} interface.
 *
 * @see Autocomplete
 */
public class TreeSetAutocomplete implements Autocomplete {
    /**
     * {@link NavigableSet} of added autocompletion terms.
     */
    private final NavigableSet<CharSequence> items;

    /**
     * Constructs an empty instance.
     */
    public TreeSetAutocomplete() {
        items = new TreeSet<>(CharSequence::compare);
    }

    @Override
    public void addAll(Collection<? extends CharSequence> terms) {
        items.addAll(terms);
    }

    @Override
    public List<CharSequence> allMatches(CharSequence prefix) {
        List<CharSequence> result = new ArrayList<>();
        if (prefix == null || prefix.length() == 0) {
            return result;
        }
        CharSequence start = items.ceiling(prefix);
        if (start == null) {
            return result;
        }
        for (CharSequence term : items.tailSet(start)) {
            if (Autocomplete.isPrefixOf(prefix, term)) {
                result.add(term);
            } else {
                return result;
            }
        }
        return result;
    }
}
