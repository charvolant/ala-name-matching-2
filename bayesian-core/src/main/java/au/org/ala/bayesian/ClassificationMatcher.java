package au.org.ala.bayesian;

import au.org.ala.util.BacktrackingIterator;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provide search services for a classification.
 * <p>
 * A classification is provided and the best possible match for the classification
 * is then returned.
 * </p>
 */
public class ClassificationMatcher<C extends Classification<C>, P extends Parameters, I extends Inferencer<C, P>, F extends NetworkFactory<C, P, I, F>> {
    /** The default possible theshold for something to be considered. @see #isPossible */
    public static double POSSIBLE_THESHOLD = 0.1;
    /** The default immediately acceptable threshold for something to be regarded as accepted. @see @isImmediateMatch */
    public static double IMMEDIATE_THRESHOLD = 0.99;
    /** The default acceptable threshold for something. @see @isAcceptableMatch */
    public static double ACCEPTABLE_THRESHOLD = 0.90;

    private F factory;
    private ClassifierSearcher<?> searcher;
    private I inferencer;
    private Analyser<C> analyser;

    /**
     * Create with a searcher and inferencer.
     *
     * @param factory The factory for creating objects for the matcher to work on
     * @param searcher The mechanism for getting candidiates
     */
    public ClassificationMatcher(F factory, ClassifierSearcher<?> searcher) {
        this.factory = factory;
        this.searcher = searcher;
        this.inferencer = this.factory.createInferencer();
        this.analyser = this.factory.createAnalyser();
    }

    public Match<C> findMatch(C classification) throws InferenceException, StoreException {
        C model = classification.clone();
        classification.infer();
        List<? extends Classifier> candidates = this.searcher.search(classification);
        if (candidates.isEmpty())
            return null;

        // First do a basic match and see if we have something easily matchable
        List<Match<C>> results = this.doMatch(classification, candidates);
        Match<C> match = this.findSingle(results);
        if (match != null)
            return match;

        // Do we have an evidence problem?
        if (results.stream().allMatch(m -> this.isBadEvidence(m))) {
            Iterator<C> subClassifications = new BacktrackingIterator<C>(model, classification.modificationOrder());
            while (subClassifications.hasNext()) {
                C modified = subClassifications.next();
                if (modified == model) // Skip null case
                    continue;
                modified.infer();
                results = this.doMatch(modified, candidates);
                match = this.findSingle(results);
                if (match != null)
                    return match;
             }
        }
        return null;
    }

    protected List<Match<C>> doMatch(C classification, List<? extends Classifier> candidates) throws StoreException, InferenceException {
        List<Match<C>> results = new ArrayList<>(candidates.size());
        P parameters = this.factory.createParameters();
        for (Classifier candidate: candidates) {
            candidate.loadParameters(parameters);
            Inference inference = this.inferencer.probability(classification, candidate, parameters);
            if (this.isPossible(classification, candidate, inference)) {
                C candidateClassification = this.factory.createClassification();
                candidateClassification.read(candidate, true);
                Match<C> match = new Match<>(candidateClassification, candidate, inference, classification.getIssues());
                if (this.isImmediateMatch(match))
                    return Collections.singletonList(match);
                results.add(match);
            }
        }
        results.sort((m1, m2) -> - Double.compare(m1.getProbability().getPosterior(), m2.getProbability().getPosterior()));
        return results;
    }

    /**
     * Find a single match that is acceptable.
     *
     * @param results The list of results
     *
     * @return A single acceptable result, or null
     */
    protected Match<C> findSingle(List<Match<C>> results) {
        if (!results.isEmpty()) {
            Match<C> first = results.get(0);
            // Look for an immediate match
            if (this.isImmediateMatch(first))
                return first;
            // See if we have a single acceptable answer
            List<Match<C>> acceptable = results.stream().filter(m -> this.isAcceptableMatch(m)).collect(Collectors.toList());
            if (acceptable.size() == 1)
                return acceptable.get(0);
        }
        return null;
    }

    /**
     * Is this a possible match.
     * <p>
     * Generally, this means something above the minmum possible threshold
     * and with a p(E | H) greater than the p(H), meaning that the evidence
     * isn't completely unlikely.
     * Subclasses can get more creative, if required.
     * </p>
     *
     * @param classification The source classification
     * @param candidate The candidate classifier
     * @param inference The match probability
     *
     * @return True if this is a possible match
     */
    protected boolean isPossible(C classification, Classifier candidate, Inference inference) {
        return inference.getPosterior() >= POSSIBLE_THESHOLD && inference.getBoost() >= 1.0;
    }

    /**
     * Is this an immediate match, meaning that it will be used as a valid match
     * without the need to examine other possibilities.
     * <p>
     * Generally, this means something above the acceptable threshold probability
     * for both the posterior probability and the conditional evidence.
     * Subclasses can get more creative, if required.
     * </p>
     *
     * @param match The potential match
     *
     * @return True if this is a possible match
     */
    protected boolean isImmediateMatch(Match<C> match) {
        Inference p = match.getProbability();
        return p.getPosterior() >= IMMEDIATE_THRESHOLD && p.getConditional() >= IMMEDIATE_THRESHOLD;
    }

    protected boolean isAcceptableMatch(Match<C> match) {
        Inference p = match.getProbability();
        return p.getPosterior() >= ACCEPTABLE_THRESHOLD && p.getConditional() > p.getPrior();
    }

    protected boolean isBadEvidence(Match<C> match) {
        Inference p = match.getProbability();
        return p.getPrior() > p.getEvidence() || p.getPrior() > p.getConditional();
    }
}
