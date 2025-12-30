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

public class AlaNameBuildAnalyserTest {
    private static final Logger logger = LoggerFactory.getLogger(AlaNameBuildAnalyserTest.class);

    private AlaNameBuildAnalyser analyser;

    @Before
    public void setUp() throws Exception {
        URL specialCases = this.getClass().getResource("name-analyser-special.csv");
        AnalyserConfig config = AnalyserConfig.builder().build().with(ScientificNameAnalyser.SPECIAL_CASE_KEY, specialCases);
        this.analyser = new AlaNameBuildAnalyser(config);
    }

    @Test
    public void testAnalyseForIndex1() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia dealbata", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Acacia", classifier.get(AlaLinnaeanFactory.genus));
        assertEquals("dealbata", classifier.get(AlaLinnaeanFactory.specificEpithet));
        assertEquals(Rank.SPECIES, classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.SCIENTIFIC, classifier.get(AlaLinnaeanFactory.nameType));
    }

    @Test
    public void testAnalyseForIndex2() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia dealbata dealbata", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Acacia", classifier.get(AlaLinnaeanFactory.genus));
        assertEquals("dealbata", classifier.get(AlaLinnaeanFactory.specificEpithet));
        assertEquals(Rank.INFRASPECIFIC_NAME, classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.SCIENTIFIC, classifier.get(AlaLinnaeanFactory.nameType));
    }

    @Test
    public void testAnalyseFoIndex3() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia dealbata dealbata", false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.VARIETY, false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Acacia", classifier.get(AlaLinnaeanFactory.genus));
        assertEquals("dealbata", classifier.get(AlaLinnaeanFactory.specificEpithet));
        assertEquals(Rank.VARIETY, classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.SCIENTIFIC, classifier.get(AlaLinnaeanFactory.nameType));
    }

    @Test
    public void testAnalyseForIndex4() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Plantae", false, false);
        this.analyser.analyseForIndex(classifier);
        assertNull(classifier.get(AlaLinnaeanFactory.genus));
        assertNull(classifier.get(AlaLinnaeanFactory.specificEpithet));
        assertNull(classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.SCIENTIFIC, classifier.get(AlaLinnaeanFactory.nameType));
     }

    @Test
    public void testAnalyseForIndex5() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia sp.", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Acacia sp.", classifier.get(AlaLinnaeanFactory.scientificName));
        assertEquals("Acacia", classifier.get(AlaLinnaeanFactory.genus));
        assertNull(classifier.get(AlaLinnaeanFactory.specificEpithet));
        assertEquals(Rank.SPECIES, classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.INFORMAL, classifier.get(AlaLinnaeanFactory.nameType));
     }

    @Test
    public void testAnalyseForIndex6() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia dealbata?", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Acacia dealbata?", classifier.get(AlaLinnaeanFactory.scientificName));
        assertEquals("Acacia", classifier.get(AlaLinnaeanFactory.genus));
        assertNull(classifier.get(AlaLinnaeanFactory.specificEpithet));
        assertEquals(Rank.SPECIES, classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.INFORMAL, classifier.get(AlaLinnaeanFactory.nameType));
    }

    // Separate specific and cultivar epithet
    @Test
    public void testAnalyseForIndex7() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Conospermum taxifolium 'Tasmanian form'", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Conospermum taxifolium 'Tasmanian form'", classifier.get(AlaLinnaeanFactory.scientificName));
        assertEquals("Conospermum", classifier.get(AlaLinnaeanFactory.genus));
        assertEquals("taxifolium", classifier.get(AlaLinnaeanFactory.specificEpithet));
        assertEquals("Tasmanian form", classifier.get(AlaLinnaeanFactory.cultivarEpithet));
        assertEquals(Rank.CULTIVAR, classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.SCIENTIFIC, classifier.get(AlaLinnaeanFactory.nameType));
    }

    // Separate specific and cultivar epithet
    @Test
    public void testAnalyseForIndex8() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Conospermum taxifolium 'Tasmanian form'", false, false);
        classifier.add(AlaLinnaeanFactory.genus, "Conospermum", false, false);
        classifier.add(AlaLinnaeanFactory.specificEpithet, "taxifolium 'Tasmanian form'", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Conospermum taxifolium 'Tasmanian form'", classifier.get(AlaLinnaeanFactory.scientificName));
        assertEquals("Conospermum", classifier.get(AlaLinnaeanFactory.genus));
        assertEquals("taxifolium", classifier.get(AlaLinnaeanFactory.specificEpithet));
        assertEquals("Tasmanian form", classifier.get(AlaLinnaeanFactory.cultivarEpithet));
        assertEquals(Rank.CULTIVAR, classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.SCIENTIFIC, classifier.get(AlaLinnaeanFactory.nameType));
    }

    // Ranks
    @Test
    public void testAnalyseForIndex9() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Melanogaster", false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.SERIES, false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Melanogaster", classifier.get(AlaLinnaeanFactory.scientificName));
        assertEquals(Rank.SERIES, classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.SCIENTIFIC, classifier.get(AlaLinnaeanFactory.nameType));
    }
    @Test
    public void testAnalyseForIndex11() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Astrotricha sp. 1", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Astrotricha sp. 1", classifier.get(AlaLinnaeanFactory.scientificName));
        assertEquals(Rank.SPECIES, classifier.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(NameType.PLACEHOLDER, classifier.get(AlaLinnaeanFactory.nameType));
    }

    // Repeated author
    @Test
    public void testAnalyseForIndex12() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Dryopteris rotundata (Willd.) C.Chr.", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "(Willd.) C.Chr.", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Dryopteris rotundata", classifier.get(AlaLinnaeanFactory.scientificName));
        assertEquals("(Willd.) C.Chr.", classifier.get(AlaLinnaeanFactory.scientificNameAuthorship));
        assertEquals(NameType.SCIENTIFIC, classifier.get(AlaLinnaeanFactory.nameType));
    }


    // Unprintable
    @Test(expected = InferenceException.class)
    public void testAnalyseForIndex13() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Dryopteris r\ufffdtundata", false, false);
        this.analyser.analyseForIndex(classifier);
    }

    // Placeholder
    @Test
    public void testAnalyseForIndex14() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Arabella sp1", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Arabella sp1", classifier.get(AlaLinnaeanFactory.scientificName));
        assertEquals(NameType.PLACEHOLDER, classifier.get(AlaLinnaeanFactory.nameType));
    }

    // Invalid kingdom
    @Test
    public void testAnalyseForIndex15() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Symbrenthia", false, false);
        classifier.add(AlaLinnaeanFactory.kingdom, "InvalidKingdom", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Symbrenthia", classifier.get(AlaLinnaeanFactory.scientificName));
        assertEquals(NameType.SCIENTIFIC, classifier.get(AlaLinnaeanFactory.nameType));
    }

    @Test
    public void testAnalyseNames1() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia dealbata", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "Link", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.SPECIES, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), false);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Acacia dealbata"));
        assertTrue(names.contains("Acacia dealbata Link"));
    }

    @Test
    public void testAnalyseNames2() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia dealbata", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "Link", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), false);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Acacia dealbata"));
        assertTrue(names.contains("Acacia dealbata Link"));
    }


    // Normaliser gets rid of accents.
    @Test
    public void testAnalyseNames3() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acäcia dealbata", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "Link", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.SPECIES, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), false);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Acacia dealbata"));
        assertTrue(names.contains("Acacia dealbata Link"));
        assertEquals("Acacia dealbata", classifier.get(AlaLinnaeanFactory.scientificName));
    }

    @Test
    public void testAnalyseNames4() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Goodenia ser. Bracteolatae", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "Benth.", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.SERIES, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), false);
        assertNotNull(names);
        assertTrue(names.contains("Goodenia ser. Bracteolatae"));
        assertTrue(names.contains("Goodenia Bracteolatae"));
        assertTrue(names.contains("Goodenia ser. Bracteolatae Benth."));
        assertTrue(names.contains("Bracteolatae"));
    }

    @Test
    public void testAnalyseNames5() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia dealbata Link dealbata", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.SUBSPECIES, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), false);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Acacia dealbata Link dealbata"));
        assertTrue(names.contains("Acacia dealbata subsp. dealbata"));
        assertTrue(names.contains("Acacia dealbata dealbata"));
    }

    @Test
    public void testAnalyseNames6() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia sp. Bigge Island (A.A. Mitchell 3436)", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Acacia sp. Bigge Island (A.A. Mitchell 3436)"));
        assertTrue(names.contains("Acacia sp. Bigge Island"));
    }


    @Test
    public void testAnalyseNames7() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia 'Morning Glory'", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), false);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Acacia 'Morning Glory'"));
    }

    @Test
    public void testAnalyseNames8() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia dealbata", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "Link", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.SPECIES, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Acacia dealbata"));
    }


    @Test
    public void testAnalyseNames9() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia sect. Acacia", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "Mill.", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Acacia sect. Acacia"));
        assertTrue(names.contains("Acacia Acacia"));
    }


    @Test
    public void testAnalyseNames10() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Goodenia subsect. Bracteolatae", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "(Benth.) K.Krause", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Goodenia subsect. Bracteolatae"));
        assertTrue(names.contains("Goodenia Bracteolatae"));
        assertTrue(names.contains("Bracteolatae"));
    }

    @Test
    public void testAnalyseNames11() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia 'H.L.White'", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.UNRANKED, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Acacia 'H.L.White'"));
    }

    @Test
    public void testAnalyseNames12() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia $ Brunioideae", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.INFRAGENUS, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Acacia $ Brunioideae"));
    }

    @Test
    public void testAnalyseNames13() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia Div. II Bipinnatae", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.INFRAGENUS, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Acacia Div. II Bipinnatae"));
    }

    @Test
    public void testAnalyseNames14() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia sp. laterite", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.UNRANKED, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Acacia sp. laterite"));
        assertTrue(names.contains("Acacia laterite"));
    }

    @Test
    public void testAnalyseNames15() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia sp. holey trunk", false, false);
        classifier.add(AlaLinnaeanFactory.nomenclaturalCode, NomenclaturalCode.BOTANICAL, false, false);
        classifier.add(AlaLinnaeanFactory.taxonRank, Rank.UNRANKED, false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Acacia sp. holey trunk"));
        assertTrue(names.contains("Acacia holey trunk"));
        assertTrue(names.contains("Acacia holey subsp. trunk"));
    }

    @Test
    public void testAnalyseNames16() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Grevillea brachystylis subsp. Busselton (G.J.Keighery s.n. 28/8/1985)", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Grevillea brachystylis subsp. Busselton (G.J.Keighery s.n. 28/8/1985)"));
        assertTrue(names.contains("Grevillea brachystylis ssp. Busselton (G.J.Keighery s.n. 28/8/1985)"));
        assertTrue(names.contains("Grevillea brachystylis subsp. Busselton"));
    }

    @Test
    public void testAnalyseNames17() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acianthus sp. 'Gibraltar Range'", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Acianthus sp. 'Gibraltar Range'"));
        assertTrue(names.contains("Acianthus 'Gibraltar Range'"));
    }

    @Test
    public void testAnalyseNames18() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Grevillea sp. aff. patulifolia 'Kanangra'", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Grevillea sp. aff. patulifolia 'Kanangra'"));
        assertTrue(names.contains("Grevillea aff. patulifolia 'Kanangra'"));
    }

    @Test
    public void testAnalyseNames19() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Grevillea sp. nov. 'Belowra'", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Grevillea sp. nov. 'Belowra'"));
    }

    @Test
    public void testAnalyseNames20() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Arthrinium aff. amnium 'Coastal'", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Arthrinium aff. amnium 'Coastal'"));
    }


    @Test
    public void testAnalyseNames21() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Oenochrominae s. str.", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Oenochrominae s. str."));
        assertTrue(names.contains("Oenochrominae"));
    }


    @Test
    public void testAnalyseNames22() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Canarium acutifolium var. acutifolium", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Canarium acutifolium var. acutifolium"));
        assertTrue(names.contains("Canarium acutifolium acutifolium"));
    }

    @Test
    public void testAnalyseNames23() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Phasma (Bacteria) spinosum", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Phasma (Bacteria) spinosum"));
        assertTrue(names.contains("Phasma spinosum"));
    }

    @Test
    public void testAnalyseNames24() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Acacia subsect. Capitatae-Racemosae", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Acacia subsect. Capitatae-Racemosae"));
    }

    @Test
    public void testAnalyseNames25() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Astrotricha sp. 1", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Astrotricha sp. 1"));
    }

    @Test
    public void testAnalyseNames26() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Dryopteris rotundata (Willd.) C.Chr.", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "(Willd.) C.Chr.", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Dryopteris rotundata (Willd.) C.Chr."));
        assertTrue(names.contains("Dryopteris rotundata"));
    }

    // author-like specific epithet
    @Test
    public void testAnalyseNames27() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Polynema filius", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Polynema filius"));
    }


    @Test
    public void testAnalyseNames28() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Arabella sp1", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("Arabella sp1"));
    }

    @Test
    public void testAnalyseNames29() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Eucalyptus subser. Corymbosae", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), true);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Eucalyptus subser. Corymbosae"));
        assertTrue(names.contains("Eucalyptus Corymbosae"));
        assertTrue(names.contains("Corymbosae"));
    }

    @Test
    public void testAnalyseNames30() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.scientificName, "Eucalyptus subser. Corymbosae", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "Benth.", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.empty(), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), false);
        assertNotNull(names);
        assertEquals(4, names.size());
        assertTrue(names.contains("Eucalyptus subser. Corymbosae Benth."));
        assertTrue(names.contains("Eucalyptus subser. Corymbosae"));
        assertTrue(names.contains("Eucalyptus Corymbosae"));
        assertTrue(names.contains("Corymbosae"));
    }

    @Test
    public void testAnalyseNames31() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.nameComplete, "Goodenia sp. Bachsten Creek (M.D.Barrett 685) WA Herbarium", false, false);
        classifier.add(AlaLinnaeanFactory.scientificName, "Goodenia sp. Bachsten Creek (M.D.Barrett 685)", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "WA Herbarium", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.of(AlaLinnaeanFactory.nameComplete), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), false);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Goodenia sp. Bachsten Creek (M.D.Barrett 685)"));
        assertTrue(names.contains("Goodenia sp. Bachsten Creek"));
        assertTrue(names.contains("Goodenia sp. Bachsten Creek (M.D.Barrett 685) WA Herbarium"));
    }

    // Unconventional capitalisation stops detection of phrase name
    @Test
    public void testAnalyseNames32() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaLinnaeanFactory.nameComplete, "Tephrosia rosea var. Fortescue creeks (M.I.H. Brooker 2186) Qld Herbarium", false, false);
        classifier.add(AlaLinnaeanFactory.scientificName, "Tephrosia rosea var. Fortescue creeks (M.I.H. Brooker 2186)", false, false);
        classifier.add(AlaLinnaeanFactory.scientificNameAuthorship, "Qld Herbarium", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaLinnaeanFactory.scientificName, Optional.of(AlaLinnaeanFactory.nameComplete), Optional.of(AlaLinnaeanFactory.scientificNameAuthorship), false);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Tephrosia rosea var. Fortescue creeks (M.I.H. Brooker 2186) Qld Herbarium"));
        assertTrue(names.contains("Tephrosia rosea var. Fortescue creeks (M.I.H. Brooker 2186)"));
        assertTrue(names.contains("Tephrosia rosea var. Fortescue creeks"));
    }

}
