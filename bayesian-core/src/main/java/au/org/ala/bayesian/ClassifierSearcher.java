package au.org.ala.bayesian;

import au.org.ala.util.Metadata;
import au.org.ala.util.Service;
import org.gbif.dwc.terms.Term;

import java.util.List;

/**
 * A searcher for classifiers that fit a classification.
 * <p>
 * The searcher looks for names and provides a selection of candidates.
 * </p>
 *
 * @param <C> The type of classification used to search
 */
@Service
abstract public class ClassifierSearcher<C extends Classifier> implements AutoCloseable {
    /**
     * Get the observable that is the key marker for the classifier
     *
     * @return The key observable
     */
    abstract public Observable<String> getKey();
    /**
     * Search for a classifier by identifier.
     *
     * @param type The type of classifier to get
     * @param identifier The identifier observable
     * @param id The id to search for
     *
     * @return An matching classifier or null for not found
     *
     * @throws BayesianException if unable to retrieve information about the classifier
     */
    abstract public C get(Term type, Observable identifier, Object id) throws BayesianException;

    /**
     * Search for all classifiers by identifier.
     *
     * @param type The type of classifier to get
     * @param identifier The identifier observable
     * @param id The id to search for
     *
     * @return All classifiers in retrieval order
     *
     * @throws BayesianException if unable to retrieve information about the classifier
     */
    abstract public List<C> getAll(Term type, Observable identifier, Object id) throws BayesianException;

    /**
     * Search for a set of possible candidate classifiers that match the supplied classification.
     *
     * @param classification The classification
     *
     * @return A list of potential classifiers
     *
     * @throws BayesianException if unable to correctly match the classifiers
     */
    abstract public List<C> search(Classification classification) throws BayesianException;

    /**
     * Get the classifier metadata.
     *
     * @return The metadata
     */
    abstract public Metadata getMetadata();
}
