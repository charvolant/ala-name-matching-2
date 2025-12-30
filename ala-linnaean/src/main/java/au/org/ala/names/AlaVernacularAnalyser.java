package au.org.ala.names;

import au.org.ala.bayesian.Analyser;
import au.org.ala.bayesian.AnalyserConfig;
import au.org.ala.bayesian.MatchOptions;

public class AlaVernacularAnalyser implements Analyser<AlaVernacularClassification> {
    /**
     * Construct for am ignored configuration
     *
     * @param config The configuration
     */
    public AlaVernacularAnalyser(AnalyserConfig config) {
    }

    /**
     * Analyse the information in a classifier and extend the classifier
     * as required for searching.
     *
     * @param classification The classification
     * @param options The match options to use
     */
    @Override
    public void analyseForSearch(AlaVernacularClassification classification, MatchOptions options)  {
    }
}
