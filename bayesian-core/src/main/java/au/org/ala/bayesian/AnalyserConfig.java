package au.org.ala.bayesian;

import au.org.ala.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration options for a name analyser.
 */
@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AnalyserConfig {
    private static final Logger logger = LoggerFactory.getLogger(AnalyserConfig.class);

    /**
     * The default file name to store configurations to
     */
    public static final String DEFAULT_CONFIG_FILE_NAME = "analyser-config.json";

    /**
     * Any simple string parameters that are specified.
     */
    @JsonProperty
    @Builder.Default
    private Map<String, String> parameters = new HashMap<>();

    /**
     * Any URL parameters that are specified.
     */
    @JsonProperty
    @Builder.Default
    private Map<String, URL> urls = new HashMap<>();

    /**
     * Add a simple string parameter to the configuration
     *
     * @param key The parameter key
     * @param value The parameter value
     */
    public AnalyserConfig with(String key, String value) {
        Map<String, String> np = new HashMap<>(this.parameters);
        np.put(key, value);
        return AnalyserConfig.builder()
                .parameters(np)
                .urls(this.urls)
                .build();
    }

    /**
     * Get a parameter from the configuration
     *
     * @param key The parameter key
     * @param dflt The default value
     */
    public String getParameter(String key, String dflt) {
        return this.parameters.getOrDefault(key, dflt);
    }

    /**
     * Add a simple string parameter to the configuration
     *
     * @param key The parameter key
     * @param value The parameter value
     */
    public AnalyserConfig with(String key, URL value) {
        Map<String, URL> nu = new HashMap<>(this.urls);
        nu.put(key, value);
        return AnalyserConfig.builder()
                .parameters(this.parameters)
                .urls(nu)
                .build();
    }

    /**
     * Get a URL parameter from the configuration
     *
     * @param key The parameter key
     * @param dflt The default value
     */
    public URL getURL(String key, URL dflt) {
        return this.urls.getOrDefault(key, dflt);
    }

    /**
     * Store the configuration.
     *
     * @param writer The writer to store the configuration to
     * @throws IOException if unable to write the configuration
     */
    public void store(Writer writer) throws IOException {
        JsonUtils.createMapper().writeValue(writer, this);
    }

    /**
     * Relativise a URL with respect to a directory
     *
     * @param base The base URL
     * @param directory The directory
     *
     * @return The relativised URL
     *
     * @throws IOException if unable to construct the URL
     */
    @SneakyThrows(IOException.class)
    private URL relative(URL base, File directory) {
        if (base != null && base.getProtocol().equals("file")) {
            try {
                Path sp = Paths.get(base.getPath()).normalize();
                Path dp = directory.toPath().normalize();
                if (sp.startsWith(dp))
                    base = new URL("file:" + dp.relativize(sp).toString());
            } catch (Exception ex) {
                throw new IOException("Unable to rebase " + base + " against " + directory, ex);
            }
        }
        return base;
    }

    /**
     * Create a configuration relative to a directory.
     *
     * @param directory The directory
     * @return The relativised configuration
     * @throws IOException if unable to relativise the configuration
     */
    public AnalyserConfig relative(File directory) throws IOException {
        Map<String, URL> rebased = this.urls.entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.toMap(e -> e.getKey(), e -> this.relative(e.getValue(), directory)));
        return AnalyserConfig.builder()
                .parameters(this.parameters)
                .urls(rebased)
                .build();
    }

    /**
     * Absolutise a URL with respect to a directory
     *
     * @param base The base URL
     * @param directory The directory
     *
     * @return The relativised URL
     *
     * @throws IOException if unable to construct the URL
     */
    @SneakyThrows(IOException.class)
    private URL absolute(URL base, File directory) {
        if (base != null && base.getProtocol().equals("file")) {
            try {
                Path sp = Paths.get(base.getPath()).normalize();
                Path dp = directory.toPath().normalize();
                base = dp.resolve(sp).toUri().toURL();
            } catch (Exception ex) {
                throw new IOException("Unable to absolutise " + base + " against " + directory, ex);
            }
        }
        return base;
    }

    /**
     * Create a configuration with absolute paths.
     *
     *
     * @param directory The directory that forms the base
     *
     * @return The absolute configuration
     */
    public AnalyserConfig absolute(File directory)  {
            Map<String, URL> rebased = this.urls.entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.toMap(e -> e.getKey(), e -> this.absolute(e.getValue(), directory)));
            return AnalyserConfig.builder()
                    .parameters(this.parameters)
                    .urls(rebased)
                    .build();
        }

    /**
     * Load an analyser configuration from a directory.
     *
     * @param directory The directory
     *
     * @return Either the configuration, made absolute to the directory or a default configutation
     *
     * @throws IOException if unable to read the configuation
     */
    public static AnalyserConfig load(File directory) throws IOException {
        File configFile = new File(directory, DEFAULT_CONFIG_FILE_NAME);
        if (!configFile.canRead()) {
            logger.info("No analyser configuration in " + directory + " using default");
            return AnalyserConfig.builder().build();
        }
        ObjectMapper mapper = JsonUtils.createMapper();
        AnalyserConfig config = mapper.readValue(configFile, AnalyserConfig.class);
        return config.absolute(directory);
    }
}
