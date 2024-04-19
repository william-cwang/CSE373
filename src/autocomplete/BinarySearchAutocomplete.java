package autocomplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Binary search implementation of the {@link Autocomplete} interface.
 *
 * @see Autocomplete
 */
public class BinarySearchAutocomplete implements Autocomplete {
    /**
     * {@link List} of added autocompletion terms.
     */
    private final List<CharSequence> elements;

    /**
     * Constructs an empty instance.
     */
    public BinarySearchAutocomplete() {
        elements = new ArrayList<>();
    }

    @Override
    public void addAll(Collection<? extends CharSequence> terms) {
        elements.addAll(terms);
        elements.sort(CharSequence::compare);
    }

    @Override
    public List<CharSequence> allMatches(CharSequence prefix) {
        List<CharSequence> matches = new ArrayList<>();
        int counter = 0;
        boolean keepRunning = true;

        while (counter < elements.size() && !Autocomplete.isPrefixOf(prefix, elements.get(counter))) {
            counter++;
        }

        while (counter < elements.size() && keepRunning) {
            if (Autocomplete.isPrefixOf(prefix, elements.get(counter))) {
                matches.add(elements.get(counter));
            } else if (!Autocomplete.isPrefixOf(prefix, elements.get(counter))) {
                keepRunning = false;
            }
            counter++;
        }

        return matches;
    }
}
