package ${packageName};

import java.util.Arrays;
import au.org.ala.names.builder.AbstractCli;
import au.org.ala.names.builder.IndexBuilderConfiguration;
import com.beust.jcommander.JCommander;

<#list builderVariables as variable>
import ${variable.import};
</#list>

<#if analyserClass??>
import ${analyserClass};
</#if>

public class ${className} extends AbstractCli<${classificationClassName}, ${builderClassName}, ${inferencerClassName}, ${factoryClassName}> {

  protected IndexBuilderConfiguration getDefaultIndexBuilderConfiguration() {
    IndexBuilderConfiguration configuration = new IndexBuilderConfiguration();
    configuration.setBuilderClass(${builderClassName}.class);
    configuration.setFactoryClass(${factoryClassName}.class);
    configuration.setBuildAnalyserClass(${buildanalyserImplementationClassName}.class);
    configuration.setWeightAnalyserClass(${weightImplementationClassName}.class);
    configuration.setNetwork(${builderClassName}.class.getResource("${networkFileName}"));
    configuration.setTypes(Arrays.asList(${factoryClassName}.CONCEPT));
    return configuration;
  }

  public static void main(String[] args) throws Exception {
     ${className} cli = new ${className}();
     JCommander commander = JCommander.newBuilder().addObject(cli).args(args).build();
     if (!cli.validate() || cli.isHelp()) {
       commander.usage();
       return;
     }
     cli.run();
   }
}
