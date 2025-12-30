package au.org.ala.names;

import au.org.ala.bayesian.Observable;
import au.org.ala.bayesian.*;
import au.org.ala.names.builder.BuildAnalyser;
import au.org.ala.vocab.TaxonomicStatus;
import au.org.ala.vocab.VernacularStatus;
import org.gbif.nameparser.api.Rank;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlaVernacularBuildAnalyser implements BuildAnalyser<AlaVernacularClassification> {
    /** The default set of weights to apply */
    private static final Map<VernacularStatus, Double> STATUS_WEIGHT_MAP;

    private static final Pattern DASH = Pattern.compile("(?<=\\w)\\s*-\\s*(?=\\w)");
    private static final Pattern POSSESSIVE = Pattern.compile("(?<=\\w)'(?=s?\\s)");

    static {
        STATUS_WEIGHT_MAP = new HashMap<>();
        STATUS_WEIGHT_MAP.put(VernacularStatus.legislated, 1000.0);
        STATUS_WEIGHT_MAP.put(VernacularStatus.standard, 100.0);
        STATUS_WEIGHT_MAP.put(VernacularStatus.preferred, 20.0);
        STATUS_WEIGHT_MAP.put(VernacularStatus.common, 10.0);
        STATUS_WEIGHT_MAP.put(VernacularStatus.traditionalKnowledge, 20.0);
        STATUS_WEIGHT_MAP.put(VernacularStatus.local, 5.0);
        STATUS_WEIGHT_MAP.put(VernacularStatus.deprecated, 1.0);
    }

    /**
     * Construct for am ignored configuration
     *
     * @param config The configuration
     */
    public AlaVernacularBuildAnalyser(AnalyserConfig config) {
    }

    /**
     * Analyse the information in a classifier and extend the classifier
     * as required for indexing.
     *
     * @param classifier The classification
     */
    @Override
    public void analyseForIndex(Classifier classifier) throws StoreException {
        if (!classifier.has(AlaVernacularFactory.weight)) {
            double weight = STATUS_WEIGHT_MAP.getOrDefault(classifier.get(AlaVernacularFactory.vernacularStatus), 1.0);
            Rank taxonRank = classifier.get(AlaVernacularFactory.taxonRank);
            TaxonomicStatus taxonomicStatus = classifier.get(AlaVernacularFactory.taxonomicStatus);
            if (taxonRank == Rank.SPECIES)
                weight *= 10;
            else if (taxonRank == Rank.GENUS)
                weight *= 2;
            else if (taxonRank != null && taxonRank != Rank.FAMILY && taxonRank != Rank.SUBSPECIES)
                weight /= 10.0;
            if (taxonomicStatus != null && !taxonomicStatus.isAcceptedFlag())
                weight /= 10.0;
            classifier.add(AlaVernacularFactory.weight, Math.max(1.0, weight), false, true);
        }
        if (classifier.has(AlaVernacularFactory.acceptedNameUsageId)) {
            classifier.add(AlaVernacularFactory.taxonId, classifier.get(AlaVernacularFactory.acceptedNameUsageId), false, true);
        }
    }

    /**
     * Build a collection of base names for the classification.
     * <p>
     * Sort out variations on hyphenation and possessives.
     * </p>
     *
     * @param classifier The classification
     * @param name       The observable that gives the name
     * @param complete   The observable that gives the complete name
     * @param disambiguator The observable that gives additional disambiguation, geneerally complete = name + ' ' + disambiguator
     * @param canonical  Only include canonical names
     * @return All the names that refer to the classification
     */
    @Override
    public Set<String> analyseNames(Classifier classifier, Observable<String> name, Optional<Observable<String>> complete, Optional<Observable<String>> disambiguator, boolean canonical) throws InferenceException {
        Set<String> allNames = new HashSet<>(classifier.getAll(name));
        if (complete.isPresent())
            allNames.addAll(classifier.getAll(complete.get()));
        Set<String> names = new HashSet<>(allNames);
 
        if (allNames.isEmpty())
            throw new InferenceException("No name for " + classifier.get(AlaVernacularFactory.nameId) + " at " +  classifier.get(AlaVernacularFactory.taxonId));
        for (String nm : allNames) {
            names.add(ScientificNameAnalyser.BASIC_NORMALISER.normalise(nm));
            names.add(ScientificNameAnalyser.PUNCTUATION_NORMALISER.normalise(nm));
            String nnm = ScientificNameAnalyser.FULL_NORMALISER.normalise(nm);
            names.add(nnm);
            Set<String> moreNames = new HashSet<>();
            moreNames.add(nnm);
            Matcher matcher = POSSESSIVE.matcher(nnm);
            if (matcher.find())
                moreNames.add(matcher.replaceAll(""));
            for (String mnm: moreNames) {
                names.add(mnm);
                matcher = DASH.matcher(mnm);
                if (matcher.find()) {
                    names.add(matcher.replaceAll(" "));
                    names.add(matcher.replaceAll(""));
                }
            }
        }
        return names;
    }

    /**
     * Decide whether to accept a synonym or not.
      *
     * @param base      The base classifier the synonym is for
     * @param candidate The classifier for the synonym
     *
     * @return True by default
     */
    @Override
    public boolean acceptSynonym(Classifier base, Classifier candidate) {
        return true;
    }
}
