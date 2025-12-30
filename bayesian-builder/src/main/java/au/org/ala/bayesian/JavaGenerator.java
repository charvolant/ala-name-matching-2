package au.org.ala.bayesian;

import au.org.ala.bayesian.derivation.CompiledDerivation;
import au.org.ala.names.builder.*;
import au.org.ala.util.IdentifierConverter;
import au.org.ala.util.SimpleIdentifierConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.CollectionModel;
import freemarker.ext.beans.StringModel;
import freemarker.template.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.gbif.dwc.terms.TermFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Compile a network into java code.
 */
@Slf4j
public class JavaGenerator extends Generator {
    private static final Logger logger = LoggerFactory.getLogger(JavaGenerator.class);

    /** The default package to generate code for */
    private static final String DEFAULT_PACKAGE = "au.org.ala.bayesian.generated";
    /** The encoding to use for code generation */
    private static final String ENCODING = "UTF-8";
    /** The name of the inference class template */
    private static final String INFERENCE_CLASS_TEMPLATE = "bayesian_inference_class.ftl";
    /** The name of the inference erasure-specific class template */
    private static final String INFERENCE_SPECIFIC_CLASS_TEMPLATE = "bayesian_inference_specific_class.ftl";
    /** The name of the parameters class template */
    private static final String PARAMETERS_CLASS_TEMPLATE = "bayesian_parameter_class.ftl";
    /** The name of the parameters erasure-specific class template */
    private static final String PARAMETERS_SPECIFIC_CLASS_TEMPLATE = "bayesian_parameter_specific_class.ftl";
    /** The name of the classification class template */
    private static final String CLASSIFICATION_CLASS_TEMPLATE = "bayesian_classification_class.ftl";
    /** The name of the builder class template */
    private static final String BUILDER_CLASS_TEMPLATE = "bayesian_builder_class.ftl";
    /** The name of the inference erasure-specific class template */
    private static final String BUILDER_SPECIFIC_CLASS_TEMPLATE = "bayesian_builder_specific_class.ftl";
    /** The name of the observables class template */
    private static final String FACTORY_CLASS_TEMPLATE = "bayesian_factory_class.ftl";
    /** The name of the CLI class template */
    private static final String CLI_CLASS_TEMPLATE = "builder_cli_class.ftl";

    /** The configuration to use */
    private static Configuration CONFIG =null;

    /** The java code target directory */
    private final File javaTargetDir;
    /** The resources target directory */
    private final File resourceTargetDir;
    /** The package to use */
    @Getter
    @Setter
    private String packageName;
    /** The artifact name to use */
    @Getter
    @Setter
    private String artifactName;
    /** The freemarker configuration */
    private final Configuration freemarkerConfig;
    /** The object wrapper to use */
    private final BeansWrapper wrapper;
    /** The identifier converter for creating class names */
    private final IdentifierConverter classConverter;
    /** Builder class specification */
    @Getter
    private final JavaGeneratorSpecification<Builder> builderSpec;
    /** Builder specific class specification */
    @Getter
    private final JavaGeneratorSpecification<Builder> builderSpecificSpec;
    /** Parameter class specification */
    @Getter
    private final JavaGeneratorSpecification<Parameters> parametersSpec;
    /** Parameter specific class specification */
    @Getter
    private final JavaGeneratorSpecification<Parameters> parametersSpecificSpec;
    /** Classification class specification */
    @Getter
    private final JavaGeneratorSpecification<Classification> classificationSpec;
    /** Inferencer class specification */
    @Getter
    private final JavaGeneratorSpecification<Inferencer> inferencerSpec;
    /** Inferencer specific class specification */
    @Getter
    private final JavaGeneratorSpecification<Inferencer> inferencerSpecificSpec;
    /** Analysis class specification */
    @Getter
    private final JavaGeneratorSpecification<Analyser> analyserSpec;
    /** Build analysis class specification */
    @Getter
    private final JavaGeneratorSpecification<BuildAnalyser> buildAnalyserSpec;
    /** Weight analysis class specification */
    @Getter
    private final JavaGeneratorSpecification<WeightAnalyser> weightAnalyserSpec;
    /** Generator class specification */
    @Getter
    private final JavaGeneratorSpecification<ClassificationMatcher> matcherSpec;
    /** Parameter class specification */
    @Getter
    private final JavaGeneratorSpecification<NetworkFactory> factorySpec;
    /** Parameter class specification */
    @Getter
    private final JavaGeneratorSpecification<Cli> cliSpec;

