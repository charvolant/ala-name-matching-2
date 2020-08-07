package au.org.ala.names.generated;

import au.org.ala.bayesian.ClassificationMatcher;
import au.org.ala.bayesian.ClassifierSearcher;
import au.org.ala.bayesian.EvidenceAnalyser;
import au.org.ala.bayesian.NetworkFactory;
import au.org.ala.bayesian.Normaliser;
import au.org.ala.bayesian.Observable;
import static au.org.ala.bayesian.ExternalContext.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleLinnaeanFactory implements NetworkFactory<SimpleLinnaeanClassification, SimpleLinnaeanParameters, SimpleLinnaeanInferencer, SimpleLinnaeanFactory> {
    private static SimpleLinnaeanFactory instance = null;

  public static final Normaliser lowerCaseNormaliser = new au.org.ala.util.BasicNormaliser("lower_case_normaliser", true, true, true, true, true);
  public static final Normaliser simpleNormaliser = new au.org.ala.util.BasicNormaliser("simple_normaliser", true, true, true, true, false);

  public static final Observable acceptedNameUsageId = new Observable(
      "acceptedNameUsageID",
      URI.create("http://rs.tdwg.org/dwc/terms/acceptedNameUsageID"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      null,
      false
    );
  public static final Observable altScientificName = new Observable(
      "altScientificName",
      URI.create("http://id.ala.org.au/terms/1.0/altScientificName"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      null,
      false
    );
  public static final Observable class_ = new Observable(
      "class",
      URI.create("http://rs.tdwg.org/dwc/terms/class"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      false
    );
  public static final Observable family = new Observable(
      "family",
      URI.create("http://rs.tdwg.org/dwc/terms/family"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      false
    );
  public static final Observable genus = new Observable(
      "genus",
      URI.create("http://rs.tdwg.org/dwc/terms/genus"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      false
    );
  public static final Observable kingdom = new Observable(
      "kingdom",
      URI.create("http://rs.tdwg.org/dwc/terms/kingdom"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      false
    );
  public static final Observable order = new Observable(
      "order",
      URI.create("http://rs.tdwg.org/dwc/terms/order"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      false
    );
  public static final Observable parentNameUsageId = new Observable(
      "parentNameUsageID",
      URI.create("http://rs.tdwg.org/dwc/terms/parentNameUsageID"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      null,
      false
    );
  public static final Observable phylum = new Observable(
      "phylum",
      URI.create("http://rs.tdwg.org/dwc/terms/phylum"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      false
    );
  public static final Observable scientificName = new Observable(
      "scientificName",
      URI.create("http://rs.tdwg.org/dwc/terms/scientificName"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      false
    );
  public static final Observable scientificNameAuthorship = new Observable(
      "scientificNameAuthorship",
      URI.create("http://rs.tdwg.org/dwc/terms/scientificNameAuthorship"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      false
    );
  public static final Observable soundexScientificName = new Observable(
      "soundexScientificName",
      URI.create("http://id.ala.org.au/terms/1.0/soundexScientificName"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      null,
      false
    );
  public static final Observable specificEpithet = new Observable(
      "specificEpithet",
      URI.create("http://rs.tdwg.org/dwc/terms/specificEpithet"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      simpleNormaliser,
      false
    );
  public static final Observable taxonId = new Observable(
      "taxonID",
      URI.create("http://rs.tdwg.org/dwc/terms/taxonID"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      null,
      false
    );
  public static final Observable taxonRank = new Observable(
      "taxonRank",
      URI.create("http://rs.tdwg.org/dwc/terms/taxonRank"),
      java.lang.String.class,
      Observable.Style.CANONICAL,
      lowerCaseNormaliser,
      false
    );
  public static final Observable weight = new Observable(
      "weight",
      URI.create("http://id.ala.org.au/bayesian/1.0/weight"),
      java.lang.Double.class,
      Observable.Style.CANONICAL,
      null,
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
  public EvidenceAnalyser<SimpleLinnaeanClassification> createAnalyser() {
        return null;
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