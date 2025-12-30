package au.org.ala.names.builder;

import au.org.ala.bayesian.*;

import java.util.Optional;
import java.util.Set;

/**
 * Analysis required by the index builder
 *
 * @param <C> The classification this analyser is for
 */
public interface BuildAnalyser<C extends Classification<C>> {
    /**
     * Analyse the information in a classifier and extend the classifier
     * as required in preparation for building an index.
     * <p>
     * Generally, if there is a recoverable problem here, fix it.
     * And if the name looks weird, then that's what the source says.
     * </p>
     *
     * @param classifier The classification
     *
     * @throws InferenceException if an error occurs during inference
     */
    void analyseForIndex(Classifier classifier) throws InferenceException, StoreException;

    /**
     * Build a collection of base names for the classification.
     * <p>
     * If a classification can be referred to in multiple ways, this method
     * builds the various ways of referring to the classification.
     * </p>
     * <p>
     * As a general principle, if {@link #analyseForIndex(Classifier)} and
     * {@link Analyser#analyseForSearch(Classification, MatchOptions)} would produce different results,
     * this method should ensure that whatever is in the search is included in the
     * set of names.
     * </p>
     *
     * @param classifier The classification
     * @param name The observable that gives the name
     * @param complete The observable that gives the complete name
     * @param disambiguator The observable that gives disambiguator disambiguation, geneerally complete = name + ' ' + disambiguator
     * @param canonical Only include canonical names
     *
     * @return All the names that refer to the classification
     *
     * @throws InferenceException if unable to analyuse the names
     */
    Set<String> analyseNames(Classifier classifier, Observable<String> name, Optional<Observable<String>> complete, Optional<Observable<String>> disambiguator, boolean canonical) throws InferenceException;

    /**
     * Decide whether to accept a synonym or not.
     * <p>
     * Some classifiers are synonyms of classifications but we really don't want to use them, since
     * they represent unusual or inaccurate synonyms.
     * </p>
     *
     * @param base The base classifier the synonym is for
     * @param candidate The classifier for the synonym
     *
     * @return True if this is an acceptable synonym.
     */
    boolean acceptSynonym(Classifier base, Classifier candidate);

}