    /**
     * Construct a generator
     *
     * @param javaTargetDir The target directory in which java code is generated
     * @param resourceTargetDir The target directory in which resources are generated
     * @param packageName The package to generate into
     *
     * @throws IOException if unable to load the templates
     */
    public JavaGenerator(File javaTargetDir, File resourceTargetDir, String packageName) throws IOException {
        this.javaTargetDir = javaTargetDir;
        this.resourceTargetDir = resourceTargetDir;
        this.packageName = packageName;
        this.artifactName = "name-matching-builder";
        this.freemarkerConfig = getConfig();
        this.classConverter = SimpleIdentifierConverter.JAVA_CLASS;
        this.wrapper = new DefaultObjectWrapperBuilder(this.freemarkerConfig.getIncompatibleImprovements()).build();
        this.parametersSpec = new JavaGeneratorSpecification<>("Parameters", PARAMETERS_CLASS_TEMPLATE, Parameters.class, false);
        this.parametersSpecificSpec = new JavaGeneratorSpecification<>("Parameters", PARAMETERS_SPECIFIC_CLASS_TEMPLATE, Parameters.class, true);
        this.classificationSpec = new JavaGeneratorSpecification<>("Classification", CLASSIFICATION_CLASS_TEMPLATE, Classification.class, false);
        this.analyserSpec = new JavaGeneratorSpecification<>("Analyser", null, Analyser.class, false, this.classificationSpec);
        this.buildAnalyserSpec = new JavaGeneratorSpecification<>("BuildAnalyser", null, BuildAnalyser.class, false, this.classificationSpec);
        this.buildAnalyserSpec.setImplementationClassName(NullBuildAnalyser.class.getName()); // Default
        this.weightAnalyserSpec = new JavaGeneratorSpecification<>("Weight", null, WeightAnalyser.class, false);
        this.weightAnalyserSpec.setImplementationClassName(DefaultWeightAnalyser.class.getName());
        this.inferencerSpec = new JavaGeneratorSpecification<>("Inferencer", INFERENCE_CLASS_TEMPLATE, Inferencer.class, false, this.classificationSpec, this.parametersSpec, this.analyserSpec);
        this.inferencerSpecificSpec = new JavaGeneratorSpecification<>("Inferencer", INFERENCE_SPECIFIC_CLASS_TEMPLATE, Inferencer.class, true, this.classificationSpec, this.parametersSpecificSpec, this.analyserSpec);
        this.factorySpec = new JavaGeneratorSpecification<>("Factory", FACTORY_CLASS_TEMPLATE, NetworkFactory.class, false, this.classificationSpec, this.parametersSpec, this.inferencerSpec, this.analyserSpec);
        this.builderSpec = new JavaGeneratorSpecification<>("Builder", BUILDER_CLASS_TEMPLATE, Builder.class, false, this.parametersSpec, this.factorySpec, this.classificationSpec);
        this.builderSpecificSpec = new JavaGeneratorSpecification<>("Builder", BUILDER_SPECIFIC_CLASS_TEMPLATE, Builder.class, true, this.parametersSpecificSpec, this.factorySpec, this.classificationSpec);
        this.cliSpec = new JavaGeneratorSpecification<>("Cli", CLI_CLASS_TEMPLATE, Cli.class, false, this.classificationSpec, this.parametersSpec, this.builderSpec, this.inferencerSpec, this.factorySpec, this.weightAnalyserSpec, this.buildAnalyserSpec);
        this.matcherSpec = new JavaGeneratorSpecification<>("matcher", null, ClassificationMatcher.class, false, this.classificationSpec, this.parametersSpec, this.inferencerSpec, this.factorySpec);
        this.factorySpec.addParameter(this.matcherSpec);
        this.classificationSpec.addParameter(this.factorySpec);
        this.classificationSpec.addParameter(this.inferencerSpec);
        this.classificationSpec.addParameter(this.analyserSpec);
    }

