package au.org.ala.names;

import au.org.ala.bayesian.Observable;
import au.org.ala.bayesian.*;
import au.org.ala.vocab.ALATerm;
import au.org.ala.vocab.BayesianTerm;
import com.google.common.base.Enums;
import org.apache.commons.text.StringEscapeUtils;
import org.gbif.api.vocabulary.NomenclaturalCode;
import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.ParsedName;
import org.gbif.nameparser.api.Rank;
import org.gbif.nameparser.util.NameFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AlaNameAnalyser extends ScientificNameAnalyser<AlaLinnaeanClassification> implements Analyser<AlaLinnaeanClassification> {
    private final Logger logger = LoggerFactory.getLogger(AlaNameAnalyser.class);

    /** Invalid authorship */
    public static final Pattern INVALID_AUTHORSHIP = Pattern.compile("\\d+");
    /** The distance in ranks allowable when looking at synonyms */
    public static final int SYNONYM_RANK_DISTANCE = 2000;

    private static final Issues INDETERMINATE_ISSUES = Issues.of(AlaLinnaeanFactory.INDETERMINATE_NAME);
    private static final Issues AFFINTIY_ISSUES = Issues.of(AlaLinnaeanFactory.AFFINITY_SPECIES_NAME);
    private static final Issues CONFER_ISSUES = Issues.of(AlaLinnaeanFactory.CONFER_SPECIES_NAME);
    private static final Issues UNPARSABLE_ISSUES = Issues.of(AlaLinnaeanFactory.UNPARSABLE_NAME);
    private static final Issues CANONICAL_MODIFICATION = Issues.of(AlaLinnaeanFactory.CANONICAL_NAME);
    private static final Issues DATA_ISSUES = Issues.of(BayesianTerm.illformedData);
    private static final Issues KINGDOM_ISSUES = Issues.of(AlaLinnaeanFactory.INVALID_KINGDOM);
    private static final Issues KINGDOM_MODIFICATION = Issues.of(AlaLinnaeanFactory.REMOVED_KINGDOM);
    private static final Issues BARE_PHRASE_NAME_ISSUES = Issues.of(AlaLinnaeanFactory.BARE_PHRASE_NAME);

    /**
     * A numeric or single letter placeholder name. We can extract the genus from this if we haven't already
     */
    public static final Pattern NUMERIC_PLACEHOLDER = Pattern.compile("^([A-Z][a-z]+)\\s(?:(?:" + RANK_MARKERS + ")\\.?\\s?)?(?:[A-Z]|\\d+)$");

    /**
     * Construct for a configuration
     *
     * @param config The configuration
     */
    public AlaNameAnalyser(AnalyserConfig config) {
        super(config);
    }

    /**
     * Remove any invalid entries from the classification before processing it.
     *
     * @param analysis The current analysis
     * @param classification The classification
     */
    protected void removeInvalid(Analysis analysis, AlaLinnaeanClassification classification) {
        classification.kingdom = this.checkInvalid(classification.kingdom, analysis, DATA_ISSUES, CANONICAL_MODIFICATION);
        classification.phylum = this.checkInvalid(classification.phylum, analysis, DATA_ISSUES, CANONICAL_MODIFICATION);
        classification.class_ = this.checkInvalid(classification.class_, analysis, DATA_ISSUES, CANONICAL_MODIFICATION);
        classification.order = this.checkInvalid(classification.order, analysis, DATA_ISSUES, CANONICAL_MODIFICATION);
        classification.family = this.checkInvalid(classification.family, analysis, DATA_ISSUES, CANONICAL_MODIFICATION);
        classification.genus = this.checkInvalid(classification.genus, analysis, DATA_ISSUES, CANONICAL_MODIFICATION);
        classification.specificEpithet = this.checkInvalid(classification.specificEpithet, analysis, DATA_ISSUES, CANONICAL_MODIFICATION);
        classification.scientificName = this.checkInvalid(classification.scientificName, analysis, DATA_ISSUES, CANONICAL_MODIFICATION);
        analysis.setScientificName(this.checkInvalid(analysis.getScientificName(), analysis, DATA_ISSUES, CANONICAL_MODIFICATION));
    }


    /**
     * This is a really, really annoying thing where we split apart names of
     * the form <em>fam. Gracilariaceae gen. Gracilaria</em> I mean,
     * come on people!
     *
     * @param analysis Thw analyser
     * @param classification The classifiction to fill out
     *
     * @return True if detected
     */
    protected boolean inferClassification(Analysis analysis, AlaLinnaeanClassification classification) {
        Map<Rank, String> map = this.detectAlternatingRankName(analysis, CANONICAL_MODIFICATION);
        if (map == null)
            return false;
        classification.scientificName = null;
        analysis.setScientificName(null);
        if (classification.kingdom == null) {
            classification.kingdom = map.get(Rank.KINGDOM);
        }
        if (classification.phylum == null) {
            classification.phylum = map.get(Rank.PHYLUM);
        }
        if (classification.class_ == null) {
            classification.class_ = map.get(Rank.CLASS);
        }
        if (classification.order == null) {
            classification.order = map.get(Rank.ORDER);
        }
        if (classification.family == null) {
            classification.family = map.get(Rank.FAMILY);
        }
        if (classification.genus == null) {
            classification.genus = map.get(Rank.GENUS);
        }
        if (classification.specificEpithet == null) {
            classification.specificEpithet = map.get(Rank.SPECIES);
        }
        return true;
    }

    /**
     * If a classification does not have a direct scientific name/rank combination then infer it.
     * Sub-species ranks are generalised to be a genric infraspecific rank, since there's too
     * much disagreement about what is actually valid.
     *
     * @param analysis       The analysis state
     * @param classification The classification
     */
    protected void inferRank(Analysis analysis, AlaLinnaeanClassification classification) {
        if (classification == null)
            return;
        if (classification.scientificName == null) {
            if (classification.specificEpithet != null && classification.genus != null) {
                classification.scientificName = classification.genus + " " + classification.specificEpithet;
                analysis.setScientificName(classification.scientificName);
                analysis.estimateRank(Rank.SPECIES);
            } else if (classification.genus != null) {
                classification.scientificName = classification.genus;
                analysis.setScientificName(classification.scientificName);
                analysis.estimateRank(Rank.GENUS);
            } else if (classification.family != null) {
                classification.scientificName = classification.family;
                analysis.setScientificName(classification.scientificName);
                analysis.estimateRank(Rank.FAMILY);
            } else if (classification.order != null) {
                classification.scientificName = classification.order;
                analysis.setScientificName(classification.scientificName);
                analysis.estimateRank(Rank.ORDER);
            } else if (classification.class_ != null) {
                classification.scientificName = classification.class_;
                analysis.setScientificName(classification.scientificName);
                analysis.estimateRank(Rank.CLASS);
            } else if (classification.phylum != null) {
                classification.scientificName = classification.phylum;
                analysis.setScientificName(classification.scientificName);
                analysis.estimateRank(Rank.PHYLUM);
            } else if (classification.kingdom != null) {
                classification.scientificName = classification.kingdom;
                analysis.setScientificName(classification.scientificName);
                analysis.estimateRank(Rank.KINGDOM);
            }
        }
        if (!analysis.isUnranked() && (classification.taxonRank == null || classification.taxonRank.otherOrUnranked())) {
            classification.taxonRank = analysis.getRank();
        }
    }

    /**
     * Generate base names from a classifier.
     * <p>
     * If an explicit scientific name is not specified, then see if we can track down a higher level name
     * and deduce the rank.
     * </p>
     *
     * @param analysis   The analysis state
     * @param classifier The classifier
     * @param name       The name observable
     * @return A set of base names
     */
    protected Set<String> generateBaseNames(Analysis analysis, Classifier classifier, Observable<String> name) {
        Set<String> names = new LinkedHashSet<>(classifier.getAll(name));

        if (names.isEmpty() && classifier.has(AlaLinnaeanFactory.genus) && classifier.has(AlaLinnaeanFactory.specificEpithet)) {
            names = classifier.getAll(AlaLinnaeanFactory.genus).stream().flatMap(g -> classifier.getAll(AlaLinnaeanFactory.specificEpithet).stream().map(s -> g + " " + s)).collect(Collectors.toSet());
            analysis.estimateRank(Rank.SPECIES);
        }
        if (names.isEmpty() && classifier.has(AlaLinnaeanFactory.genus)) {
            names = classifier.getAll(AlaLinnaeanFactory.genus);
            analysis.estimateRank(Rank.GENUS);
        }
        if (names.isEmpty() && classifier.has(AlaLinnaeanFactory.family)) {
            names = classifier.getAll(AlaLinnaeanFactory.family);
            analysis.estimateRank(Rank.FAMILY);
        }
        if (names.isEmpty() && classifier.has(AlaLinnaeanFactory.order)) {
            names = classifier.getAll(AlaLinnaeanFactory.order);
            analysis.estimateRank(Rank.ORDER);
        }
        if (names.isEmpty() && classifier.has(AlaLinnaeanFactory.class_)) {
            names = classifier.getAll(AlaLinnaeanFactory.class_);
            analysis.estimateRank(Rank.CLASS);
        }
        if (names.isEmpty() && classifier.has(AlaLinnaeanFactory.phylum)) {
            names = classifier.getAll(AlaLinnaeanFactory.phylum);
            analysis.estimateRank(Rank.PHYLUM);
        }
        if (names.isEmpty() && classifier.has(AlaLinnaeanFactory.kingdom)) {
            names = classifier.getAll(AlaLinnaeanFactory.kingdom);
            analysis.estimateRank(Rank.KINGDOM);
        }
        return names;
    }

    protected void fillOutClassification(Analysis analysis, AlaLinnaeanClassification classification) throws InferenceException {
        ParsedName name = analysis.getParsedName();
        if (classification == null || name == null)
            return;
        if (name.isPhraseName()) {
            if (classification.nominatingParty == null && name.getNominatingParty() != null) {
                classification.nominatingParty = name.getNominatingParty();
            }
            if (classification.voucher == null && name.getVoucher() != null) {
                classification.voucher = AlaLinnaeanFactory.voucher.getAnalysis().analyse(name.getVoucher());
            }
            if (classification.phraseName == null && name.getPhrase() != null) {
                classification.phraseName = AlaLinnaeanFactory.phraseName.getAnalysis().analyse(name.getPhrase());
            }
        }
        if (name.getType() == NameType.SCIENTIFIC || name.getType() == NameType.INFORMAL || name.getType() == NameType.PLACEHOLDER) {
            // Only do this for non-synonyms to avoid dangling speciesID
            if (classification.acceptedNameUsageId == null && classification.specificEpithet == null && name.getSpecificEpithet() != null && !analysis.isUncertain())
                classification.specificEpithet = name.getSpecificEpithet();

        }
        if (classification.acceptedNameUsageId == null && classification.genus == null && !analysis.isUnranked() && !analysis.getRank().higherThan(Rank.GENUS) && name.getGenus() != null && !name.getGenus().equals("?"))
            classification.genus = name.getGenus();
        if (classification.cultivarEpithet == null && name.getCultivarEpithet() != null && !name.getCultivarEpithet().equals("?"))
            classification.cultivarEpithet = name.getCultivarEpithet();
        if (classification.canonicalName == null) {
            classification.canonicalName = name.canonicalNameWithoutAuthorship();
        }
    }


    protected void fillOutClassifier(Analysis analysis, Classifier classifier) throws InferenceException, StoreException {
        ParsedName name = analysis.getParsedName();
        if (classifier == null || name == null)
            return;
        if (name.isPhraseName()) {
            if (!classifier.has(AlaLinnaeanFactory.nominatingParty) && name.getNominatingParty() != null) {
                classifier.add(AlaLinnaeanFactory.nominatingParty, name.getNominatingParty(), false, true);
            }
            if (!classifier.has(AlaLinnaeanFactory.voucher) && name.getVoucher() != null) {
                classifier.add(AlaLinnaeanFactory.voucher, AlaLinnaeanFactory.voucher.getAnalysis().analyse(name.getVoucher()), false, true);
            }
            if (!classifier.has(AlaLinnaeanFactory.phraseName) && name.getPhrase() != null) {
                classifier.add(AlaLinnaeanFactory.phraseName, AlaLinnaeanFactory.phraseName.getAnalysis().analyse(name.getPhrase()), false, true);
            }
        }
        if (name.getType() == NameType.SCIENTIFIC || name.getType() == NameType.INFORMAL || name.getType() == NameType.PLACEHOLDER) {
            // Only do this for non-synonyms to avoid dangling speciesID
            if (!classifier.has(AlaLinnaeanFactory.acceptedNameUsageId) && !classifier.has(AlaLinnaeanFactory.specificEpithet)  && name.getSpecificEpithet() != null && !analysis.isUncertain()) {
                classifier.add(AlaLinnaeanFactory.specificEpithet, name.getSpecificEpithet(), false, true);
            }
        }
        if (!classifier.has(AlaLinnaeanFactory.acceptedNameUsageId) && !classifier.has(AlaLinnaeanFactory.genus)  && !analysis.isUnranked() && !analysis.getRank().higherThan(Rank.GENUS) && name.getGenus() != null) {
            classifier.add(AlaLinnaeanFactory.genus, name.getGenus(), false, true);
        }
        if (!classifier.has(AlaLinnaeanFactory.cultivarEpithet) && name.getCultivarEpithet() != null) {
            classifier.add(AlaLinnaeanFactory.cultivarEpithet, name.getCultivarEpithet(), false, true);
        }
    }

    /**
     * Look for a scientific name of the form "Genus A" or "Genus 453"
     *
     * @param analysis The analysis
     * @param classification The classification
     */
    protected void detectNumericPlaceholder(Analysis analysis, AlaLinnaeanClassification classification) {
        Matcher numericPlaceholder = NUMERIC_PLACEHOLDER.matcher(analysis.getScientificName());
        if (numericPlaceholder.matches()) {
            if (classification.genus == null)
                classification.genus = numericPlaceholder.group(1);
            analysis.setNameType(NameType.PLACEHOLDER);
        }
    }

    /**
     * Look for a scientific name of the form "Genus A" or "Genus 453"
     *
     * @param analysis The analysis
     * @param classifier The classification
     */
    protected void detectNumericPlaceholder(Analysis analysis, Classifier classifier) throws StoreException {
        Matcher numericPlaceholder = NUMERIC_PLACEHOLDER.matcher(analysis.getScientificName());
        if (numericPlaceholder.matches()) {
            if (!classifier.has(AlaLinnaeanFactory.genus))
                classifier.add(AlaLinnaeanFactory.genus, numericPlaceholder.group(1), false, true);
            analysis.setNameType(NameType.PLACEHOLDER);
        }
    }

    /**
     * Analyse the information in a classifier and extend the classifier
     * as required for searching.
     *
     * @param classification The classification
     * @param options The match options
     * @throws StoreException if an error occurs updating the classification
     */
    @Override
    public void analyseForSearch(AlaLinnaeanClassification classification, MatchOptions options) throws InferenceException {
        Analysis analysis = new Analysis(
                classification.scientificName,
                classification.scientificNameAuthorship,
                classification.taxonRank,
                classification.nomenclaturalCode,
                options
        );
        boolean phraseLike = false;

        this.removeInvalid(analysis, classification);
        analysis.setKingdom(classification.kingdom);
        this.checkKingdom(analysis, KINGDOM_ISSUES, KINGDOM_MODIFICATION);
        this.inferClassification(analysis, classification);
        this.inferRank(analysis, classification);
        if (this.processCommentary(analysis, DATA_ISSUES, CANONICAL_MODIFICATION)) {
            if (analysis.getScientificName() == null || analysis.getScientificName().isEmpty()) {
                analysis.setScientificName(null);
                this.inferRank(analysis, classification);
            }
        }
        if (analysis.getScientificName() == null)
            return;
        if (MARKER_ONLY.matcher(analysis.getScientificName()).matches())
            throw new InferenceException("Supplied scientific name is a rank marker.");
        this.removeSurroundingQuotes(analysis, DATA_ISSUES, CANONICAL_MODIFICATION);
        this.replaceUnprintable(analysis, 'x', DATA_ISSUES, CANONICAL_MODIFICATION);
        this.processIndeterminate(analysis, " ", INDETERMINATE_ISSUES, CANONICAL_MODIFICATION);
        this.processAffinitySpecies(analysis, " aff. ", AFFINTIY_ISSUES, CANONICAL_MODIFICATION);
        this.processConferSpecies(analysis, " cf. ", CONFER_ISSUES, CANONICAL_MODIFICATION);
        this.processSpeciesNovum(analysis, true, null, CANONICAL_MODIFICATION);
        if (!this.processPhraseName(analysis, null)) {
            phraseLike = this.processPhraseLikeName(analysis, BARE_PHRASE_NAME_ISSUES);
        };
        this.processRankEnding(analysis, true, INDETERMINATE_ISSUES, CANONICAL_MODIFICATION);
        this.processRankMarker(analysis, true, null, CANONICAL_MODIFICATION);
        if (!analysis.isPhraseName())
            this.processEmbeddedAuthor(analysis, null, CANONICAL_MODIFICATION);
        if (!phraseLike) {
            this.parseName(analysis, UNPARSABLE_ISSUES);
            this.processParsedScientificName(analysis, null, CANONICAL_MODIFICATION);
        }
        this.processTrinomial(analysis, CANONICAL_MODIFICATION);
        this.fillOutClassification(analysis, classification);
        this.detectNumericPlaceholder(analysis, classification);
        if (analysis.isPhraseName() && analysis.getParsedName() != null && classification.voucher != null) {
            analysis.setScientificName(this.reducedPhraseName(analysis.getParsedName()));
        }
        classification.scientificName = analysis.getScientificName();
        classification.scientificNameAuthorship = analysis.getScientificNameAuthorship();
        classification.taxonRank = analysis.isUnranked() ? null : analysis.getRank();
        classification.nameType = analysis.getNameType();
        classification.kingdom = analysis.getKingdom();
        classification.addIssues(analysis.getIssues());
        if (classification.scientificNameAuthorship != null && INVALID_AUTHORSHIP.matcher(classification.scientificNameAuthorship).matches()) {
            classification.scientificNameAuthorship = null;
            classification.addIssues(CANONICAL_MODIFICATION);
        }
        if (classification.nomenclaturalCode == null && analysis.getNomenclaturalCode() != null) {
            classification.addHint(AlaLinnaeanFactory.nomenclaturalCode, analysis.getNomenclaturalCode());
        }
    }


    /**
     * Store the analyser configuration, including special cases if required
     *
     * @param directory The directory to write to
     *
     * @return The path to the analyser configuration
     *
     * @throws IOException if unable to write the configuration
     */
    @Override
    public File writeConfiguration(File directory) throws IOException {
        File specialFile = null;
        if (this.special != null) {
            specialFile = new File(directory, SPECIAL_CASE_NAMES);
            if (specialFile.exists())
                throw new FileAlreadyExistsException("Special case file " + specialFile + " already exists");
            FileWriter writer = new FileWriter(specialFile, Charset.forName("UTF-8"));
            this.special.store(writer);
        }
        AnalyserConfig config = AnalyserConfig.builder()
                .build()
                .with(SPECIAL_CASE_KEY, specialFile == null ? null : specialFile.toURI().toURL());
        File configFile = new File(directory, AnalyserConfig.DEFAULT_CONFIG_FILE_NAME);
        if (configFile.exists())
            throw new FileAlreadyExistsException("Configuration " + configFile + " already exists");
        FileWriter writer = new FileWriter(configFile);
        config.relative(directory).store(writer);
        writer.close();
        return configFile;
    }

}
