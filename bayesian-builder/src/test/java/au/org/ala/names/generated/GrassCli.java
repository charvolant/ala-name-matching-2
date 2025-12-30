package au.org.ala.names.generated;

import java.util.Arrays;
import au.org.ala.names.builder.AbstractCli;
import au.org.ala.names.builder.IndexBuilderConfiguration;
import com.beust.jcommander.JCommander;



public class GrassCli extends AbstractCli<GrassClassification, GrassBuilder, GrassInferencer, GrassFactory> {

  protected IndexBuilderConfiguration getDefaultIndexBuilderConfiguration() {
    IndexBuilderConfiguration configuration = new IndexBuilderConfiguration();
    configuration.setBuilderClass(GrassBuilder.class);
    configuration.setFactoryClass(GrassFactory.class);
    configuration.setBuildAnalyserClass(au.org.ala.names.builder.NullBuildAnalyser.class);
    configuration.setWeightAnalyserClass(au.org.ala.names.builder.DefaultWeightAnalyser.class);
    configuration.setNetwork(GrassBuilder.class.getResource("grass.json"));
    configuration.setTypes(Arrays.asList(GrassFactory.CONCEPT));
    return configuration;
  }

  public static void main(String[] args) throws Exception {
     GrassCli cli = new GrassCli();
     JCommander commander = JCommander.newBuilder().addObject(cli).args(args).build();
     if (!cli.validate() || cli.isHelp()) {
       commander.usage();
       return;
     }
     cli.run();
   }
}
