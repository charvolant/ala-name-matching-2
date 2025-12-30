package au.org.ala.names.builder;

import au.org.ala.bayesian.AnalyserConfig;
import au.org.ala.bayesian.Classification;
import au.org.ala.bayesian.Inferencer;
import au.org.ala.bayesian.NetworkFactory;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.IParameterSplitter;
import com.beust.jcommander.converters.URLConverter;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic implementation of a builder CLI.
 *
 * @param <C> The classification class
 * @param <B> The builder class
 * @param <I> The inferencer class
 * @param <F> The factory class
 */
abstract public class AbstractCli<C extends Classification<C>, B extends Builder, I extends Inferencer<C>, F extends NetworkFactory<C, I, F>> implements Cli<C, B, I, F> {
    @Parameter(names = "--help", help = true)
    @Getter
    private boolean help = false;
    /** The configuration file */
    @Parameter(names = {"-f", "--config-url"}, description = "An external JSON configuration file (other options override this file)", converter=URLConverter.class)
    @Getter
    private URL configUrl = null;
    /** The work directory */
    @Parameter(names = { "-w", "--work"}, description = "The work directory to use", converter = FileConverter.class)
    @Getter
    private File work = null;
    /** The configuration directory */
    @Parameter(names = { "-c", "--config"}, description = "The directory that holds configuration files", converter = FileConverter.class)
    @Getter
    private File config = null;
    /** The data directory */
    @Parameter(names = { "-d", "--data"}, description = "The directory that holds data files", converter = FileConverter.class)
    @Getter
    private File data = null;
    /** The output directory */
    @Parameter(names = { "-o", "--output"}, description = "The directory that holds the output index", converter = FileConverter.class, required = true)
    @Getter
    private File output = null;
    /** The number of parallel processing threads to run */
    @Parameter(names = { "-t", "--thread"}, description = "The number of threads to use for parallel processing")
    @Getter
    private int threads = 0;
    @Parameter(names = { "-k", "--cache"}, description = "The cache size to use when loading data")
    @Getter
    private int cacheSize = 0;
    @Parameter(names = { "-m", "--metadata"}, splitter=NoSplitter.class, description = "Additional metadata settings for the final index in term=value form")
    @Getter
    private List<String> metadata = new ArrayList<>();
    @Parameter(names = { "-p", "--parameter"}, splitter=NoSplitter.class, description = "Additional parameter settings for objects such as the weight analyser or name analyser in term=value form. See individual classes for more information.")
    @Getter
    private List<String> parameters = new ArrayList<>();
    @Parameter(names = { "-u", "--url"}, splitter=NoSplitter.class, description = "Additional parameter settings for objects such as the weight analyser or name analyser in term=value form. These are URLs and can be relativised, if needed. See individual classes for more information.")
    @Getter
    private List<String> urls = new ArrayList<>();
     /** The input sources */
    @Parameter(description = "The source DwCAs", listConverter = URLConverter.class, required = true)
    @Getter
    private List<URL> sources;

    /**
     * Create a default builder configuration for this network
     *
     * @return The base builder configuration
     */
    abstract protected IndexBuilderConfiguration getDefaultIndexBuilderConfiguration();

    /**
     * Create a builder configuration for this run
     *
     * @return The builder configuration
     *
     * @throws IOException if unable to read the
     */
    protected IndexBuilderConfiguration buildIndexBuilderConfiguration() throws IOException {
        IndexBuilderConfiguration configuration = this.configUrl == null ?
                this.getDefaultIndexBuilderConfiguration() :
                IndexBuilderConfiguration.read(this.configUrl);
        if (this.work != null)
            configuration.setWork(this.work);
        if (this.config != null)
            configuration.setConfig(this.config);
        if (this.data != null)
            configuration.setData(this.data);
        if (this.threads > 0)
            configuration.setThreads(this.threads);
        if (this.cacheSize > 0)
            configuration.setCacheSize(this.cacheSize);
        for (String md: this.metadata) {
            String[] mdkv = md.split("=", 2);
            if (mdkv.length < 2)
                throw new IllegalStateException("Can't parse metadata value " + md);
            configuration.addMetadata(mdkv[0], mdkv[1]);
        }
        for (String p: this.parameters) {
            String[] pkv = p.split("=", 2);
            if (pkv.length < 2)
                throw new IllegalStateException("Can't parse parameter value " + p);
            configuration.addParameter(pkv[0], pkv[1]);
        }
        for (String u: this.urls) {
            String[] ukv = u.split("=", 2);
            if (ukv.length < 2)
                throw new IllegalStateException("Can't parse  URL parameter value " + u);
            configuration.addURL(ukv[0], new URL(ukv[1]));
        }
        AnalyserConfig analyserConfig = AnalyserConfig.builder()
                .parameters(configuration.getParameters())
                .urls(configuration.getUrls())
                .build();
        configuration.setAnalyserConfig(analyserConfig);
        return configuration;
    }

    /**
     * Validate the command line.
      *
     * @return True if validation has passed, false otherwise
     */
    public boolean validate() {
        return true;
    }

    /**
     * Build and execute a builder
     *
     * @throws Exception if something has gone wrong
     */
    public void run() throws Exception {
        IndexBuilderConfiguration configuration = this.buildIndexBuilderConfiguration();
        IndexBuilder<C, I, F, ?> builder = new IndexBuilder<>(configuration);
        for (URL input: this.sources) {
            Source source = null;
            source = Source.create(input, builder.getFactory(), builder.getNetwork().getObservables(), configuration.getTypes());
            builder.load(source);
            source.close();
        }
        LoadStore processed = builder.build();
        builder.buildIndex(this.output, processed);
        builder.close();
    }

    public static class NoSplitter implements IParameterSplitter {
        @Override
        public List<String> split(String value) {
            return Arrays.asList(value);
        }
    }

}