    /**
     * Construct a default generator
     *
     * @throws IOException if unable to load the templates
     */
    public JavaGenerator() throws IOException {
        this(new File("."), new File("."), DEFAULT_PACKAGE);
    }

    /**
     * Remove the package name from a class to make a simple name
     *
     * @param className The class name
     *
     * @return The class name without package
     */
    private String stripPackage(String className) {
        return className.replaceFirst("^([a-z$_][a-zA-Z0-9$_]*\\.)+", "");
    }

    /**
     * Generate all results requested.
     * <p>
     * By default, everything is generated.
     * You can set the individual specifications to flag whether they should be generated or not
     * </p>
     *
     * @param compiler The compuler to generate from
     *
     * @throws Exception if unable to generate code
     */
    @Override
    public void generate(NetworkCompiler compiler) throws Exception {
        String classBase = this.classConverter.convert(compiler.getNetwork());
        File javaTarget = this.javaTargetDir;
        File resourceTarget = this.resourceTargetDir;
        Writer writer;

        for (String p: this.packageName.split("\\.")) {
            javaTarget = new File(javaTarget, p);
            resourceTarget = new File(resourceTarget, p);
        }
        javaTarget.mkdirs();
        if (!javaTarget.exists())
            throw new IOException("Unable to create " + javaTarget);
        resourceTarget.mkdirs();
        if (!resourceTarget.exists())
            throw new IOException("Unable to create " + resourceTarget);

        log.info("Generating for network " + compiler.getNetwork());
        this.generateClass(compiler, this.classificationSpec, javaTarget, compiler.getClassificationVariables());
        this.generateClass(compiler, this.inferencerSpec, javaTarget, null);
        // this.generateClass(compiler, this.parametersSpec, javaTarget, null);
        this.generateClass(compiler, this.factorySpec, javaTarget, null);
        this.generateClass(compiler, this.builderSpec, javaTarget, compiler.getBuilderVariables());
        this.generateClass(compiler, this.cliSpec, javaTarget, null);
        if (this.cliSpec.isGenerate()) {
            writer = new FileWriter(new File(resourceTarget, this.getNetworkFileName(compiler)));
            this.generateNetworkFile(compiler, writer);
            writer.close();
        }
        for (NetworkCompiler child: compiler.getChildren()) {
            this.generateClass(child, this.parametersSpecificSpec, javaTarget, null);
            this.generateClass(child, this.inferencerSpecificSpec, javaTarget, null);
            this.generateClass(child, this.builderSpecificSpec, javaTarget, null);
        }
     }

    /**
     * Populate the template environnment with common values.
     *
     * @param environment The environmwent to populate
     * @param spec The base specification
     */
    protected void populate(Environment environment, NetworkCompiler compiler, JavaGeneratorSpecification spec, Collection<CompiledDerivation.Variable> variables) {
        Set<String> imports = new HashSet<>();
        JavaGeneratorSpecification.Context baseContext = spec.withContext(compiler, this.packageName);
        if (variables != null) {
            for (CompiledDerivation.Variable v: variables) {
                baseContext.addImport(v.getImport(), imports);
            }
        }
        for (Observable observable: compiler.getNetwork().getObservables()) {
            baseContext.addImport(observable.getType(), imports);
            baseContext.addImport(observable.getAnalysis().getClass(), imports);
        }
        environment.setVariable("compiler", new BeanModel(compiler, this.wrapper));
        environment.setVariable("packageName", new StringModel(this.packageName, this.wrapper));
        environment.setVariable("artifactName", new StringModel(this.artifactName, this.wrapper));
        environment.setVariable("networkFileName", new StringModel(this.getNetworkFileName(compiler), this.wrapper));
        environment.setVariable("externalContexts", new CollectionModel(Arrays.asList(ExternalContext.LUCENE), this.wrapper));
        environment.setVariable("variantExternalContexts", new CollectionModel(Arrays.asList(ExternalContext.LUCENE_VARIANT), this.wrapper));
        environment.setVariable("termFactory", new BeanModel(TermFactory.instance(), this.wrapper));
        baseContext.populate(environment, this.wrapper, imports);
        environment.setVariable("imports", new CollectionModel(imports, this.wrapper));
    }

