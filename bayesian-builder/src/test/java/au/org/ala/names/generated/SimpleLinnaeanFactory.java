package au.org.ala.names.generated;

import au.org.ala.bayesian.ClassificationMatcher;
import au.org.ala.bayesian.ClassifierSearcher;
import au.org.ala.bayesian.Analyser;
import au.org.ala.bayesian.NetworkFactory;
import au.org.ala.bayesian.Normaliser;
import au.org.ala.bayesian.Observable;
import static au.org.ala.bayesian.ExternalContext.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;

import au.org.ala.bayesian.analysis.StringAnalysis;
import au.org.ala.bayesian.Analyser;
import au.org.ala.bayesian.analysis.DoubleAnalysis;
import au.org.ala.bayesian.ClassificationMatcher;

public class SimpleLinnaeanFactory implements NetworkFactory<SimpleLinnaeanClassification, SimpleLinnaeanParameters, SimpleLinnaeanInferencer, SimpleLinnaeanFactory> {
    private static SimpleLinnaeanFactory instance = null;

  public static final Normaliser lowerCaseNormaliser = new au.org.ala.util.BasicNormaliser("lower_case_normaliser", true, true, true, true, true);
  public static final Normaliser simpleNormaliser = new au.org.ala.util.BasicNormaliser("simple_normaliser", true, true, true, true, false);

