package au.org.ala.names;

import au.org.ala.bayesian.*;
import au.org.ala.names.lucene.LuceneClassifier;
import au.org.ala.vocab.BayesianTerm;
import org.gbif.api.vocabulary.NomenclaturalCode;
import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.Rank;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.Assert.*;

public class AlaNameAnalyserTest {
    private static final Logger logger = LoggerFactory.getLogger(AlaNameAnalyserTest.class);

    private AlaNameAnalyser analyser;

    @Before
    public void setUp() throws Exception {
        URL specialCases = this.getClass().getResource("name-analyser-special.csv");
        AnalyserConfig config = AnalyserConfig.builder().build().with(ScientificNameAnalyser.SPECIAL_CASE_KEY, specialCases);
        this.analyser = new AlaNameAnalyser(config);
    }

    @Test
    public void testAnalyseForSearch1() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia dealbata";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Acacia", classification.genus);
        assertEquals("dealbata", classification.specificEpithet);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch2() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia dealbata dealbata";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Acacia", classification.genus);
        assertEquals("dealbata", classification.specificEpithet);
        assertEquals(Rank.INFRASPECIFIC_NAME, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    @Test
    public void testAnalyseFoSearch3() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia dealbata dealbata";
        classification.taxonRank = Rank.VARIETY;
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Acacia", classification.genus);
        assertEquals("dealbata", classification.specificEpithet);
        assertEquals(Rank.VARIETY, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch4() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Plantae";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertNull(classification.genus);
        assertNull(classification.specificEpithet);
        assertNull(classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }


    @Test
    public void testAnalyseForSearch5() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "sp.";
        try {
            this.analyser.analyseForSearch(classification, MatchOptions.ALL);
            fail("Expecting inference exception");
        } catch (InferenceException e) {
        }
    }

    @Test
    public void testAnalyseForSearch6() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia dealbata?";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Acacia dealbata", classification.scientificName);
        assertEquals("Acacia", classification.genus);
        assertNull(classification.specificEpithet);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertTrue(classification.getIssues().contains(AlaLinnaeanFactory.INDETERMINATE_NAME));
    }

    @Test
    public void testAnalyseForSearch7() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia aff. dealbata";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Acacia aff. dealbata", classification.scientificName);
        assertEquals("Acacia", classification.genus);
        assertNull(classification.specificEpithet);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.INFORMAL, classification.nameType);
        assertTrue(classification.getIssues().contains(AlaLinnaeanFactory.AFFINITY_SPECIES_NAME));
    }

    @Test
    public void testAnalyseForSearch8() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia cf. dealbata";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Acacia cf. dealbata", classification.scientificName);
        assertEquals("Acacia", classification.genus);
        assertNull(classification.specificEpithet);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.INFORMAL, classification.nameType);
        assertTrue(classification.getIssues().contains(AlaLinnaeanFactory.CONFER_SPECIES_NAME));
    }


    @Test
    public void testAnalyseForSearch9() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia sp. dealbata";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Acacia dealbata", classification.scientificName);
        assertEquals("Acacia", classification.genus);
        assertEquals("dealbata", classification.specificEpithet);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertTrue(classification.getIssues().contains(AlaLinnaeanFactory.CANONICAL_NAME));
    }

    @Test
    public void testAnalyseForSearch10() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Sida sp. Walhallow Station (C.Edgood 28/Oct/94)";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Sida sp. Walhallow Station", classification.scientificName);
        assertEquals("Sida", classification.genus);
        assertEquals("CEDGOOD28OCT94", classification.voucher);
        assertNull(classification.specificEpithet);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.INFORMAL, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch11() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Goodenia sp. Bachsten Creek (M.D.Barrett 685) WA Herbarium";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Goodenia sp. Bachsten Creek", classification.scientificName);
        assertEquals("WA Herbarium", classification.nominatingParty);
        assertEquals("Goodenia", classification.genus);
        assertEquals("BACHSTENCREEK", classification.phraseName);
        assertEquals("MDBARRETT685", classification.voucher);
        assertNull(classification.specificEpithet);
        assertNull(classification.scientificNameAuthorship);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.INFORMAL, classification.nameType);
        assertEquals(Issues.of(AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch12() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Canarium acutifolium var. acutifolium";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Canarium acutifolium var. acutifolium", classification.scientificName);
        assertEquals("Canarium", classification.genus);
        assertEquals("acutifolium", classification.specificEpithet);
        assertEquals(Rank.VARIETY, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch13() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Canarium acutifolium acutifolium";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Canarium acutifolium acutifolium", classification.scientificName);
        assertEquals("Canarium", classification.genus);
        assertEquals("acutifolium", classification.specificEpithet);
        assertEquals(Rank.INFRASPECIFIC_NAME, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    // Ranks
    @Test
    public void testAnalyseForSearch14() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Melanogaster";
        classification.taxonRank = Rank.SERIES;
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Melanogaster", classification.scientificName);
        assertEquals(Rank.SERIES, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    // Author
    @Test
    public void testAnalyseForSearch15() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Canarium longiflorum";
        classification.scientificNameAuthorship = "Zipp.";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Canarium longiflorum", classification.scientificName);
        assertEquals("Zipp.", classification.scientificNameAuthorship);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    // Placeholder species
    @Test
    public void testAnalyseForSearch16() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Astrotricha sp. 1";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Astrotricha sp. 1", classification.scientificName);
        assertEquals(NameType.PLACEHOLDER, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    // Repeated author
    @Test
    public void testAnalyseForSearch17() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Dryopteris rotundata (Willd.) C.Chr.";
        classification.scientificNameAuthorship = "(Willd.) C.Chr.";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Dryopteris rotundata", classification.scientificName);
        assertEquals("(Willd.) C.Chr.", classification.scientificNameAuthorship);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    // Commentary
    @Test
    public void testAnalyseForSearch18() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Aleucosia fulvipes (Unmatched taxon)";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Aleucosia fulvipes", classification.scientificName);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(BayesianTerm.illformedData, AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    // Commentary
    @Test
    public void testAnalyseForSearch19() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.genus = "Aleucosia";
        classification.specificEpithet = "fulvipes";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Aleucosia fulvipes", classification.scientificName);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }


    // Unprintable
    @Test
    public void testAnalyseForSearch20() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "A\ufffdeucosia fulvipes";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Axeucosia fulvipes", classification.scientificName);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(BayesianTerm.illformedData, AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    // Surrounding quotes
    @Test
    public void testAnalyseForSearch21() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "'Aleucosia fulvipes'";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Aleucosia fulvipes", classification.scientificName);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(BayesianTerm.illformedData, AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    // Embedded author
    @Test
    public void testAnalyseForSearch22() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Gymnothorax javanicus (Bleeker, 1859)";
        classification.scientificNameAuthorship = "Bleeker";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Gymnothorax javanicus", classification.scientificName);
        assertEquals("Bleeker", classification.scientificNameAuthorship);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    // Embedded author
    @Test
    public void testAnalyseForSearch23() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Ahamitermes inclusus Gay, 1955";
        classification.scientificNameAuthorship = "Gay";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Ahamitermes inclusus", classification.scientificName);
        assertEquals("Gay", classification.scientificNameAuthorship);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }


    // Nomenclatural code
    @Test
    public void testAnalyseForSearch24() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Ahamitermes inclusus Gay, 1955";
         this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Ahamitermes inclusus", classification.scientificName);
        assertEquals("Gay, 1955", classification.scientificNameAuthorship);
        assertEquals(Issues.of(AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
        List<List<Function<AlaLinnaeanClassification, AlaLinnaeanClassification>>> mods = classification.hintModificationOrder();
        assertNotNull(mods);
        assertEquals(1, mods.size());
        assertEquals(2, mods.get(0).size());
        AlaLinnaeanClassification c1 = mods.get(0).get(1).apply(classification);
        assertEquals(NomenclaturalCode.ZOOLOGICAL, c1.nomenclaturalCode);
    }

    // Nomenclatural code
    @Test
    public void testAnalyseForSearch25() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Ahamitermes inclusus";
        classification.scientificNameAuthorship = "Gay, 1955";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Ahamitermes inclusus", classification.scientificName);
        assertEquals("Gay, 1955", classification.scientificNameAuthorship);
        assertEquals(Issues.of(), classification.getIssues());
        List<List<Function<AlaLinnaeanClassification, AlaLinnaeanClassification>>> mods = classification.hintModificationOrder();
        assertNotNull(mods);
        assertEquals(1, mods.size());
        assertEquals(2, mods.get(0).size());
        AlaLinnaeanClassification c1 = mods.get(0).get(1).apply(classification);
        assertEquals(NomenclaturalCode.ZOOLOGICAL, c1.nomenclaturalCode);
    }

    // Nomenclatural code (cultivar)
    @Test
    public void testAnalyseForSearch26() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Brachychiton 'Coral Beauty'";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Brachychiton 'Coral Beauty'", classification.scientificName);
        assertEquals(null, classification.scientificNameAuthorship);
        assertEquals(null, classification.nomenclaturalCode);
        assertEquals(Issues.of(), classification.getIssues());
    }

    // Placeholder name
    @Test
    public void testAnalyseForSearch27() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Clathria 1";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Clathria 1", classification.scientificName);
        assertEquals("Clathria", classification.genus);
        assertEquals(NameType.PLACEHOLDER, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }


    // Canonical name
    @Test
    public void testAnalyseForSearch28() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia bivenosa DC.";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Acacia bivenosa", classification.scientificName);
        assertEquals("DC.", classification.scientificNameAuthorship);
        assertEquals("Acacia", classification.genus);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    // Squished placeholder
    @Test
    public void testAnalyseForSearch29() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Arabella sp1";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Arabella sp1", classification.scientificName);
        assertEquals("Arabella", classification.genus);
        assertEquals(NameType.PLACEHOLDER, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    // Multiple names
    @Test
    public void testAnalyseForSearch30() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "fam. Physidae gen. Physa";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Physa", classification.scientificName);
        assertEquals("Physa", classification.genus);
        assertEquals("Physidae", classification.family);
        assertEquals(Rank.GENUS, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    // CF/AFF sets genus
    @Test
    public void testAnalyseForSearch31() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Callogobius clitellus cf";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Callogobius clitellus cf.", classification.scientificName);
        assertEquals("Callogobius", classification.genus);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.INFORMAL, classification.nameType);
        assertEquals(Issues.of(AlaLinnaeanFactory.CONFER_SPECIES_NAME, AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    // Invalid kingdom
    @Test
    public void testAnalyseForSearch32() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Symbrenthia";
        classification.kingdom = "InvalidKingdom";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Symbrenthia", classification.scientificName);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(AlaLinnaeanFactory.INVALID_KINGDOM, AlaLinnaeanFactory.REMOVED_KINGDOM), classification.getIssues());
    }


    // Invalid data
    @Test
    public void testAnalyseForSearch33() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Genus sp.";
        classification.kingdom = "Invalid";
        classification.phylum = "Incertae sedis";
        classification.class_ = "Flora";
        classification.order = "Flora";
        classification.family = "Physidae";
        classification.genus = "Genus";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Physidae", classification.scientificName);
        assertNull(classification.kingdom);
        assertNull(classification.phylum);
        assertNull(classification.class_);
        assertNull(classification.order);
        assertEquals("Physidae", classification.family);
        assertNull(classification.genus);
        assertEquals(Issues.of(BayesianTerm.illformedData, AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch34() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia dealbata";
        this.analyser.analyseForSearch(classification, MatchOptions.NONE);
        assertEquals("Acacia", classification.genus);
        assertEquals("dealbata", classification.specificEpithet);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch35() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Sida sp. Walhallow Station (C.Edgood 28/Oct/94)";
        this.analyser.analyseForSearch(classification, MatchOptions.NONE);
        assertEquals("Sida sp. Walhallow Station", classification.scientificName);
        assertEquals("CEDGOOD28OCT94", classification.voucher);
        assertEquals("WALHALLOWSTATION", classification.phraseName);
        assertEquals("Sida", classification.genus);
        assertNull(classification.specificEpithet);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.INFORMAL, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch36() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Canarium longiflorum";
        classification.scientificNameAuthorship = "Zipp.";
        this.analyser.analyseForSearch(classification, MatchOptions.NONE);
        assertEquals("Canarium longiflorum", classification.scientificName);
        assertEquals("Zipp.", classification.scientificNameAuthorship);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch37() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "'Aleucosia fulvipes'";
        this.analyser.analyseForSearch(classification, MatchOptions.NONE);
        assertEquals("'Aleucosia fulvipes'", classification.scientificName);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(BayesianTerm.illformedData), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch38() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "*";
        classification.kingdom = "Animalia";
        classification.class_ = "Malacostraca";
        classification.order = "Decapoda";
        classification.family = "Parthenopidae";
        classification.genus = "Parthenope";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Parthenope", classification.scientificName);
        assertEquals(Rank.GENUS, classification.taxonRank);
        assertEquals(NameType.SCIENTIFIC, classification.nameType);
        assertEquals(Issues.of(BayesianTerm.illformedData, AlaLinnaeanFactory.CANONICAL_NAME), classification.getIssues());
    }

    // Phrase name without voucher
    @Test
    public void testAnalyseForSearch39() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Elaeocarpus sp. Rocky Creek";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Elaeocarpus sp. Rocky Creek", classification.scientificName);
        assertEquals(Rank.SPECIES, classification.taxonRank);
        assertEquals(NameType.PLACEHOLDER, classification.nameType);
        assertEquals(Issues.of(AlaLinnaeanFactory.BARE_PHRASE_NAME), classification.getIssues());
    }

    @Test
    public void testAnalyseForSearch40() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Grevillea brachystylis subsp. Busselton (G.J.Keighery s.n. 28/8/1985)";
        this.analyser.analyseForSearch(classification, MatchOptions.NONE);
        assertEquals("Grevillea brachystylis subsp. Busselton", classification.scientificName);
        assertEquals("GJKEIGHERY2881985", classification.voucher);
        assertEquals("BUSSELTON", classification.phraseName);
        assertEquals("Grevillea", classification.genus);
        assertEquals("brachystylis", classification.specificEpithet);
        assertEquals(Rank.SUBSPECIES, classification.taxonRank);
        assertEquals(NameType.INFORMAL, classification.nameType);
        assertEquals(Issues.of(), classification.getIssues());
    }

}
