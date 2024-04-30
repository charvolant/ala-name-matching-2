package au.org.ala.names;

import au.org.ala.bayesian.*;

import java.util.List;
import java.util.stream.Collectors;

public class ALAVernacularClassificationMatcher extends ClassificationMatcher<AlaVernacularClassification, AlaVernacularInferencer, AlaVernacularFactory, MatchMeasurement> {
    /** The default immediately acceptable threshold for something to be regarded as accepted. @see #isImmediateMatch */
    public static double POSSIBLE_THRESHOLD = 0.02;
    /** The default immediately acceptable threshold for something to be regarded as accepted. @see #isImmediateMatch */
    public static double IMMEDIATE_THRESHOLD = 0.90;
    /** The default acceptable threshold for something. @see #isAcceptableMatch */
    public static double ACCEPTABLE_THRESHOLD = 0.70;

    /**
     * Create with a searcher and inferencer.
     *
     * @param factory  The factory for creating objects for the matcher to work on
     * @param searcher The mechanism for getting candidiates
     * @param config The classificatio configuration
     * @param analyserConfig The analyser configuration
     */
    public ALAVernacularClassificationMatcher(AlaVernacularFactory factory, ClassifierSearcher<?> searcher, ClassificationMatcherConfiguration config, AnalyserConfig analyserConfig) {
        super(factory, searcher, config, analyserConfig);
    }

    /**
     * Find a single match that is acceptable.
     *
     * @param results The list of results
     * @return A single acceptable result, or null
     */
    @Override
    protected Match<AlaVernacularClassification, MatchMeasurement> findSingle(final AlaVernacularClassification classification, final List<Match<AlaVernacularClassification, MatchMeasurement>> results, MatchMeasurement measurement) {
        if (results.isEmpty())
            return null;
        if (results.size() == 1 && !this.isBadEvidence(results.get(0)))
            return results.get(0);
        // Combine vernacular names pointing to the same thing
        final List<String> ids = results.stream().map(m -> m.getAccepted().taxonId).distinct().collect(Collectors.toList());
        final List<Match<AlaVernacularClassification, MatchMeasurement>> combined = ids.stream().map(id -> {
            final Match<AlaVernacularClassification, MatchMeasurement> first = results.stream().filter(m -> id.equals(m.getAccepted().taxonId)).findFirst().get();
            final List<Match<AlaVernacularClassification, MatchMeasurement>> boosts = results.stream().filter(m -> m != first && id.equals(m.getAccepted().taxonId)).collect(Collectors.toList());
            return first.boost(boosts);
        })
                .sorted(this.getMatchSorter())
                .collect(Collectors.toList());
        List<Match<AlaVernacularClassification, MatchMeasurement>> usable = results.stream().filter(m -> !this.isBadEvidence(m)).collect(Collectors.toList());
        if (usable.isEmpty())
            return null;
        return usable.get(0);
    }

    /**
     * Is this a possible match.
     * <p>
     * We'll accept anything that vaguely looks right according to the soundex match.
     * </p>
     *
     * @param classification The source classification
     * @param candidate The candidate classifier
     * @param inference The match probability
     *
     * @return True if this is a possible match
     */
    @Override
    protected boolean isPossible(AlaVernacularClassification classification, Classifier candidate, Inference inference) {
        try {
            return classification.soundexVernacularName != null && candidate.match(classification.soundexVernacularName, AlaVernacularFactory.soundexVernacularName);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Since we're working off vernacular names, be relaxed
     *
     * @param match The potential match
     *
     * @return True if this is a possible match
     */
    protected boolean isImmediateMatch(Match<AlaVernacularClassification, MatchMeasurement> match) {
        Inference p = match.getProbability();
        return p.getPosterior() >= IMMEDIATE_THRESHOLD && p.getConditional() >= IMMEDIATE_THRESHOLD;
    }

    /**
     * Since we're working off vernacular names, be relaxed
     *
     * @param match The candidate match
     *
     * @return True if this is an acceptable match
     */
    protected boolean isAcceptableMatch(Match<AlaVernacularClassification, MatchMeasurement> match) {
        Inference p = match.getProbability();
        return p.getPosterior() >= ACCEPTABLE_THRESHOLD && p.getEvidence() >= EVIDENCE_THRESHOLD && p.getConditional() >= POSSIBLE_THRESHOLD;
    }

}
