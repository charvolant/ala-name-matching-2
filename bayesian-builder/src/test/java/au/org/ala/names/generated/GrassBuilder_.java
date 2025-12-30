package au.org.ala.names.generated;

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

import au.org.ala.bayesian.analysis.StringAnalysis;

public class GrassBuilder_ implements Builder<GrassClassification> {
  public final static String SIGNATURE = "";

  public  GrassBuilder_() {
  }

  @Override
  public String getSignature() {
    return SIGNATURE;
  }

  @Override
  public void generate(Classifier classifier, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public void interpret(Classifier classifier, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public void infer(Classifier classifier, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public void expand(Classifier classifier, Deque<Classifier> parents, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public Function<Classifier, Boolean> getBroadener(Classifier document, Analyser<GrassClassification> analyser, BuildAnalyser<GrassClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public String buildSignature(Classifier classifier) {
    return SIGNATURE;
  }

  @Override
  public Parameters calculate(ParameterAnalyser analyser, Classifier classifier) throws BayesianException {
    GrassParameters_ parameters = new GrassParameters_();
    parameters.prior_rain_t = analyser.computePrior(analyser.getObservation(true, GrassFactory.rain, classifier));
    parameters.inf_sprinkler_t$t = analyser.computeConditional(analyser.getObservation(true, GrassFactory.sprinkler, classifier) , analyser.getObservation(true, GrassFactory.rain, classifier));
    parameters.inf_sprinkler_t$f = analyser.computeConditional(analyser.getObservation(true, GrassFactory.sprinkler, classifier) , analyser.getObservation(false, GrassFactory.rain, classifier));
    parameters.inf_wet_t$t_t = analyser.computeConditional(analyser.getObservation(true, GrassFactory.wet, classifier) , analyser.getObservation(true, GrassFactory.rain, classifier), analyser.getObservation(true, GrassFactory.sprinkler, classifier));
    parameters.inf_wet_t$t_f = analyser.computeConditional(analyser.getObservation(true, GrassFactory.wet, classifier) , analyser.getObservation(true, GrassFactory.rain, classifier), analyser.getObservation(false, GrassFactory.sprinkler, classifier));
    parameters.inf_wet_t$f_t = analyser.computeConditional(analyser.getObservation(true, GrassFactory.wet, classifier) , analyser.getObservation(false, GrassFactory.rain, classifier), analyser.getObservation(true, GrassFactory.sprinkler, classifier));
    parameters.inf_wet_t$f_f = analyser.computeConditional(analyser.getObservation(true, GrassFactory.wet, classifier) , analyser.getObservation(false, GrassFactory.rain, classifier), analyser.getObservation(false, GrassFactory.sprinkler, classifier));
    return parameters;
  }
}
