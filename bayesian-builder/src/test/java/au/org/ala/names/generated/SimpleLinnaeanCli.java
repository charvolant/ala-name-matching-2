package au.org.ala.names.generated;

import java.util.Arrays;
import au.org.ala.names.builder.AbstractCli;
import au.org.ala.names.builder.IndexBuilderConfiguration;
import com.beust.jcommander.JCommander;

import au.org.ala.bayesian.derivation.SoundexGenerator;
import au.org.ala.bayesian.derivation.PrefixDerivation.PrefixGenerator;


public class SimpleLinnaeanCli extends AbstractCli<SimpleLinnaeanClassification, SimpleLinnaeanBuilder, SimpleLinnaeanInferencer, SimpleLinnaeanFactory> {

  protected IndexBuilderConfiguration getDefaultIndexBuilderConfiguration() {
    IndexBuilderConfiguration configuration = new IndexBuilderConfiguration();
    configuration.setBuilderClass(SimpleLinnaeanBuilder.class);
    configuration.setFactoryClass(SimpleLinnaeanFactory.class);
    configuration.setBuildAnalyserClass(au.org.ala.names.builder.NullBuildAnalyser.class);
    configuration.setWeightAnalyserClass(au.org.ala.names.builder.DefaultWeightAnalyser.class);
    configuration.setNetwork(SimpleLinnaeanBuilder.class.getResource("simple-linnaean.json"));
    configuration.setTypes(Arrays.asList(SimpleLinnaeanFactory.CONCEPT));
    return configuration;
  }

  public static void main(String[] args) throws Exception {
     SimpleLinnaeanCli cli = new SimpleLinnaeanCli();
     JCommander commander = JCommander.newBuilder().addObject(cli).args(args).build();
     if (!cli.validate() || cli.isHelp()) {
       commander.usage();
       return;
     }
     cli.run();
   }
}
