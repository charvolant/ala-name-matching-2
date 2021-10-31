package au.org.ala.vocab;

import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;

import java.net.URI;

/**
 * Terms used for optimisating search, loading sources and the like.
 * <p>
 * These contain a number of elements that should really be in sub-modules but which are not properly linked
 * if they are absent.
 * </p>
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 *
 * Copyright (c) 2016 CSIRO
 */
public enum OptimisationTerm implements Term {
    /** Do not use this observable for candidate search in Lucene, usually for performance reasons */
    luceneNoSearch,
    /**
     * Aggregate multiple terms to a single value when loading from a source.
     * The value gives the aggregation operation.
     * Possible values are:
     * <dl>
     *     <dt>first</dt><dd>The default, return the first non-null value</dd>
     *     <dt>max</dt><dd>Return the maximum value of a {@link Comparable} class</dd>
     *     <dt>min</dt><dd>Return the minimum value of a {@link Comparable} class</dd>
     * </dl>
     */
    aggregate;

    public static final String NS = "http://id.ala.org.au/optimisation/1.0/";
    public static final URI NAMESPACE = URI.create(NS);
    public static final String PREFIX = "opt";

    @Override
    public String qualifiedName() {
        return NS + this.simpleName();
    }

    @Override
    public String prefix() {
        return PREFIX;
    }

    @Override
    public URI namespace() {
        return NAMESPACE;
    }

    @Override
    public String simpleName() {
        return this.name();
    }

    public String toString() {
         return this.prefixedName();
    }

    @Override
    public boolean isClass() {
        return Character.isUpperCase(this.simpleName().charAt(0));
    }

    static {
        TermFactory factory = TermFactory.instance();
        for (Term term : OptimisationTerm.values()) {
            factory.registerTerm(term);
        }
    }

}
