package au.org.ala.location;

import au.org.ala.bayesian.*;
import au.org.ala.names.builder.BuildAnalyser;
import au.org.ala.util.BasicNormaliser;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AlaLocationBuildAnalyser implements BuildAnalyser<AlaLocationClassification> {
    /** Unaccented version of the name */
    private static final BasicNormaliser CLEAN_NORMALISER = new BasicNormaliser("clean", true, true, false, false, true, false);

    /**
     * Create for a configuration.
     *
     * @param config The configuration (unused)
     */
    public AlaLocationBuildAnalyser(AnalyserConfig config) {
    }

    @Override
    public void analyseForIndex(Classifier classifier) throws InferenceException, StoreException {
    }

    @Override
    public Set<String> analyseNames(Classifier classifier, Observable<String> name, Optional<Observable<String>> complete, Optional<Observable<String>> disambiguator, boolean canonical) throws InferenceException {
        final Set<String> names = new LinkedHashSet<>();
        this.addNames(names, classifier.getAll(name));
        complete.ifPresent(o -> this.addNames(names, classifier.getAll(o)));
        disambiguator.ifPresent(o -> this.addNames(names, classifier.getAll(o)));
        return names;
    }

    /**
     * Add all variants of names
     *
     * @param names The collected names
     * @param add The base names to add
     */
    protected void addNames(Set<String> names, Set<String> add) {
        names.addAll(add);
        names.addAll(add.stream().map(n -> CLEAN_NORMALISER.normalise(n)).collect(Collectors.toList()));
    }

    @Override
    public boolean acceptSynonym(Classifier base, Classifier candidate) {
        return false;
    }
}
