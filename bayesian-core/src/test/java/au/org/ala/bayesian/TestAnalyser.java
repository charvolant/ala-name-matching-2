package au.org.ala.bayesian;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class TestAnalyser implements Analyser<TestClassification> {
    /**
     * No extension
     */
    @Override
    public void analyseForSearch(TestClassification classification, MatchOptions options) throws InferenceException {
    }
}
