package au.org.ala.bayesian;

import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A list of issues associated with some operation.
 * <p>
 * Issues are generally regarded as warnings, rather than errors, indicating
 * possible problems with processing and matching evidence.
 * Issues are stored as URI-based {@link Term}s, to provide an extensible way
 * of recording issues.
 * </p>
 */
public class Issues extends HashSet<Term> {
    /**
     * Create an empty issues list
     */
    public Issues() {
        super();
    }

    /**
     * Create from a list of issues.
     *
     * @param issues The list of issues
     */
    public Issues(Collection<Term> issues) {
        super();
        this.interiorAddAll(issues);
    }

    /**
     * Create a set of issues.
     *
     * @param terms The terms to add
     *
     * @return The resulting issues set
     */
    public static Issues of(Term... terms) {
        Issues issues = new Issues();
        for (Term t: terms)
            issues.interiorAdd(t);
        return issues;
    }

    /**
     * Create a set of issues from a list of strings
     *
     * @param strings The strings
     *
     * @return An issues list
     */
    public static Issues fromStrings(Collection<String> strings) {
        Issues issues = new Issues();
        final TermFactory factory = TermFactory.instance();
        issues.interiorAddAll(strings.stream().map(t -> factory.findTerm(t)).collect(Collectors.toList()));
        return issues;
    }

    /**
     * Convert a list of issues into a list of strings suitable for JSON.
     *
     * @return The list of issue strings
     */
    public List<String> asStrings() {
        return this.stream().map(Term::qualifiedName).collect(Collectors.toList());
    }

    /**
     * Throws an unsupported operation exception.
     * <p>
     * Issues look like values and should use the {@link #with} methods
     * </p>
     */
    @Override
    public boolean add(Term term) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an unsupported operation exception.
     * <p>
     * Issues look like values and should use the {@link #with} methods
     * </p>
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an unsupported operation exception.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

     private void interiorAdd(Term term) {
        super.add(term);
    }

    private void interiorAddAll(Collection<Term> terms) {
        for (Term t: terms)
            this.interiorAdd(t);
    }

    /**
     * Add another issue to this this of issues and return the merged issues list.
     *
     * @param issue The issue to add, if null no issue is added.
     *
     * @return The resulting combined issues.
     */
    public Issues with(Term issue) {
        if (issue == null || this.contains(issue))
            return this;
        Issues merged = new Issues(this);
        merged.interiorAdd(issue);
        return merged;
    }
    /**
     * Merge two sets of issues.
     * <p>
     * If this issues or the other is empty or a subset, the other issue set is returned.
     * </p>
     *
     * @param other The other issues to merge
     *
     * @return An issues collection representing the merged issues list
     */
    public Issues merge(Issues other) {
        if (this.isEmpty() || other.containsAll(this))
            return other;
        if (other.isEmpty() || this.containsAll(other))
            return this;
        Issues merged = new Issues();
        merged.interiorAddAll(this);
        merged.interiorAddAll(other);
        return merged;
    }
}
