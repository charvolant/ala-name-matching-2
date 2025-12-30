package au.org.ala.names.generated;

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

import au.org.ala.bayesian.analysis.StringAnalysis;

public class GrassBuilder implements Builder<GrassClassification> {
  // Assumed to be stateless
  private static final Builder[] BUILDERS = new Builder[] {
    new GrassBuilder_()
  };

  private Map<String, Builder> subBuilders;


  public GrassBuilder() {
    this.subBuilders = new HashMap<>(BUILDERS.length);
    for (Builder b: BUILDERS)
      this.subBuilders.put(b.getSignature(), b);
  }

  @Override
  public String getSignature() {
    return null;
  }

  @Override
  public void generate(Classifier classifier, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
  }

  @Override
  public void interpret(Classifier classifier, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
  }

  @Override
  public void infer(Classifier classifier, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
  }

  @Override
  public void expand(Classifier classifier, Deque<Classifier> parents, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
  }

  @Override
  public Function<Classifier, Boolean> getBroadener(Classifier document, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
    List<Function<Classifier, Boolean>> broadeners = new ArrayList<>();
    return broadeners.isEmpty() ? null : (c -> broadeners.stream().anyMatch(b -> b.apply(c)));
  }

  @Override
  public String buildSignature(Classifier classifier) {
    char[] sig = new char[0];
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
