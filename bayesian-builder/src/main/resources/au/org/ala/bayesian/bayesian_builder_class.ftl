<#import "derivations.ftl" as derivations>
package ${packageName};

import au.org.ala.bayesian.Analyser;
import au.org.ala.bayesian.BayesianException;
import au.org.ala.bayesian.Classifier;
import au.org.ala.bayesian.ParameterAnalyser;
import au.org.ala.bayesian.Parameters;
import au.org.ala.names.builder.BuildAnalyser;
import au.org.ala.names.builder.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

<#list imports as import>
import ${import};
</#list>

public class ${className} implements Builder<${classificationClassName}> {
  // Assumed to be stateless
  private static final Builder[] BUILDERS = new Builder[] {
<#list children as child>
    new ${className}_${child.network.signature}()<#if child_has_next>,</#if>
</#list>
  };

  private Map<String, Builder> subBuilders;

  <#list builderVariables as variable>
  private ${variable.clazz.simpleName} ${variable.name};
  </#list>

  public ${className}() {
  <#list builderVariables as variable>
    this.${variable.name} = new ${variable.clazz.simpleName}();
  </#list>
    this.subBuilders = new HashMap<>(BUILDERS.length);
    for (Builder b: BUILDERS)
      this.subBuilders.put(b.getSignature(), b);
  }

  @Override
  public String getSignature() {
    return null;
  }

  @Override
  public void generate(Classifier classifier, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
<#list derivationOrder as observable>
    <#assign derivation = observable.derivation>
    <#if derivation.generator>
        <#if derivation.conditional>
    if (${derivation.generateCondition(compiler, "classifier", factoryClassName)}) {
        </#if>
        <#if derivation.hasExtra()>
    ${derivation.extra.type.name} e_${observable?index} = ${derivation.generateExtra("classifier", factoryClassName)};
        </#if>
    if (!classifier.has(${factoryClassName}.${observable.javaVariable})) {
        <#if derivation.hasTransform()>
      ${derivation.valueClass.name} i_${observable?index} = ${derivation.generateValue("classifier", factoryClassName)};
      ${observable.type.name} v_${observable?index} = ${derivation.generateBuilderTransform("i_${observable?index}", "e_${observable?index}", "classifier")};
        <#else>
      ${observable.type.name} v_${observable?index} = ${derivation.generateValue("classifier", factoryClassName)};
        </#if>
      classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}),  false, false);
    }
    </#if>
    <#if derivation.conditional>
    }
    </#if>
</#list>
  }

  @Override
  public void interpret(Classifier classifier, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
<#list derivationOrder as observable>
    <#assign derivation = observable.derivation >
    <#if derivation.preAnalysis>
        <#if derivation.conditional>
    if (${derivation.generateCondition(compiler, "classifier", factoryClassName)}) {
        </#if>
        <#if derivation.hasExtra()>
      ${derivation.extra.type.name} e_${observable?index} = ${derivation.generateExtra("classifier", factoryClassName)};
        </#if>
      if (!classifier.has(${factoryClassName}.${observable.javaVariable})){
        ${derivation.valueClass.name} i_${observable?index} = ${derivation.generateValue("classifier", factoryClassName)};
        <#if derivation.hasTransform()>
        v_${observable?index} = ${factoryClassName}.${observable.javaVariable}.analyse(${derivation.generateBuilderTransform("v_${observable?index}", "e_${observable?index}", "classifier")}<#if derivation.multiValued>.stream().findFirst().orElse(null)</#if>;
        classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}), false, false);
        <#else>
        classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(i_${observable?index}), false, false);
        </#if>
      }
        <#if observable.multiplicity.many>
      for (${derivation.valueClass.name} i_${observable?index}: ${derivation.generateVariants("classifier", factoryClassName)}){
            <#if derivation.hasTransform()>
                <#if derivation.multiValued>
        for (${observable.type.name} v_${observable?index}: ${derivation.generateBuilderTransform("i_${observable?index}", "e_${observable?index}", "classifier")}) {
          classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}), true, false);
        }
                <#else>
        ${observable.type.name} v_${observable?index} = ${derivation.generateBuilderTransform("i_${observable?index}", "e_${observable?index}", "classifier")};
        classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}), true, false);
                </#if>
        ${observable.type.name} v_${observable?index} = ${derivation.generateBuilderTransform("i_${observable?index}", "e_${observable?index}", "classifier")};
        classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}), false, false);
            <#else>
        classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(i_${observable?index}), false, false);
            </#if>
      }
        </#if>
    </#if>
    <#if derivation.conditional>
    }
    </#if>
