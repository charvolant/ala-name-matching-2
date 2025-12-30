package au.org.ala.names.builder;

import au.org.ala.bayesian.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * A default, null analyser that performs no special analysis.
 * <p>
 * This class is present so that there can always be an analyser object.
 * If no analyser is specified, the null analyser is used.
 * </p>
 *
 * @param <C> The classification type.
 */
public class NullBuildAnalyser<C extends Classification<C>> implements BuildAnalyser<C> {
    public NullBuildAnalyser() {
    }

    public NullBuildAnalyser(AnalyserConfig config) {
    }

    @Override
    public void analyseForIndex(Classifier classifier) {
    }

    @Override
    public Set<String> analyseNames(Classifier classifier, Observable<String> name, Optional<Observable<String>> complete, Optional<Observable<String>> disambiguator, boolean canonical) {
        if (classifier.has(name))
            return Collections.singleton(classifier.get(name));
        return Collections.emptySet();
    }

    @Override
    public boolean acceptSynonym(Classifier base, Classifier candidate) {
        return true;
    }
}