    protected String getNetworkFileName(NetworkCompiler compiler) {
        return SimpleIdentifierConverter.FILE_NAME.convert(compiler.getNetwork()) + ".json";
    }

    protected <I> void generateClass(@NonNull NetworkCompiler compiler, @NonNull JavaGeneratorSpecification<I> spec, @NonNull Writer writer, Collection<CompiledDerivation.Variable> variables) throws IOException, TemplateException {
       if (spec.getTemplate() == null || !spec.isGenerate())
            return;
        Template template = this.freemarkerConfig.getTemplate(spec.getTemplate());
        Environment environment = template.createProcessingEnvironment(compiler, writer);
        this.populate(environment, compiler, spec, variables);
        log.info("Generating " + compiler.getNetwork().getId() + "/" + spec.getFunction() + " class " + environment.getVariable("className"));
        environment.process();
    }

    protected <I> void generateClass(@NonNull NetworkCompiler compiler, @NonNull JavaGeneratorSpecification<I> spec, File javaTarget, Collection<CompiledDerivation.Variable> variables) throws IOException, TemplateException {
        if (spec.getTemplate() == null || !spec.isGenerate())
            return;
        File target = new File(javaTarget, spec.withContext(compiler, this.packageName).getClassName() + ".java");
        log.info("Generating " + compiler.getNetwork().getId() + "/" + spec.getFunction()  + "/" + compiler.getNetwork().getSignature() + " to file " + target);
        Writer writer = new FileWriter(target);
        this.generateClass(compiler, spec, writer, variables);
        writer.close();
    }

    /**
     * Generate a readable version of the network for the builder class
     *
     * @param compiler The compiler/analyser
     * @param writer The writer to write to
     *
     * @throws IOException if unable to write the network
     */
    protected void generateNetworkFile(@NonNull NetworkCompiler compiler, Writer writer) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.writeValue(writer, compiler.getNetwork());
    }

    /**
     * Get the freemarker configuration, lazily generated.
     *
     * @return The freemarker configuration
     */
    synchronized public static Configuration getConfig() {
        if (CONFIG == null) {
            CONFIG = new Configuration(Configuration.VERSION_2_3_29);
            CONFIG.setClassForTemplateLoading(JavaGenerator.class, "");
            CONFIG.setDefaultEncoding(ENCODING);
            CONFIG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }
        return CONFIG;
    }

    /**
     * Run the java generator
     *
     * @param args Command line arguments
     *
     * @throws Exception if unable to complete
     */
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        JCommander commander = JCommander.newBuilder().addObject(options).args(args).build();

        log.info("Generating for package " + options.pkg + " and output " + options.output.getAbsolutePath());
        if (!options.pkg.matches("[a-z]+(\\.[a-z]+)*"))
            throw new IllegalArgumentException("Invalid package name " + options.pkg);
        if (!options.output.exists())
            throw new IllegalArgumentException("Output directory " + options.output + " does not exist");
        JavaGenerator generator = new JavaGenerator(options.output, options.output, options.pkg);
        for (File source: options.sources) {
             log.info("Generating " + source.getAbsolutePath());
            if (!source.exists())
                throw new IllegalArgumentException("Base network " + source + " does not exist");
            Network network = Network.read(source.toURI().toURL());
            NetworkCompiler compiler = new NetworkCompiler(network, null);
            compiler.analyse();
            generator.generate(compiler);
        }
    }

    public static class Options {
        @Parameter(names = "-o")
        public File output = new File(System.getProperty("user.dir"));
        @Parameter(names = "-p")
        public String pkg = "au.org.ala.names.generated";
        @Parameter(required = true)
        public List<File> sources;
    }
}
