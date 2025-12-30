package au.org.ala.bayesian;

import au.org.ala.util.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Provides application-specific analysis and extension of evidence supplied.
 * <p>
 * Subclasses can be used to fill out inferred evidence that doesn't
 * fit nicely into a network.
 * For example, it can be used to work out implied rank from a scientific name,
 * if one has not been specified.
 * </p>
 * <p>
 * Analysers are intended to be <em>services</em>.
 * They can be used by multiple clients to analyse
 * </p>
 *
 * @param <C> The classifier that this analyser handles
 *
 */
@Service
public interface Analyser<C extends Classification<C>> {
    /**
     * Analyse the information in a classification and extend the classification
     * as required in preparation for search/inference.
     *
     * @param classification The classification
     * @param options Options on what analysis to perform
     *
     * @throws InferenceException if an error occurs during inference
     */
    void analyseForSearch(C classification, MatchOptions options) throws InferenceException;

    /**
     * Write the configuration needed by this analyser into a directory.
     * <p>
     * This may include a {@link AnalyserConfig} and any ancillary files that may be needed.
     * The default implementation returns null for no configuration needed.
     * </p>
     *
     * @param directory The directory to write to
     *
     * @return The file the configuration has been written to, ot null for nothing written.
     *
     * @throws IOException if unable to write the configuration
     */
    default File writeConfiguration(File directory) throws IOException {
        return null;
    }
}
