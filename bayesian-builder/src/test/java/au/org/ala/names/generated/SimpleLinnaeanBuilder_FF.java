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
import au.org.ala.bayesian.analysis.IntegerAnalysis;
import au.org.ala.bayesian.analysis.DoubleAnalysis;

public class SimpleLinnaeanBuilder_FF implements Builder<SimpleLinnaeanClassification> {
  public final static String SIGNATURE = "FF";

  public  SimpleLinnaeanBuilder_FF() {
  }

  @Override
  public String getSignature() {
    return SIGNATURE;
  }

  @Override
  public void generate(Classifier classifier, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public void interpret(Classifier classifier, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public void infer(Classifier classifier, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public void expand(Classifier classifier, Deque<Classifier> parents, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public Function<Classifier, Boolean> getBroadener(Classifier document, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
    throw new UnsupportedOperationException("Sub-builders do not support this operation");
  }

  @Override
  public String buildSignature(Classifier classifier) {
    return SIGNATURE;
  }

  @Override
  public Parameters calculate(ParameterAnalyser analyser, Classifier classifier) throws BayesianException {
    SimpleLinnaeanParameters_FF parameters = new SimpleLinnaeanParameters_FF();
    parameters.prior_taxonId_t = analyser.computePrior(analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier));
    parameters.inf_taxonRank_t$t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.taxonRank, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier));
    parameters.inf_taxonRank_t$f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.taxonRank, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier));
    parameters.inf_scientificNameAuthorship_t$t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.scientificNameAuthorship, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier));
    parameters.inf_scientificNameAuthorship_t$f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.scientificNameAuthorship, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier));
    parameters.inf_scientificName_t$t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier));
    parameters.inf_scientificName_t$f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier));
    parameters.inf_prefixScientificName_t$t_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier));
    parameters.inf_prefixScientificName_t$t_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier));
    parameters.inf_prefixScientificName_t$f_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier));
    parameters.inf_prefixScientificName_t$f_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier));
    parameters.inf_soundexScientificName_t$t_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier));
    parameters.inf_soundexScientificName_t$t_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier));
    parameters.inf_soundexScientificName_t$f_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier));
    parameters.inf_soundexScientificName_t$f_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier));
    parameters.inf_family_t$t_ttt = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$t_ttf = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$t_tft = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$t_tff = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$t_ftt = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$t_ftf = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$t_fft = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$t_fff = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$f_ttt = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$f_ttf = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$f_tft = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$f_tff = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$f_ftt = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$f_ftf = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$f_fft = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_family_t$f_fff = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.scientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.prefixScientificName, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.soundexScientificName, classifier));
    parameters.inf_order_t$t_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.order, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier));
    parameters.inf_order_t$t_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.order, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.family, classifier));
    parameters.inf_order_t$f_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.order, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.family, classifier));
    parameters.inf_order_t$f_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.order, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.family, classifier));
    parameters.inf_class__t$t_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.class_, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.order, classifier));
    parameters.inf_class__t$t_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.class_, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.order, classifier));
    parameters.inf_class__t$f_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.class_, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.order, classifier));
    parameters.inf_class__t$f_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.class_, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.order, classifier));
    parameters.inf_phylum_t$t_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.phylum, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.class_, classifier));
    parameters.inf_phylum_t$t_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.phylum, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.class_, classifier));
    parameters.inf_phylum_t$f_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.phylum, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.class_, classifier));
    parameters.inf_phylum_t$f_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.phylum, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.class_, classifier));
    parameters.inf_kingdom_t$t_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.kingdom, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.phylum, classifier));
    parameters.inf_kingdom_t$t_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.kingdom, classifier) , analyser.getObservation(true, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.phylum, classifier));
    parameters.inf_kingdom_t$f_t = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.kingdom, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(true, SimpleLinnaeanFactory.phylum, classifier));
    parameters.inf_kingdom_t$f_f = analyser.computeConditional(analyser.getObservation(true, SimpleLinnaeanFactory.kingdom, classifier) , analyser.getObservation(false, SimpleLinnaeanFactory.taxonId, classifier), analyser.getObservation(false, SimpleLinnaeanFactory.phylum, classifier));
    return parameters;
  }
}
