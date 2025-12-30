package au.org.ala.names;

import au.org.ala.bayesian.*;
import au.org.ala.location.AlaLocationBuildAnalyser;
import au.org.ala.names.builder.IndexBuilder;
import au.org.ala.names.builder.IndexBuilderConfiguration;
import au.org.ala.names.builder.LoadStore;
import au.org.ala.names.builder.Source;
import au.org.ala.names.lucene.LuceneClassifier;
import au.org.ala.names.lucene.LuceneClassifierSearcher;
import au.org.ala.util.FileUtils;
import au.org.ala.util.TestUtils;
import au.org.ala.vocab.TaxonomicStatus;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.nameparser.api.Rank;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test using a large (20000 record) dataset
 */
public class AlaLinnaeanBuilderLargeTest extends TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(AlaLinnaeanBuilderLargeTest.class);

    private static File work;
    private static File output;
    private static IndexBuilder builder;
    private static LoadStore parameterised;

    private AlaLinnaeanFactory factory;
    private LuceneClassifierSearcher searcher;
    private ClassificationMatcher<AlaLinnaeanClassification, AlaLinnaeanInferencer, AlaLinnaeanFactory, MatchMeasurement> matcher;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestUtils.assumeNotTravis(); // Avoid very long-running tests that cause travis to fail
        work = FileUtils.makeTmpDir("work");
        output = FileUtils.makeTmpDir("output");
        IndexBuilderConfiguration config = new IndexBuilderConfiguration();
        config.setBuilderClass(AlaLinnaeanBuilder.class);
        config.setNetwork(AlaLinnaeanBuilder.class.getResource("/ala-linnaean.json"));
        config.setWork(work);
        config.setFactoryClass(AlaLinnaeanFactory.class);
        config.setWeightAnalyserClass(AlaWeightAnalyser.class);
        config.setBuildAnalyserClass(AlaNameBuildAnalyser.class);
        builder = new IndexBuilder(config);
        Source source = Source.create(AlaLinnaeanBuilderLargeTest.class.getResource("/sample-2.zip"), AlaLinnaeanFactory.instance(), AlaLinnaeanFactory.instance().getObservables(), config.getTypes());
        builder.load(source);
        long buildStart = System.currentTimeMillis();
        parameterised = builder.build();
        logger.info("Parameterised build took " + (System.currentTimeMillis() - buildStart) + " ms");
        builder.buildIndex(output, parameterised);
   }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (builder != null)
            builder.close();
        if (output != null)
            FileUtils.deleteAll(output);
        if (work != null)
            FileUtils.deleteAll(work);
    }

    @Before
    public void setUp() throws Exception {
        this.factory = AlaLinnaeanFactory.instance();
        this.searcher = new LuceneClassifierSearcher(this.output, null, this.factory.getIdentifier().get());
        this.matcher = this.factory.createMatcher(this.searcher, null, AnalyserConfig.load(this.output));
    }

    @After
    public void tearDown() throws Exception {
        if (this.searcher != null)
            this.searcher.close();
    }

    @Test
    public void testLoadBuild1() throws Exception {
        Classifier doc = parameterised.get(DwcTerm.Taxon, AlaLinnaeanFactory.taxonId, "https://id.biodiversity.org.au/taxon/fungi/60082056");
        assertNotNull(doc);
        assertEquals("FFFFFFT", doc.getSignature());
        assertEquals("Fungi", doc.get(AlaLinnaeanFactory.scientificName));
        doc = parameterised.get(DwcTerm.Taxon, AlaLinnaeanFactory.taxonId, "https://id.biodiversity.org.au/node/apni/2917630");
        assertNotNull(doc);
        assertEquals("TTTTTTT", doc.getSignature());
        assertEquals("Melaleuca stipitata", doc.get(AlaLinnaeanFactory.scientificName));
        assertEquals(new LinkedHashSet<String>(Arrays.asList("Melaleuca stipitata")), doc.getAll(AlaLinnaeanFactory.scientificName));
        assertEquals(Rank.SPECIES, doc.get(AlaLinnaeanFactory.taxonRank));
        assertEquals("Craven", doc.get(AlaLinnaeanFactory.scientificNameAuthorship));
        assertEquals("Craven", doc.get(AlaLinnaeanFactory.canonicalAuthorship));
        assertEquals("MILALICA STIPITATA", doc.get(AlaLinnaeanFactory.soundexScientificName));
        assertEquals("Melaleuca", doc.get(AlaLinnaeanFactory.genus));
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51376808", doc.get(AlaLinnaeanFactory.parentNameUsageId));
        assertNull(doc.get(AlaLinnaeanFactory.vernacularName));
        assertEquals(TaxonomicStatus.accepted, doc.get(AlaLinnaeanFactory.taxonomicStatus));
        assertEquals("Myrtaceae", doc.get(AlaLinnaeanFactory.family));
        assertEquals(new LinkedHashSet<String>(Arrays.asList("Myrtaceae")), doc.getAll(AlaLinnaeanFactory.family));
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51376810", doc.get(AlaLinnaeanFactory.familyId));
        assertEquals("Myrtales", doc.get(AlaLinnaeanFactory.order));
        assertEquals(new LinkedHashSet<String>(Arrays.asList("Myrtales", "Rosanae", "Rosanae Takht.")), doc.getAll(AlaLinnaeanFactory.order));
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51376809", doc.get(AlaLinnaeanFactory.orderId));
        assertEquals("Equisetopsida", doc.get(AlaLinnaeanFactory.class_));
        assertEquals(new LinkedHashSet<String>(Arrays.asList("Equisetopsida","Ophioglossidae","Ophioglossidae Klinge","Psilotidae","Psilotidae Reveal","Marattiidae","Marattiidae Klinge","Cycadidae","Cycadidae Pax","Polypodiidae","Polypodiidae Cronquist, Takht. & W.Zimm.","Lycopodiidae","Lycopodiidae Bek.","Pinidae","Pinidae Cronquist, Takht. & W.Zimm.","Magnoliidae","Magnoliidae Novak ex Takht.")), doc.getAll(AlaLinnaeanFactory.class_));
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51414457", doc.get(AlaLinnaeanFactory.classId));
        assertEquals("Charophyta", doc.get(AlaLinnaeanFactory.phylum));
        assertEquals(new LinkedHashSet<String>(Arrays.asList("Charophyta")), doc.getAll(AlaLinnaeanFactory.phylum));
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51414458", doc.get(AlaLinnaeanFactory.phylumId));
        assertEquals("Plantae", doc.get(AlaLinnaeanFactory.kingdom));
        assertEquals(new LinkedHashSet<String>(Arrays.asList("Plantae", "Biliphyta", "Viridiplantae")), doc.getAll(AlaLinnaeanFactory.kingdom));
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51414459", doc.get(AlaLinnaeanFactory.kingdomId));
        assertEquals(Arrays.asList("Melaleuca stipitata"), doc.getNames());
        assertEquals(new LinkedHashSet<String>(Arrays.asList("Melaleuca stipitata Craven")), doc.getAll(AlaLinnaeanFactory.altScientificName));
        AlaLinnaeanParameters_TTTTTTT params = new AlaLinnaeanParameters_TTTTTTT();
        doc.loadParameters(params);
        AlaLinnaeanInferencer_TTTTTTT inference = new AlaLinnaeanInferencer_TTTTTTT();
        AlaLinnaeanInferencer.Evidence evidence = new AlaLinnaeanInferencer.Evidence();
        evidence.e$scientificName = true;
        Inference prob = inference.probability(evidence, params, null);
        assertEquals(1.0, prob.getPosterior(), 0.00001);
        evidence.e$genus = false;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.00097, prob.getPosterior(), 0.00001);
        evidence.e$soundexGenus = true;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.0, prob.getPosterior(), 0.00001); // Zero because genus is still false see modifiers
        evidence.e$soundexGenus = false;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.00097, prob.getPosterior(), 0.00001);
        evidence.e$scientificName = false;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.0, prob.getPosterior(), 0.00001);
        evidence.e$scientificName = true;
        evidence.e$genus = true;
        evidence.e$soundexGenus = true;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.00024, prob.getEvidence(), 0.00001);
        assertEquals(1.0, prob.getPosterior(), 0.00001);
    }


    @Test
    public void testLoadBuild2() throws Exception {
        Classifier doc = parameterised.get(DwcTerm.Taxon, AlaLinnaeanFactory.taxonId, "https://biodiversity.org.au/afd/taxa/daae6012-be30-4777-8b17-5c066774ed54");
        assertNotNull(doc);
        assertEquals("FFTTTTT", doc.getSignature()); // ie no specific epithet or genus from copy
        assertEquals("Decilaus sobrinus", doc.get(AlaLinnaeanFactory.scientificName));
        assertEquals("DICILAIS SABRINA", doc.get(AlaLinnaeanFactory.soundexScientificName));
        assertEquals("Lea, 1908", doc.get(AlaLinnaeanFactory.scientificNameAuthorship));
        assertNull(doc.get(AlaLinnaeanFactory.vernacularName));
        assertEquals("https://biodiversity.org.au/afd/taxa/0c94c0d0-55dc-4f5b-8aac-69bbb382ffd2", doc.get(AlaLinnaeanFactory.acceptedNameUsageId));
        assertNull(doc.get(AlaLinnaeanFactory.genus));
        assertEquals(TaxonomicStatus.subjectiveSynonym, doc.get(AlaLinnaeanFactory.taxonomicStatus));
        assertNull(doc.get(AlaLinnaeanFactory.genusId));
        assertNull(doc.get(AlaLinnaeanFactory.familyId));
        assertNull(doc.get(AlaLinnaeanFactory.orderId));
        assertNull(doc.get(AlaLinnaeanFactory.classId));
        assertNull(doc.get(AlaLinnaeanFactory.phylumId));
        assertEquals("Animalia", doc.get(AlaLinnaeanFactory.kingdom));
        assertEquals("https://biodiversity.org.au/afd/taxa/4647863b-760d-4b59-aaa1-502c8cdf8d3c", doc.get(AlaLinnaeanFactory.kingdomId));
        assertEquals(TaxonomicStatus.subjectiveSynonym, doc.get(AlaLinnaeanFactory.taxonomicStatus));
        AlaLinnaeanParameters_FFTTTTT params = new AlaLinnaeanParameters_FFTTTTT();
        doc.loadParameters(params);
        assertEquals(1.0, params.inf_kingdom_t$t_t, 0.00001);
        AlaLinnaeanInferencer_FFTTTTT inference = new AlaLinnaeanInferencer_FFTTTTT();
        AlaLinnaeanInferencer.Evidence evidence = new AlaLinnaeanInferencer.Evidence();
        evidence.e$scientificName = true;
        Inference prob = inference.probability(evidence, params, null);
        assertEquals(0.09091, prob.getPosterior(), 0.00001);
        evidence.e$kingdom = false;
        evidence.e$soundexKingdom = false;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.00009, prob.getEvidence(), 0.00001);
        assertEquals(0.0, prob.getConditional(), 0.00001);
        assertEquals(0.0, prob.getPosterior(), 0.00001);
        evidence.e$kingdom = true;
        evidence.e$soundexKingdom = true;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.00017, prob.getEvidence(), 0.00001);
        assertEquals(1.0, prob.getConditional(), 0.00001);
        assertEquals(0.14060, prob.getPosterior(), 0.00001);
    }


    @Test
    public void testLoadBuild3() throws Exception {
        Classifier doc = parameterised.get(DwcTerm.Taxon, AlaLinnaeanFactory.taxonId, "NZOR-6-50008");
        assertNotNull(doc);
        assertEquals("FFTTTTT", doc.getSignature());
        assertEquals("Xestoleberididae", doc.get(AlaLinnaeanFactory.scientificName));
        assertEquals("ZISTALIBIRIDIDI", doc.get(AlaLinnaeanFactory.soundexScientificName));
        assertEquals(TaxonomicStatus.accepted, doc.get(AlaLinnaeanFactory.taxonomicStatus));
        assertEquals("Animalia", doc.get(AlaLinnaeanFactory.kingdom));
        AlaLinnaeanParameters_FFTTTTT params = new AlaLinnaeanParameters_FFTTTTT();
        doc.loadParameters(params);
        assertEquals(1.0, params.inf_kingdom_t$t_t, 0.00001);
        AlaLinnaeanInferencer_FFTTTTT inference = new AlaLinnaeanInferencer_FFTTTTT();
        AlaLinnaeanInferencer.Evidence evidence = new AlaLinnaeanInferencer.Evidence();
        evidence.e$scientificName = true;
        evidence.e$soundexScientificName = true;
        Inference prob = inference.probability(evidence, params, null);
        assertEquals(0.48621, prob.getPosterior(), 0.00001);
        evidence.e$kingdom = false;
        evidence.e$soundexKingdom = false;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.00002, prob.getEvidence(), 0.00001);
        assertEquals(0.0, prob.getConditional(), 0.00001);
        assertEquals(0.0, prob.getPosterior(), 0.00001);
    }

    @Test
    public void testLoadBuild4() throws Exception {
        Classifier doc = parameterised.get(DwcTerm.Taxon, AlaLinnaeanFactory.taxonId, "https://biodiversity.org.au/afd/taxa/da002998-b551-4328-ac4e-5e04fc72708b");
        assertNotNull(doc);
        assertEquals("TTTTTTT", doc.getSignature());
        assertEquals("Phylidonyris (Meliornis) novaehollandiae", doc.get(AlaLinnaeanFactory.scientificName));
        assertEquals("PILIDANIRIS MILIARNA NAVIALANDI", doc.get(AlaLinnaeanFactory.soundexScientificName));
        assertEquals("(Latham, 1790)", doc.get(AlaLinnaeanFactory.scientificNameAuthorship));
        assertEquals("Latham 1790", doc.get(AlaLinnaeanFactory.canonicalAuthorship));
        assertEquals("New Holland Honeyeater", doc.get(AlaLinnaeanFactory.vernacularName));
        assertEquals(TaxonomicStatus.accepted, doc.get(AlaLinnaeanFactory.taxonomicStatus));
    }

    @Test
    public void testLoadBuild5() throws Exception {
        Classifier doc = parameterised.get(DwcTerm.Taxon, AlaLinnaeanFactory.taxonId, "https://id.biodiversity.org.au/name/apni/70666");
        assertNotNull(doc);
        assertEquals("FTTTTTT", doc.getSignature());
        assertEquals("Dolicholus sect. Rhynchosia", doc.get(AlaLinnaeanFactory.scientificName));
        assertEquals(new HashSet<>(Arrays.asList("Dolicholus sect. Rhynchosia", "Dolicholus Rhynchosia", "Rhynchosia")), doc.getAll(AlaLinnaeanFactory.scientificName));
        assertEquals("DALICALIS RINCASA", doc.get(AlaLinnaeanFactory.soundexScientificName));
        assertEquals(new HashSet<>(Arrays.asList("DALICALIS RINCASA", "RINCASA")), doc.getAll(AlaLinnaeanFactory.soundexScientificName));
        assertEquals("(Lour.) Kuntze", doc.get(AlaLinnaeanFactory.scientificNameAuthorship));
        assertEquals("Kuntze", doc.get(AlaLinnaeanFactory.canonicalAuthorship));
        assertEquals(TaxonomicStatus.inferredAccepted, doc.get(AlaLinnaeanFactory.taxonomicStatus));
        assertEquals("Plantae", doc.get(AlaLinnaeanFactory.kingdom));
        assertEquals(Rank.SECTION_BOTANY, doc.get(AlaLinnaeanFactory.taxonRank));
        assertEquals(6300, doc.get(AlaLinnaeanFactory.rankId).intValue());
    }

    @Test
    public void testSearch1() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Amphileptus rotundus";
        List<LuceneClassifier> classifiers = this.searcher.search(classification);
        assertNotNull(classifiers);
        assertEquals(1, classifiers.size());
        assertEquals("9f8111dcc72d108b8306d92d63bfd511", classifiers.get(0).get(AlaLinnaeanFactory.taxonId));
    }

    @Test
    public void testMatch1() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia congesta wonganensis";
        Match<AlaLinnaeanClassification, MatchMeasurement> match = matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals("https://id.biodiversity.org.au/node/apni/2888346", match.getMatch().taxonId);
        assertEquals(0.0000121, match.getProbability().getEvidence(), 0.0000001);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.00001);
        classification.scientificName = "Acacia congesta subsp. wonganensis";
        match = matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals("https://id.biodiversity.org.au/node/apni/2888346", match.getMatch().taxonId);
        assertEquals(0.0000121, match.getProbability().getEvidence(), 0.0000001);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.00001);
    }

}
