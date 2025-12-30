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
import au.org.ala.bayesian.derivation.PrefixDerivation.PrefixGenerator;
import au.org.ala.bayesian.derivation.SoundexGenerator;
import au.org.ala.bayesian.analysis.IntegerAnalysis;
import au.org.ala.bayesian.analysis.DoubleAnalysis;

public class SimpleLinnaeanBuilder implements Builder<SimpleLinnaeanClassification> {
  // Assumed to be stateless
  private static final Builder[] BUILDERS = new Builder[] {
    new SimpleLinnaeanBuilder_TT(),
    new SimpleLinnaeanBuilder_TF(),
    new SimpleLinnaeanBuilder_FT(),
    new SimpleLinnaeanBuilder_FF()
  };

  private Map<String, Builder> subBuilders;

  private SoundexGenerator soundex;
  private PrefixGenerator prefixer;

  public SimpleLinnaeanBuilder() {
    this.soundex = new SoundexGenerator();
    this.prefixer = new PrefixGenerator();
    this.subBuilders = new HashMap<>(BUILDERS.length);
    for (Builder b: BUILDERS)
      this.subBuilders.put(b.getSignature(), b);
  }

  @Override
  public String getSignature() {
    return null;
  }

  @Override
  public void generate(Classifier classifier, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
  }

  @Override
  public void interpret(Classifier classifier, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
  }

  @Override
  public void infer(Classifier classifier, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
      if (!classifier.has(SimpleLinnaeanFactory.soundexScientificName)) {
        java.lang.String i_0 = classifier.get(SimpleLinnaeanFactory.scientificName);
        java.lang.String v_0 = this.soundex.soundex((String) i_0);
        classifier.add(SimpleLinnaeanFactory.soundexScientificName, SimpleLinnaeanFactory.soundexScientificName.analyse(v_0), false, false);
      }
      for (java.lang.String i_0: classifier.getAll(SimpleLinnaeanFactory.scientificName)){
        java.lang.String v_0 = this.soundex.soundex((String) i_0);
        classifier.add(SimpleLinnaeanFactory.soundexScientificName, SimpleLinnaeanFactory.soundexScientificName.analyse(v_0), true, false);
      }
      if (!classifier.has(SimpleLinnaeanFactory.prefixScientificName)) {
        java.lang.String i_1 = classifier.get(SimpleLinnaeanFactory.scientificName);
        java.lang.String v_1 = this.prefixer.generate((String) i_1, 5, 8).stream().findFirst().orElse(null);
        classifier.add(SimpleLinnaeanFactory.prefixScientificName, SimpleLinnaeanFactory.prefixScientificName.analyse(v_1), false, false);
      }
      for (java.lang.String i_1: classifier.getAll(SimpleLinnaeanFactory.scientificName)){
        for (java.lang.String v_1: this.prefixer.generate((String) i_1, 5, 8)) {
          classifier.add(SimpleLinnaeanFactory.prefixScientificName, SimpleLinnaeanFactory.prefixScientificName.analyse(v_1), true, false);
        }
      }
  }

