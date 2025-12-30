package au.org.ala.names;

import au.org.ala.bayesian.*;
import au.org.ala.names.builder.IndexBuilder;
import au.org.ala.names.builder.IndexBuilderConfiguration;
import au.org.ala.names.builder.LoadStore;
import au.org.ala.names.builder.Source;
import au.org.ala.names.lucene.LuceneClassifier;
import au.org.ala.names.lucene.LuceneClassifierSearcher;
import au.org.ala.util.FileUtils;
import au.org.ala.util.TestUtils;
import org.gbif.dwc.terms.GbifTerm;
import org.junit.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AlaVernacularBuilderTest extends TestUtils {
    private static File work;
    private static File output;
    private static IndexBuilder<AlaVernacularClassification, AlaVernacularInferencer, AlaVernacularFactory, LuceneClassifier> builder;
    private static LoadStore<LuceneClassifier> parameterised;

    private AlaVernacularFactory factory;
    private LuceneClassifierSearcher searcher;
    private ClassificationMatcher<AlaVernacularClassification, AlaVernacularInferencer, AlaVernacularFactory, MatchMeasurement> matcher;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestUtils.assumeNotTravis(); // Avoid very long-running tests that cause travis to fail
        work = FileUtils.makeTmpDir("work");
        output = FileUtils.makeTmpDir("output");
        IndexBuilderConfiguration config = new IndexBuilderConfiguration();
        config.setTypes(Arrays.asList(GbifTerm.VernacularName));
        config.setBuilderClass(AlaVernacularBuilder.class);
        config.setNetwork(AlaVernacularBuilder.class.getResource("/ala-vernacular.json"));
        config.setWork(work);
        config.setFactoryClass(AlaVernacularFactory.class);
        config.setBuildAnalyserClass(AlaVernacularBuildAnalyser.class);
        builder = new IndexBuilder<AlaVernacularClassification, AlaVernacularInferencer, AlaVernacularFactory, LuceneClassifier>(config);
        Source source = Source.create(AlaVernacularBuilderTest.class.getResource("/sample-1.zip"), AlaVernacularFactory.instance(), AlaVernacularFactory.instance().getObservables(), config.getTypes());
        builder.load(source);
        parameterised = builder.build();
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
        this.factory = AlaVernacularFactory.instance();
        this.searcher = new LuceneClassifierSearcher(output, null, this.factory.getIdentifier().get());
        this.matcher = this.factory.createMatcher(this.searcher, null, AnalyserConfig.load(output));
    }

    @After
    public void tearDown() throws Exception {
        if (this.searcher != null)
            this.searcher.close();
    }

    @Test
    public void testLoadBuild1() throws Exception {
        Classifier doc = parameterised.get(GbifTerm.VernacularName, AlaVernacularFactory.nameId, "https://id.biodiversity.org.au/name/apni/447687");
        assertNotNull(doc);
        assertEquals("Hill Sida", doc.get(AlaVernacularFactory.vernacularName));
        doc = parameterised.get(GbifTerm.VernacularName, AlaVernacularFactory.nameId, "urn:lsid:biodiversity.org.au:afd.name:282881");
        assertNotNull(doc);
        assertEquals("Jewel Beetles", doc.get(AlaVernacularFactory.vernacularName));
        assertEquals("en", doc.get(AlaVernacularFactory.language));
        assertNull(doc.get(AlaVernacularFactory.locationId));
        AlaVernacularParameters_ params = new AlaVernacularParameters_();
        doc.loadParameters(params);
        AlaVernacularInferencer_ inference = new AlaVernacularInferencer_();
        AlaVernacularInferencer.Evidence evidence = new AlaVernacularInferencer.Evidence();
        evidence.e$vernacularName = true;
        Inference prob = inference.probability(evidence, params, null);
        assertEquals(1.0, prob.getPosterior(), 0.00001);
        evidence.e$vernacularName = null;
        evidence.e$soundexVernacularName = true;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.00730, prob.getEvidence(), 0.00001);
        assertEquals(1.0, prob.getPosterior(), 0.00001);
    }

    @Test
    public void testLoadBuild2() throws Exception {
        List<LuceneClassifier> docs = parameterised.getAllClassifiers(GbifTerm.VernacularName, new Observation<String>(true, AlaVernacularFactory.nameId, "urn:lsid:biodiversity.org.au:afd.name:247359"));
        assertNotNull(docs);
        assertEquals(2, docs.size());
        Classifier doc = docs.get(0);
        assertEquals("Spiders", doc.get(AlaVernacularFactory.vernacularName));
        AlaVernacularParameters_ params = new AlaVernacularParameters_();
        doc.loadParameters(params);
        AlaVernacularInferencer_ inference = new AlaVernacularInferencer_();
        AlaVernacularInferencer.Evidence evidence = new AlaVernacularInferencer.Evidence();
        evidence.e$vernacularName = true;
        Inference prob = inference.probability(evidence, params, null);
        assertEquals(0.5, prob.getPosterior(), 0.00001);
    }


    @Test
    public void testSearch1() throws Exception {
        AlaVernacularClassification classification = new AlaVernacularClassification();
        classification.vernacularName = "Australian hollyhock";
        List<LuceneClassifier> classifiers = this.searcher.search(classification);
        assertNotNull(classifiers);
        assertEquals(2, classifiers.size());
        assertEquals("https://id.biodiversity.org.au/node/apni/2902835", classifiers.get(0).get(AlaVernacularFactory.taxonId));
    }

    @Test
    public void testMatch1() throws Exception {
        AlaVernacularClassification classification = new AlaVernacularClassification();
        classification.vernacularName = "Flood Mallow";
        Match<AlaVernacularClassification, MatchMeasurement> match = matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals("https://id.biodiversity.org.au/node/apni/2902835", match.getMatch().taxonId);
        assertEquals(0.07299, match.getProbability().getEvidence(), 0.00001);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.00001);
        classification.vernacularName = "flood mallow";
        match = matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals("https://id.biodiversity.org.au/node/apni/2902835", match.getMatch().taxonId);
        assertEquals(0.07299, match.getProbability().getEvidence(), 0.00001);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.00001);
    }
}
