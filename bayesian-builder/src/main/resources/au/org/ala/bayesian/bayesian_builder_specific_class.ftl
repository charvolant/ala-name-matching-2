<#import "derivations.ftl" as derivations>
package ${packageName};

import au.org.ala.bayesian.Analyser;
import au.org.ala.bayesian.BayesianException;
import au.org.ala.bayesian.Classifier;
import au.org.ala.bayesian.ParameterAnalyser;
import au.org.ala.bayesian.Parameters;
import au.org.ala.names.builder.BuildAnalyser;
import au.org.ala.names.builder.Builder;

import java.util.Arrays;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Function;

<#list imports as import>
import ${import};
</#list>

public class ${className} implements Builder<${classificationClassName}> {
  public final static String SIGNATURE = "${network.signature}";

  public  ${className}() {
  }

  @Override
  public String getSignature() {
    return SIGNATURE;
  }

  @Override
  public void generate(Classifier classifier, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public void interpret(Classifier classifier, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public void infer(Classifier classifier, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public void expand(Classifier classifier, Deque<Classifier> parents, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public Function<Classifier, Boolean> getBroadener(Classifier document, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public String buildSignature(Classifier classifier) {
    return SIGNATURE;
  }

  @Override
  public Parameters calculate(ParameterAnalyser analyser, Classifier classifier) throws BayesianException {
    ${parametersClassName} parameters = new ${parametersClassName}();
<#list inputs as inc>
    parameters.${inc.prior.id} = analyser.computePrior(analyser.getObservation(true, ${factoryClassName}.${inc.observable.javaVariable}, classifier));
</#list>
<#list orderedNodes as node>
  <#list node.inference as inf>
      <#if !inf.derived>
    parameters.${inf.id} = analyser.computeConditional(analyser.getObservation(true, ${factoryClassName}.${inf.outcome.observable.javaVariable}, classifier) <#list inf.contributors as c>, analyser.getObservation(${c.match?c}, ${factoryClassName}.${c.observable.javaVariable}, classifier)</#list>);
      </#if>
  </#list>
</#list>
    return parameters;
  }
}
