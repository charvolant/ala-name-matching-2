package au.org.ala.names.builder;

import au.org.ala.bayesian.*;
import au.org.ala.names.lucene.LuceneLoadStore;
import au.org.ala.util.JsonUtils;
import au.org.ala.util.Metadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.Getter;
import lombok.Setter;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;

import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IndexBuilderConfiguration {
    /** The working area directory to use, null for the general temparay directory */
    @JsonProperty
    @Getter
    @Setter
    private File work;
    /** The configuration directory to use, for classes that load an external configuration */
    @JsonProperty
    @Getter
    @Setter
    private File config;
    /** The data directory to use, for classes that load external data */
    @JsonProperty
    @Getter
    @Setter
    private File data;
    /** The network description file */
    @JsonProperty("network")
    @Getter
    @Setter
    private URL network;
    /** The class of the network factory */
    @JsonProperty
    @Getter
    @Setter
    private Class<? extends NetworkFactory<?, ?, ?>> factoryClass;
    /** The class of the builder */
    @JsonProperty
    @Getter
    @Setter
    private Class<? extends Builder> builderClass;
    @JsonProperty
    @Getter
    @Setter
    private Class<? extends BuildAnalyser> buildAnalyserClass;
    /** The class of the load store */
    @JsonProperty
    @Getter
    @Setter
    private Class<? extends LoadStore<?>> loadStoreClass;
    /** The class of the weight analyser */
    @JsonProperty
    @Getter
    @Setter
    private Class<? extends WeightAnalyser> weightAnalyserClass;
    /** The analyzer configuration */
    @JsonProperty
    @Getter
    @Setter
    private AnalyserConfig analyserConfig;
    /** The default weight to use for an unweighted taxon */
    @JsonProperty
    @Getter
    @Setter
    private double defaultWeight;
    /** The log interval */
    @JsonProperty
    @Getter
    @Setter
    private int logInterval;
    /** The types of records to load and index */
    @JsonProperty
    @Getter
    @Setter
    private List<Term> types;
    /** The number of worker threads */
    @JsonProperty
    @Getter
    @Setter
    private int threads;
    /**
     * The size of any internal cache (0 for default)
     */
    @JsonProperty
    @Getter
    @Setter
    private int cacheSize;
    /** Use JMX instrumentation (true by default) */
    @JsonProperty
    @Getter
    @Setter
    private boolean enableJmx;
    /** The metadata template with default values */
    @JsonProperty
    @Getter
    private Metadata metadataTemplate;
    /** Additional parameters for initialising various elements */
    @JsonProperty
    @Getter
    private Map<String, String> parameters;
    /** Additional URL parameters for initialising various elements */
    @JsonProperty
    @Getter
    private Map<String, URL> urls;
    /** Additional resource URLs for initialising various elements */
    @JsonProperty
    @Getter
    private Map<String, URL> resources;

    public IndexBuilderConfiguration() {
        this.builderClass = EmptyBuilder.class;
        this.loadStoreClass = LuceneLoadStore.class;
        this.weightAnalyserClass = DefaultWeightAnalyser.class;
        this.analyserConfig = AnalyserConfig.builder().build();
        this.defaultWeight = 1.0;
        this.logInterval = 10000;
        this.types = Arrays.asList(DwcTerm.Taxon);
        this.threads = Runtime.getRuntime().availableProcessors();
        this.cacheSize = 0;
        this.enableJmx = true;
        this.metadataTemplate = Metadata.builder().build();
        this.parameters = new HashMap<>();
        this.urls = new HashMap<>();
        this.resources = new HashMap<>();
    }

    /**
     * Add a metadata term to the template.
     *
     * @param term The field name
     * @param value The value as a string
     *
     * @see Metadata#with(String, Object)
     */
    public void addMetadata(String term, String value) {
        this.metadataTemplate = metadataTemplate.with(term, value);
    }

    /**
     * Add a parameter to the template.
     *
     * @param term The field name
     * @param value The value as a string
     *
     * @see Metadata#with(String, Object)
     */
    public void addParameter(String term, String value) {
        this.parameters.put(term, value);
    }

    /**
     * Add a URL to the template.
     *
     * @param term The field name
     * @param value The value as a string
     *
     * @see Metadata#with(String, Object)
     */
    public void addURL(String term, URL value) {
        this.urls.put(term, value);
    }

    /**
     * Constuct a load-store.
     * <p>
     * The load store must have either: A constuctor for an annotator and index builder condfiguration,
     * a constructor for an annotator and file (working directory) or an annotator only constuctor.
     * These are tried in turn.
     * </p>
     *
     * @param name The name of the store
     * @param working This is a temporary working store
     *
     * @return A newly created load-store.
     *
     * @throws StoreException if unable to build the store
     */
    public <Cl extends Classifier> LoadStore<Cl> createLoadStore(String name, boolean working) throws StoreException {
        Constructor<? extends LoadStore<Cl>> c;

        if (this.loadStoreClass == null)
            throw new StoreException("Load store class not defined");
        try {
            c = (Constructor<? extends LoadStore<Cl>>) this.loadStoreClass.getConstructor(String.class, IndexBuilderConfiguration.class, boolean.class, boolean.class, int.class);
            return c.newInstance(name, this, working, true, this.cacheSize);
        } catch (InvocationTargetException ex) {
            throw new StoreException("Unable to construct load store for " + this.loadStoreClass, ex.getCause());
        } catch (Exception ex) {
        }
        try {
            c = (Constructor<? extends LoadStore<Cl>>) this.loadStoreClass.getConstructor(String.class, IndexBuilderConfiguration.class, boolean.class, int.class);
            return c.newInstance(name, this, working, this.cacheSize);
        } catch (InvocationTargetException ex) {
            throw new StoreException("Unable to construct load store for " + this.loadStoreClass, ex.getCause());
        } catch (Exception ex) {
        }
        try {
            c = (Constructor<? extends LoadStore<Cl>>) this.loadStoreClass.getConstructor(String.class, File.class, boolean.class, boolean.class, int.class);
            return c.newInstance(name, this.getWork(), working, true, this.cacheSize);
        } catch (InvocationTargetException ex) {
            throw new StoreException("Unable to construct load store for " + this.loadStoreClass, ex.getCause());
        } catch (Exception ex) {
        }
        try {
            c = (Constructor<? extends LoadStore<Cl>>) this.loadStoreClass.getConstructor(String.class, File.class, boolean.class, int.class);
            return c.newInstance(name, this.getWork(), working, this.cacheSize);
        } catch (InvocationTargetException ex) {
            throw new StoreException("Unable to construct load store for " + this.loadStoreClass, ex.getCause());
        } catch (Exception ex) {
        }
        try {
            c = (Constructor<? extends LoadStore<Cl>>) this.loadStoreClass.getConstructor(String.class, int.class);
            return c.newInstance(name, this.cacheSize);
        } catch (InvocationTargetException ex) {
            throw new StoreException("Unable to construct load store for " + this.loadStoreClass, ex.getCause());
        } catch (Exception ex) {
        }
        throw new StoreException("Unable to construct load store for " + this.loadStoreClass);
    }

    /**
     * Construct a factory.
     * <p>
     * The factory must have either:
     * A static instance() method for the factory,
     * A constructor for an index builder configuration,
     * or an empty constuctor.
     * These are tried in turn.
     * </p>
     *
     * @param annotator The annotator to use
     *
     * @return A newly created builder.
     *
     * @throws StoreException if unable to build the store
     */
    public <C extends Classification<C>, I extends Inferencer<C>, F extends NetworkFactory<C, I, F>> F createFactory(Annotator annotator) throws StoreException {
        Constructor<F> c;

        if (this.factoryClass == null)
            throw new StoreException("Factory class not defined");
        try {
            Method m = this.factoryClass.getMethod("instance");
            if (m != null && Modifier.isStatic(m.getModifiers()))
                return (F) m.invoke(null);
        } catch (Exception ex) {
        }
        try {
            c = (Constructor<F>) this.factoryClass.getConstructor(IndexBuilderConfiguration.class);
            return c.newInstance(this);
        } catch (Exception ex) {
        }
        try {
            c = (Constructor<F>) this.factoryClass.getConstructor();
            return c.newInstance();
        } catch (Exception ex) {
        }
        throw new StoreException("Unable to construct factory for " + this.factoryClass);
    }

    /**
     * Construct a builder.
     * <p>
     * The builder must have either: A constructor for an index builder configuration and a factory,
     * a constructor for a index builder configuration,
     * a constructor for a factory,
     * or an empty constuctor.
     * These are tried in turn.
     * </p>
     *
     * @param factory The factory to use
     *
     * @return A newly created builder.
     *
     * @throws StoreException if unable to build the store
     */
    public <F extends NetworkFactory<?, ?, F>> Builder createBuilder(F factory) throws StoreException {
        Constructor<? extends Builder> c;

        if (this.builderClass == null)
            throw new StoreException("Builder class not defined");
        try {
            c = this.builderClass.getConstructor(IndexBuilderConfiguration.class, NetworkFactory.class);
            return c.newInstance(this, factory);
        } catch (Exception ex) {
        }
        try {
            c = this.builderClass.getConstructor(IndexBuilderConfiguration.class);
            return c.newInstance(this);
        } catch (Exception ex) {
        }
        try {
            c = this.builderClass.getConstructor(NetworkFactory.class);
            return c.newInstance(factory);
        } catch (Exception ex) {
        }
        try {
            c = this.builderClass.getConstructor();
            return c.newInstance();
        } catch (Exception ex) {
        }
        throw new StoreException("Unable to construct builder for " + this.builderClass);
    }

    /**
     * Construct a build analyser.
     * <p>
     * The build analyser must have either:
     * A static instance() method for the factory,
     * A constructor for an index builder configuration,
     * or an empty constuctor.
     * These are tried in turn.
     * </p>
     *
     * @param annotator The annotator to use
     *
     * @return A newly created builder.
     *
     * @throws StoreException if unable to build the store
     */
    public <C extends Classification<C>> BuildAnalyser<C> createBuildAnalyser(Annotator annotator) throws StoreException {
        Constructor<BuildAnalyser<C>> c;

        if (this.buildAnalyserClass == null)
            throw new StoreException("Build analyser class not defined");
        try {
            Method m = this.buildAnalyserClass.getMethod("instance");
            if (m != null && Modifier.isStatic(m.getModifiers()))
                return (BuildAnalyser<C>) m.invoke(null);
        } catch (Exception ex) {
        }
        try {
            c = (Constructor<BuildAnalyser<C>>) this.buildAnalyserClass.getConstructor(AnalyserConfig.class);
            return c.newInstance(this.getAnalyserConfig());
        } catch (Exception ex) {
        }
        try {
            c = (Constructor<BuildAnalyser<C>>) this.buildAnalyserClass.getConstructor();
            return c.newInstance();
        } catch (Exception ex) {
        }
        throw new StoreException("Unable to construct build analyser for " + this.buildAnalyserClass);
    }

    /**
     * Constuct a weight analysers.
     * <p>
     * The weight analyser must have either: A constuctor for an network and index builder condfiguration,
     * a constructor for an index builder configuration, a constructor for a network or a default constuctor.
     * These are tried in turn.
     * </p>
     *
     * @param network The network to analyse
     *
     * @return A newly created weight analyser.
     *
     * @throws StoreException if unable to build the store
     */
    public WeightAnalyser createWeightAnalyser(Network network) throws StoreException {
        Constructor<? extends WeightAnalyser> c;

        if (this.weightAnalyserClass == null)
            throw new StoreException("Weight analyser class not defined");
        try {
            c = this.weightAnalyserClass.getConstructor(Network.class, IndexBuilderConfiguration.class);
            return c.newInstance(network, this);
        } catch (InvocationTargetException ex) {
            throw new StoreException("Unable to construct weight analyser for " + this.weightAnalyserClass, ex.getCause());
        } catch (Exception ex) {
        }
        try {
            c = this.weightAnalyserClass.getConstructor(IndexBuilderConfiguration.class);
            return c.newInstance(this);
        } catch (InvocationTargetException ex) {
            throw new StoreException("Unable to construct weight analyser for " + this.weightAnalyserClass, ex.getCause());
        } catch (Exception ex) {
        }
        try {
            c = this.weightAnalyserClass.getConstructor(Network.class);
            return c.newInstance(network);
        } catch (InvocationTargetException ex) {
            throw new StoreException("Unable to construct weight analyser for " + this.weightAnalyserClass, ex.getCause());
        } catch (Exception ex) {
        }
        try {
            c = this.weightAnalyserClass.getConstructor();
            return c.newInstance();
        } catch (InvocationTargetException ex) {
            throw new StoreException("Unable to construct weight analyser for " + this.weightAnalyserClass, ex.getCause());
        } catch (Exception ex) {
        }
        throw new StoreException("Unable to construct weight analyser for " + this.weightAnalyserClass);
    }

    /**
     * Write the configuration as JSON.
     *
     * @param writer The writer to write to
     *
     * @throws IOException if unable to write
     */
    public void write(Writer writer) throws IOException {
        JsonUtils.createMapper().writeValue(writer, this);
    }
    /**
     * Read a index builder config from a URL.
     *
     * @param source The source URL
     *
     * @return The resulting configuration
     *
     * @throws IOException If unable to read the configuration
     */
    public static IndexBuilderConfiguration read(URL source) throws IOException {
        return JsonUtils.createMapper().readValue(source, IndexBuilderConfiguration.class);
    }

    /**
     * Open a CSV reader for a configuration file.
     * <p>
     * If a file of that name exists in the config directory, use that.
     * Otherwise, get the file as a resource.
     * The CSV file is expected to have a single row header with column names
     * and have a default format (commas, double quotes, escape characters).
     * </p>
     * @param file The file name
     * @param clazz The class making the request
     *
     * @return A CSV reader for the file
     */
    public CSVReader openConfigCsv(String file, Class<?> clazz) throws IOException {
        Reader r = null;
        if (this.getConfig() != null) {
            File f = new File(this.getConfig(), file);
            if (f.exists())
                r = new FileReader(f);
        }
        if (r == null)
            r = new InputStreamReader(clazz.getResourceAsStream(file));
        CSVReader reader = new CSVReaderBuilder(r).withSkipLines(1).build();
        return reader;
    }


    /**
     * Open a CSV reader for a data file.
     * <p>
     * If a file of that name exists in the config directory, use that.
     * The CSV file is expected to have a single row header with column names
     * and have a default format (commas, double quotes, escape characters).
     * </p>
     * @param file The file name
     *
     * @return A CSV reader for the file or null for not found
     */
    public CSVReader openDataCsv(String file) throws IOException {
        CSVReader reader = null;
        Reader r = null;
        if (this.getData() != null) {
            File f = new File(this.getData(), file);
            if (f.exists())
                r = new FileReader(f);
        }
        if (r != null) {
            reader = new CSVReaderBuilder(r).withSkipLines(1).build();
        }
        return reader;
    }

}