</#list>
  }

  @Override
  public void infer(Classifier classifier, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
<#list derivationOrder as observable>
    <#assign derivation = observable.derivation>
  <#if derivation.postAnalysis>
      <#if derivation.conditional>
    if (${derivation.generateCondition(compiler, "classifier", factoryClassName)}) {
      </#if>
      <#if derivation.hasExtra()>
      ${derivation.extra.type.name} e_${observable?index} = ${derivation.generateExtra("classifier", factoryClassName)};
      </#if>
      if (!classifier.has(${factoryClassName}.${observable.javaVariable})) {
        ${derivation.valueClass.name} i_${observable?index} = ${derivation.generateValue("classifier", factoryClassName)};
      <#if derivation.hasTransform()>
        ${observable.type.name} v_${observable?index} = ${derivation.generateBuilderTransform("i_${observable?index}", "e_${observable?index}", "classifier")}<#if derivation.multiValued>.stream().findFirst().orElse(null)</#if>;
        classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}), false, false);
      <#else>
        classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(i_${observable?index}), false, false);
      </#if>
      }
      <#if observable.multiplicity.many>
      for (${derivation.valueClass.name} i_${observable?index}: ${derivation.generateVariants("classifier", factoryClassName)}){
          <#if derivation.hasTransform()>
              <#if derivation.multiValued>
        for (${observable.type.name} v_${observable?index}: ${derivation.generateBuilderTransform("i_${observable?index}", "e_${observable?index}", "classifier")}) {
          classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}), true, false);
        }
              <#else>
        ${observable.type.name} v_${observable?index} = ${derivation.generateBuilderTransform("i_${observable?index}", "e_${observable?index}", "classifier")};
        classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}), true, false);
              </#if>
          <#else>
        classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(i_${observable?index}), true, false);
          </#if>
      }
      </#if>
    <#if derivation.conditional>
    }
    </#if>
  </#if>
</#list>
  }

  @Override
  public void expand(Classifier classifier, Deque<Classifier> parents, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
<#list baseOrder as observable>
    <#assign derivation = observable.base>
    <#assign docVar = "classifier">
    <#assign select><#if derivation.selectable>.filter(c -> ${derivation.generateSelect(compiler, "c", "classifier", factoryClassName, "parents")})</#if></#assign>
    <#if derivation.conditional>
    if (${derivation.generateCondition(compiler, "classifier", factoryClassName)}) {
    </#if>
    <#assign docVar = "d_${observable?index}">
    <#if derivation.includeSelf>
      Classifier ${docVar};
      if (${derivation.generateSelect(compiler, "classifier", "classifier", factoryClassName, "parents")})
          ${docVar} = classifier;
      else
          ${docVar} = parents.stream()${select}.findFirst().orElse(null);
    <#else>
      Optional<Classifier> ${docVar} = parents.stream()${select}.findFirst().orElse(null);
    </#if>
      if (${docVar} != null) {
    <#if derivation.hasExtra()>
        ${derivation.extra.type.name} e_${observable?index} = ${derivation.generateExtra("classifier", factoryClassName)};
    </#if>
    <#if derivation.overwrite>
        ${derivation.valueClass.name} i_${observable?index} = ${derivation.generateValue(docVar, factoryClassName)};
        if (i_${observable?index} != null) {
      <#if derivation.hasTransform()>
          ${observable.type.name} v_${observable?index} = ${derivation.generateBuilderTransform("i_${observable?index}", "e_${observable?index}", "classifier")};
          classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}), false, true);
      <#else>
          classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(i_${observable?index}), false, true);
      </#if>
        }
    <#else>
        if (!classifier.has(${factoryClassName}.${observable.javaVariable})) {
          ${derivation.valueClass.name} i_${observable?index} = ${derivation.generateValue(docVar, factoryClassName)};
      <#if derivation.hasTransform()>
          ${observable.type.name} v_${observable?index} = ${derivation.generateBuilderTransform("i_${observable?index}", "e_${observable?index}", "classifier")};
          classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}), false, false);
      <#else>
          classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(i_${observable?index}), false, false);
        }
      </#if>
    </#if>
    <#if observable.multiplicity.many>
        for(${derivation.valueClass.name} j_${observable?index}: ${derivation.generateVariants(docVar, factoryClassName)}) {
        <#if derivation.hasTransform()>
          ${observable.type.name} v_${observable?index} = ${derivation.generateBuilderTransform("j_${observable?index}", "e_${observable?index}", "classifier")};
          classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(v_${observable?index}),  true, false);
        <#else>
          classifier.add(${factoryClassName}.${observable.javaVariable}, ${factoryClassName}.${observable.javaVariable}.analyse(j_${observable?index}),  true, false);
        </#if>
        }
    </#if>
      }
    <#if derivation.conditional>
    }
    </#if>
</#list>
  }

  @Override
  public Function<Classifier, Boolean> getBroadener(Classifier document, Analyser<${classificationClassName}> analyser, BuildAnalyser<${classificationClassName}> buildAnalyser) throws BayesianException {
    List<Function<Classifier, Boolean>> broadeners = new ArrayList<>();
<#list network.broadeners as broadener>
    if (${broadener.broaden.buildClassifierCheck(compiler, "document", factoryClassName)}) {
      broadeners.add(c -> ${broadener.condition.buildClassifierCheck(compiler, "c", factoryClassName)});
    }
</#list>
    return broadeners.isEmpty() ? null : (c -> broadeners.stream().anyMatch(b -> b.apply(c)));
  }

  @Override
  public String buildSignature(Classifier classifier) {
    char[] sig = new char[${erasureStructure?size}];
<#list erasureStructure as erasure>
    sig[${erasure_index}] = (<#list erasure as observable>classifier.hasAny(${factoryClassName}.${observable.javaVariable})<#if observable_has_next> || </#if></#list>) ? 'T' : 'F';
</#list>
    return new String(sig);
  }

  @Override
  public Parameters calculate(ParameterAnalyser analyser, Classifier classifier) throws BayesianException {
    Builder sub = this.subBuilders.get(classifier.getSignature());
    if (sub == null)
        throw new IllegalArgumentException("Signature " + classifier.getSignature() + " not found");
    return sub.calculate(analyser, classifier);
  }
}