  public static final Observable acceptedNameUsageId = new Observable(
      "acceptedNameUsageID",
      URI.create("http://rs.tdwg.org/dwc/terms/acceptedNameUsageID"),
      String.class,
      Observable.Style.CANONICAL,
      null,
      new StringAnalysis(),
      false
    );
  public static final Observable altScientificName = new Observable(
      "altScientificName",
      URI.create("http://id.ala.org.au/terms/1.0/altScientificName"),
      String.class,
      Observable.Style.CANONICAL,
      null,
      new StringAnalysis(),
      false
    );
  public static final Observable class_ = new Observable(
      "class",
      URI.create("http://rs.tdwg.org/dwc/terms/class"),
      String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable family = new Observable(
      "family",
      URI.create("http://rs.tdwg.org/dwc/terms/family"),
      String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable genus = new Observable(
      "genus",
      URI.create("http://rs.tdwg.org/dwc/terms/genus"),
      String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable kingdom = new Observable(
      "kingdom",
      URI.create("http://rs.tdwg.org/dwc/terms/kingdom"),
      String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable order = new Observable(
      "order",
      URI.create("http://rs.tdwg.org/dwc/terms/order"),
      String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable parentNameUsageId = new Observable(
      "parentNameUsageID",
      URI.create("http://rs.tdwg.org/dwc/terms/parentNameUsageID"),
      String.class,
      Observable.Style.CANONICAL,
      null,
      new StringAnalysis(),
      false
    );
  public static final Observable phylum = new Observable(
      "phylum",
      URI.create("http://rs.tdwg.org/dwc/terms/phylum"),
      String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable scientificName = new Observable(
      "scientificName",
      URI.create("http://rs.tdwg.org/dwc/terms/scientificName"),
      String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable scientificNameAuthorship = new Observable(
      "scientificNameAuthorship",
      URI.create("http://rs.tdwg.org/dwc/terms/scientificNameAuthorship"),
      String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable soundexScientificName = new Observable(
      "soundexScientificName",
      URI.create("http://id.ala.org.au/terms/1.0/soundexScientificName"),
      String.class,
      Observable.Style.CANONICAL,
      null,
      new StringAnalysis(),
      false
    );
  public static final Observable specificEpithet = new Observable(
      "specificEpithet",
      URI.create("http://rs.tdwg.org/dwc/terms/specificEpithet"),
      String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable taxonId = new Observable(
      "taxonID",
      URI.create("http://rs.tdwg.org/dwc/terms/taxonID"),
      String.class,
      Observable.Style.CANONICAL,
      null,
      new StringAnalysis(),
      false
    );
  public static final Observable taxonRank = new Observable(
      "taxonRank",
      URI.create("http://rs.tdwg.org/dwc/terms/taxonRank"),
      String.class,
      Observable.Style.CANONICAL,
      lowerCaseNormaliser,
      new StringAnalysis(),
      false
    );
  public static final Observable weight = new Observable(
      "weight",
      URI.create("http://id.ala.org.au/bayesian/1.0/weight"),
      Double.class,
      Observable.Style.CANONICAL,
      null,
      new DoubleAnalysis(),
      false
    );

  public static List<Observable> OBSERVABLES = Collections.unmodifiableList(Arrays.asList(
    acceptedNameUsageId,
    altScientificName,
    class_,
    family,
    genus,
    kingdom,
    order,
    parentNameUsageId,
    phylum,
    scientificName,
    scientificNameAuthorship,
    soundexScientificName,
    specificEpithet,
    taxonId,
    taxonRank,
    weight
  ));

  public static final TermFactory TERM_FACTORY = TermFactory.instance();

  /** Issue if remove_class is used using matching */
  public static final Term ISSUE_REMOVE_CLASS = TERM_FACTORY.findTerm("http://id.ala.org.au/issues/1.0/removedClass");
  /** Issue if remove_order is used using matching */
  public static final Term ISSUE_REMOVE_ORDER = TERM_FACTORY.findTerm("http://id.ala.org.au/issues/1.0/removedOrder");
  /** Issue if remove_phylum is used using matching */
  public static final Term ISSUE_REMOVE_PHYLUM = TERM_FACTORY.findTerm("http://id.ala.org.au/issues/1.0/removedPhylum");
  /** Issue if remove_rank is used using matching */
  public static final Term ISSUE_REMOVE_RANK = TERM_FACTORY.findTerm("http://id.ala.org.au/issues/1.0/removedRank");

  static {
    acceptedNameUsageId.setExternal(LUCENE, "dwc_acceptedNameUsageID");
    altScientificName.setExternal(LUCENE, "altScientificName");
    class_.setExternal(LUCENE, "dwc_class");
    family.setExternal(LUCENE, "dwc_family");
    genus.setExternal(LUCENE, "dwc_genus");
    kingdom.setExternal(LUCENE, "dwc_kingdom");
    order.setExternal(LUCENE, "dwc_order");
    parentNameUsageId.setExternal(LUCENE, "dwc_parentNameUsageID");
    phylum.setExternal(LUCENE, "dwc_phylum");
    scientificName.setExternal(LUCENE, "dwc_scientificName");
    scientificNameAuthorship.setExternal(LUCENE, "dwc_scientificNameAuthorship");
    soundexScientificName.setExternal(LUCENE, "soundexScientificName");
    specificEpithet.setExternal(LUCENE, "dwc_specificEpithet");
    taxonId.setExternal(LUCENE, "dwc_taxonID");
    taxonRank.setExternal(LUCENE, "dwc_taxonRank");
    weight.setExternal(LUCENE, "bayesian_weight");
  }

  @Override
  public List<Observable> getObservables() {
      return OBSERVABLES;
  }

  @Override
  public SimpleLinnaeanClassification createClassification() {
      return new SimpleLinnaeanClassification();
  }

  @Override
  public SimpleLinnaeanInferencer createInferencer() {
      return new SimpleLinnaeanInferencer();
  }

  @Override
  public SimpleLinnaeanParameters createParameters() {
        return new SimpleLinnaeanParameters();
  }

  @Override
  public Analyser<SimpleLinnaeanClassification> createAnalyser() {
        return new au.org.ala.bayesian.NullAnalyser<>();
  }

  @Override
  public ClassificationMatcher<SimpleLinnaeanClassification, SimpleLinnaeanParameters, SimpleLinnaeanInferencer, SimpleLinnaeanFactory> createMatcher(ClassifierSearcher searcher){
        return new ClassificationMatcher<>(this, searcher);
  }

  public static SimpleLinnaeanFactory instance() {
      if (instance == null) {
          synchronized (SimpleLinnaeanFactory.class) {
              if (instance == null) {
                  instance = new SimpleLinnaeanFactory();
              }
          }
      }
      return instance;
  }
}