  @Override
  public void expand(Classifier classifier, Deque<Classifier> parents, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
      Classifier d_0;
      if (classifier.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "genus".equals(x)))
          d_0 = classifier;
      else
          d_0 = parents.stream().filter(c -> c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "genus".equals(x))).findFirst().orElse(null);
      if (d_0 != null) {
        if (!classifier.has(SimpleLinnaeanFactory.genus)) {
          java.lang.String i_0 = d_0.get(SimpleLinnaeanFactory.scientificName);
          classifier.add(SimpleLinnaeanFactory.genus, SimpleLinnaeanFactory.genus.analyse(i_0), false, false);
        }
      }
      Classifier d_1;
      if (classifier.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "family".equals(x)))
          d_1 = classifier;
      else
          d_1 = parents.stream().filter(c -> c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "family".equals(x))).findFirst().orElse(null);
      if (d_1 != null) {
        if (!classifier.has(SimpleLinnaeanFactory.family)) {
          java.lang.String i_1 = d_1.get(SimpleLinnaeanFactory.scientificName);
          classifier.add(SimpleLinnaeanFactory.family, SimpleLinnaeanFactory.family.analyse(i_1), false, false);
        }
        for(java.lang.String j_1: d_1.getAll(SimpleLinnaeanFactory.scientificName, SimpleLinnaeanFactory.synonymScientificName, SimpleLinnaeanFactory.broadSynonymScientificName)) {
          classifier.add(SimpleLinnaeanFactory.family, SimpleLinnaeanFactory.family.analyse(j_1),  true, false);
        }
      }
      Classifier d_2;
      if (classifier.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "order".equals(x)))
          d_2 = classifier;
      else
          d_2 = parents.stream().filter(c -> c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "order".equals(x))).findFirst().orElse(null);
      if (d_2 != null) {
        if (!classifier.has(SimpleLinnaeanFactory.order)) {
          java.lang.String i_2 = d_2.get(SimpleLinnaeanFactory.scientificName);
          classifier.add(SimpleLinnaeanFactory.order, SimpleLinnaeanFactory.order.analyse(i_2), false, false);
        }
        for(java.lang.String j_2: d_2.getAll(SimpleLinnaeanFactory.scientificName, SimpleLinnaeanFactory.synonymScientificName, SimpleLinnaeanFactory.broadSynonymScientificName)) {
          classifier.add(SimpleLinnaeanFactory.order, SimpleLinnaeanFactory.order.analyse(j_2),  true, false);
        }
      }
      Classifier d_3;
      if (classifier.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "class".equals(x)))
          d_3 = classifier;
      else
          d_3 = parents.stream().filter(c -> c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "class".equals(x))).findFirst().orElse(null);
      if (d_3 != null) {
        if (!classifier.has(SimpleLinnaeanFactory.class_)) {
          java.lang.String i_3 = d_3.get(SimpleLinnaeanFactory.scientificName);
          classifier.add(SimpleLinnaeanFactory.class_, SimpleLinnaeanFactory.class_.analyse(i_3), false, false);
        }
        for(java.lang.String j_3: d_3.getAll(SimpleLinnaeanFactory.scientificName, SimpleLinnaeanFactory.synonymScientificName)) {
          classifier.add(SimpleLinnaeanFactory.class_, SimpleLinnaeanFactory.class_.analyse(j_3),  true, false);
        }
      }
      Classifier d_4;
      if (classifier.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "phylum".equals(x)))
          d_4 = classifier;
      else
          d_4 = parents.stream().filter(c -> c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "phylum".equals(x))).findFirst().orElse(null);
      if (d_4 != null) {
        if (!classifier.has(SimpleLinnaeanFactory.phylum)) {
          java.lang.String i_4 = d_4.get(SimpleLinnaeanFactory.scientificName);
          classifier.add(SimpleLinnaeanFactory.phylum, SimpleLinnaeanFactory.phylum.analyse(i_4), false, false);
        }
        for(java.lang.String j_4: d_4.getAll(SimpleLinnaeanFactory.scientificName, SimpleLinnaeanFactory.synonymScientificName)) {
          classifier.add(SimpleLinnaeanFactory.phylum, SimpleLinnaeanFactory.phylum.analyse(j_4),  true, false);
        }
      }
      Classifier d_5;
      if (classifier.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "kingdom".equals(x)))
          d_5 = classifier;
      else
          d_5 = parents.stream().filter(c -> c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "kingdom".equals(x))).findFirst().orElse(null);
      if (d_5 != null) {
        if (!classifier.has(SimpleLinnaeanFactory.kingdom)) {
          java.lang.String i_5 = d_5.get(SimpleLinnaeanFactory.scientificName);
          classifier.add(SimpleLinnaeanFactory.kingdom, SimpleLinnaeanFactory.kingdom.analyse(i_5), false, false);
        }
        for(java.lang.String j_5: d_5.getAll(SimpleLinnaeanFactory.scientificName, SimpleLinnaeanFactory.synonymScientificName)) {
          classifier.add(SimpleLinnaeanFactory.kingdom, SimpleLinnaeanFactory.kingdom.analyse(j_5),  true, false);
        }
      }
  }

  @Override
  public Function<Classifier, Boolean> getBroadener(Classifier document, Analyser<SimpleLinnaeanClassification> analyser, BuildAnalyser<SimpleLinnaeanClassification> buildAnalyser) throws BayesianException {
    List<Function<Classifier, Boolean>> broadeners = new ArrayList<>();
    if (document.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "family".equals(x))) {
      broadeners.add(c -> (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "superfamily".equals(x))) || (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "subfamily".equals(x))));
    }
    if (document.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "order".equals(x))) {
      broadeners.add(c -> (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "superorder".equals(x))) || (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "suborder".equals(x))));
    }
    if (document.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "class".equals(x))) {
      broadeners.add(c -> (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "superclass".equals(x))) || (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "subclass".equals(x))));
    }
    if (document.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "phylum".equals(x))) {
      broadeners.add(c -> (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "superphylum".equals(x))) || (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "subphylum".equals(x))));
    }
    if (document.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "kingdom".equals(x))) {
      broadeners.add(c -> (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "superkingdom".equals(x))) || (c.getAll(SimpleLinnaeanFactory.taxonRank).stream().anyMatch(x -> "subkingdom".equals(x))));
    }
    return broadeners.isEmpty() ? null : (c -> broadeners.stream().anyMatch(b -> b.apply(c)));
  }

  @Override
  public String buildSignature(Classifier classifier) {
    char[] sig = new char[2];
    sig[0] = (classifier.hasAny(SimpleLinnaeanFactory.specificEpithet)) ? 'T' : 'F';
    sig[1] = (classifier.hasAny(SimpleLinnaeanFactory.genus)) ? 'T' : 'F';
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
