package au.org.ala.bayesian;

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
public class NullAnalyser<C extends Classification<C>> implements Analyser<C> {
    public NullAnalyser() {
    }

    public NullAnalyser(AnalyserConfig config) {
    }

    @Override
    public void analyseForSearch(C classification, MatchOptions options) {
    }

    @Override
    public File writeConfiguration(File directory) throws IOException {
        return null;
    }
}
