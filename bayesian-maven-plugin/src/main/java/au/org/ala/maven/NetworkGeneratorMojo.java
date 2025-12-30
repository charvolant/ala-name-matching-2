package au.org.ala.maven;

import au.org.ala.bayesian.JavaGenerator;
import au.org.ala.bayesian.Network;
import au.org.ala.bayesian.NetworkCompiler;
import au.org.ala.names.builder.DefaultWeightAnalyser;
import au.org.ala.names.builder.NullBuildAnalyser;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal that generates java code for bayesian networks.
 */
@Mojo(name = "generate-network", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class NetworkGeneratorMojo extends AbstractMojo {
    /** Project reference */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /** The location of the network file (this is expected to be in a resources folder somewhere, so that it can be included in the final package) */
    @Parameter(property="source", required=true)
    private File source;

    /** The package to use */
    @Parameter(property="outputPackage", defaultValue = "${project.groupId}.generated")
    private String outputPackage;

    /** The directory to write to (generated java and resources subdirectories are added as required) */
    @Parameter(property="outputDirectory", defaultValue = "${project.build.directory}")
    private File outputDirectory;

    /** Any matcher class to use. If null, a parameterised matcher will be used */
    @Parameter(property="matcherClass")
    private String matcherClass;

    /** Any analyser class to use. If null, a default parameterised analyser will be used */
    @Parameter(property="analyserClass")
    private String analyserClass;

    /** Any build analyser class to use. If null, a default parameterised analyser will be used */
    @Parameter(property="buildAnalyserClass")
    private String buildAnalyserClass = NullBuildAnalyser.class.getName();

    /** Any weight analyser class to use. If null, a default parameterised analyser will be used */
    @Parameter(property="weightAnalyserClass")
    private String weightAnalyserClass = DefaultWeightAnalyser.class.getName();

    /** Generate the builder? */
    @Parameter(property="generateBuilder", defaultValue = "true")
    private boolean generateBuilder = true;

    /** A superclass for the builder */
    @Parameter(property="builderSuperClass")
    private String builderSuperClass;

    /** An implementation class for the builder */
    @Parameter(property="builderImplementationClass")
    private String builderImplementationClass;

    /** Generate the parameters? */
    @Parameter(property="generateParameters", defaultValue = "true")
    private boolean generateParameters = true;

    /** A superclass for the parameters */
    @Parameter(property="parametersSuperClass")
    private String parametersSuperClass;

    /** An implementation class for the parameters */
    @Parameter(property="parametersImplementationClass")
    private String parametersImplementationClass;

    /** Generate the inferencer? */
    @Parameter(property="generateInferencer", defaultValue = "true")
    private boolean generateInferencer = true;

    /** A superclass for the inferencer */
    @Parameter(property="inferencerSuperClass")
    private String inferencerSuperClass;

    /** An implementation class for the inferencer */
    @Parameter(property="inferencerImplementationClass")
    private String inferencerImplementationClass;

    /** Generate the classification? */
    @Parameter(property="generateClassification", defaultValue = "true")
    private boolean generateClassification = true;

    /** A superclass for the classification */
    @Parameter(property="classificationSuperClass")
    private String classificationSuperClass;

    /** An implementation class for the classification */
    @Parameter(property="classificationImplementationClass")
    private String classificationImplementationClass;

    /** Generate the factory? */
    @Parameter(property="generatefactory", defaultValue = "true")
    private boolean generateFactory = true;

    /** A superclass for the factory */
    @Parameter(property="factorySuperClass")
    private String factorySuperClass;

    /** An implementation class for the factory */
    @Parameter(property="factoryImplementationClass")
    private String factoryImplementationClass;

    /** Generate the command line interface? */
    @Parameter(property="generateCli", defaultValue = "true")
    private boolean generateCli = true;

    /** A superclass for the cli */
    @Parameter(property="cliSuperClass")
    private String cliSuperClass;

    /** An implementation class for the cli */
    @Parameter(property="cliImplementationClass")
    private String cliImplementationClass;

    /** Additional vocabulary classes to load */
    @Parameter(property="vocabularyClass")
    private List<String> vocabularyClass = new ArrayList<>();

    /**
     * Run the goal.
     * <p>
     *     Read and analyse the network. Then create appropriate files in the output directory.
     * </p>
     *
     * @see AbstractMojo#execute()
     *
     * @throws MojoExecutionException if there is an error in generation
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            if (this.project == null)
                throw new MojoExecutionException("Network code generator requires a project");
            for (String vc: this.vocabularyClass) {
                try {
                    this.getLog().info("Loading vocabulary " + vc);
                    Class.forName(vc);
                } catch (ClassNotFoundException ex) {
                    throw new MojoExecutionException("Unable to load vocabulary class " + vc, ex);
                }
            }
            File javaOutput = new File(outputDirectory, "generated-sources/java");
            File resourcesOutput = new File(outputDirectory, "generated-resources");
            this.getLog().info("Compiling " + this.source + " to " + this.outputDirectory + " under " + this.outputPackage);
            this.project.addCompileSourceRoot(javaOutput.getCanonicalPath());
            Resource resource = new Resource();
            resource.setDirectory(resourcesOutput.getCanonicalPath());
            resource.addInclude("**/*");
            resource.setFiltering(false);
            this.project.addResource(resource);
            Network network = Network.read(this.source.toURI().toURL());
            NetworkCompiler compiler = new NetworkCompiler(network, null);
            compiler.analyse();
            JavaGenerator generator = new JavaGenerator(javaOutput, resourcesOutput, this.outputPackage);
            generator.getBuilderSpec().setGenerate(this.generateBuilder);
            generator.getBuilderSpec().setSuperClassName(this.builderSuperClass);
            generator.getBuilderSpec().setImplementationClassName(this.builderImplementationClass);
            generator.getBuilderSpecificSpec().setGenerate(this.generateBuilder);
            generator.getClassificationSpec().setGenerate(this.generateClassification);
            generator.getClassificationSpec().setSuperClassName(this.classificationSuperClass);
            generator.getClassificationSpec().setImplementationClassName(this.classificationImplementationClass);
            generator.getCliSpec().setGenerate(this.generateCli);
            generator.getCliSpec().setSuperClassName(this.cliSuperClass);
            generator.getCliSpec().setImplementationClassName(this.cliImplementationClass);
            generator.getFactorySpec().setGenerate(this.generateFactory);
            generator.getFactorySpec().setSuperClassName(this.factorySuperClass);
            generator.getFactorySpec().setImplementationClassName(this.factoryImplementationClass);
            generator.getInferencerSpec().setGenerate(this.generateInferencer);
            generator.getInferencerSpec().setSuperClassName(this.inferencerSuperClass);
            generator.getInferencerSpec().setImplementationClassName(this.inferencerImplementationClass);
            generator.getInferencerSpecificSpec().setGenerate(this.generateInferencer);
            generator.getParametersSpecificSpec().setGenerate(this.generateParameters);
            generator.getParametersSpecificSpec().setSuperClassName(this.parametersSuperClass);
            generator.getParametersSpecificSpec().setImplementationClassName(this.parametersImplementationClass);
            generator.setArtifactName(this.project.getArtifactId());
            generator.getMatcherSpec().setImplementationClassName(this.matcherClass);
            generator.getAnalyserSpec().setImplementationClassName(this.analyserClass);
            generator.getBuildAnalyserSpec().setImplementationClassName(this.buildAnalyserClass);
            generator.getWeightAnalyserSpec().setImplementationClassName(this.weightAnalyserClass);
            generator.generate(compiler);
        } catch (MojoExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to create network code for " + this.source + " to " + this.outputDirectory + " under " + this.outputPackage + ": " + ex.getMessage() , ex);
        }
    }
}
