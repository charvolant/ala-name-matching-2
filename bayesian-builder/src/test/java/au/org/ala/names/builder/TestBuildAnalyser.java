package au.org.ala.names.builder;

import au.org.ala.bayesian.*;

import java.util.Optional;
import java.util.Set;

public class TestBuildAnalyser implements BuildAnalyser<TestClassification> {
    /**
     * No extension
     */
    @Override
    public void analyseForIndex(Classifier classifier) throws InferenceException {
    }

    /**
     * Build a collection of base names for the classification.
     */
    @Override
    public Set<String> analyseNames(Classifier classifier, Observable<String> name, Optional<Observable<String>> complete, Optional<Observable<String>> disambiguator, boolean canonical) throws InferenceException {
        return classifier.getAll(TestFactory.SCIENTIFIC_NAME);
    }

    @Override
    public boolean acceptSynonym(Classifier base, Classifier candidate) {
        return true;
    }
}
