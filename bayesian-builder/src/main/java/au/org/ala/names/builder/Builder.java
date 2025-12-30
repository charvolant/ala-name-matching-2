package au.org.ala.names.builder;

import au.org.ala.bayesian.*;

import java.util.Deque;
import java.util.List;
import java.util.function.Function;

/**
 * A class that can be used to derive all the additional, inferred
 * information from an inference network.
 * <p>
 * Subclasses are generated based on the network configuration.
 * </p>
 */
public interface Builder<C extends Classification<C>> {
    /**
     * Get the erasure signature for this builder.
     *
     * @return The erasure signature, null for an erasure-insentitive, top-level builder.
     *
     * @see Inferencer#getSignature()
     */
    String getSignature();

    /**
     * Generate for a classifier during building.
     * <p>
     * This method generates any derivations that imply a once-off generation
     * of something.
     * </p>
     *
     * @param classifier The document
     * @param analyser The document analyser
     * @param buildAnalyser The builder analyser
     *
     * @throws BayesianException if unable to calculate the inference
      */
    void generate(Classifier classifier, Analyser<C> analyser, BuildAnalyser<C> buildAnalyser) throws BayesianException;

    /**
     * Interpret values in a classifier.
     * <p>
     * Perform any immediate derivations that can be collected from the classifier.
     * </p>
     *
     * @param classifier The document
     * @param analyser The document analyser
     * @param buildAnalyser The builder analyser
     *
     * @throws BayesianException if unable to calculate the inference
     */
    void interpret(Classifier classifier, Analyser<C> analyser, BuildAnalyser<C> buildAnalyser) throws BayesianException;

    /**
     * Infer from a classifier during building.
     * <p>
     * This method creates all the derived values from the incoming classifier.
     * </p>
     *
     * @param classifier The document
     * @param analyser The document analyser
     * @param buildAnalyser The builder analyser
     *
     * @throws BayesianException if unable to calculate the inference
      */
    void infer(Classifier classifier, Analyser<C> analyser, BuildAnalyser<C> buildAnalyser) throws BayesianException;

    /**
     * Expand a classifier during building.
     * <p>
     * This method inserts new values from the classifier and the stack of parent classifiers
     * </p>
     *
     * @param document The classifier
     * @param parents The classifier's parents
     * @param analyser The document analyser
     * @param buildAnalyser The builder analyser
     *
     * @throws BayesianException if unable to calculate the expansion
    */
    void expand(Classifier document, Deque<Classifier> parents, Analyser<C> analyser, BuildAnalyser<C> buildAnalyser) throws BayesianException;

    /**
     * Get any broadener for thisd classifier.
     *
     * @param document The document to be broadened
     * @param analyser An analyster
     * @param buildAnalyser The builder analyser
     *
     * @return A function that takes another classification and determines whether to use it to broaden the original classifier
     *
     * @throws BayesianException if unable to determine the broadening.
     */
    Function<Classifier, Boolean> getBroadener(Classifier document, Analyser<C> analyser, BuildAnalyser<C> buildAnalyser) throws BayesianException;

    /**
     * Build a signture for a classifier.
     * This gives the inference required by the type of information available in the classifier.
     *
     * @param classifier The classifier to build a signature for
     *
     * @return The classifier signature
     *
     * @see Inferencer#getSignature()
     */
    String buildSignature(Classifier classifier);

    /**
     * Calculate parameter values for a particular document.
     *
     * @param analyser The parameter analyser
     * @param document The document
     *
     * @return The parameters for this document
     *
     * @throws BayesianException if unable to calculate the parameters
     */
    Parameters calculate(ParameterAnalyser analyser, Classifier document) throws BayesianException;
}
