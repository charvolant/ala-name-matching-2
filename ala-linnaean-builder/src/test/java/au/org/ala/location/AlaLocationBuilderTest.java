package au.org.ala.location;

import au.org.ala.bayesian.*;
import au.org.ala.names.builder.IndexBuilder;
import au.org.ala.names.builder.IndexBuilderConfiguration;
import au.org.ala.names.builder.LoadStore;
import au.org.ala.names.builder.Source;
import au.org.ala.names.lucene.LuceneClassifier;
import au.org.ala.names.lucene.LuceneClassifierSearcher;
import au.org.ala.util.FileUtils;
import au.org.ala.util.TestUtils;
import org.junit.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AlaLocationBuilderTest extends TestUtils {
    private static File work;
    private static File output;
    private static IndexBuilder<AlaLocationClassification, AlaLocationInferencer, AlaLocationFactory, LuceneClassifier> builder;
    private static LoadStore<LuceneClassifier> parameterised;

    private AlaLocationFactory factory;
    private LuceneClassifierSearcher searcher;
    private ClassificationMatcher<AlaLocationClassification, AlaLocationInferencer, AlaLocationFactory, MatchMeasurement> matcher;

    @BeforeClass
    public static void setUpClass() throws Exception {
        work = FileUtils.makeTmpDir("work");
        output = FileUtils.makeTmpDir("output");
        IndexBuilderConfiguration config = new IndexBuilderConfiguration();
        config.setTypes(Arrays.asList(AlaLocationFactory.CONCEPT));
        config.setBuilderClass(AlaLocationBuilder.class);
        config.setNetwork(AlaLocationBuilder.class.getResource("/ala-location.json"));
        config.setWork(work);
        config.setFactoryClass(AlaLocationFactory.class);
        config.setBuildAnalyserClass(AlaLocationBuildAnalyser.class);
        builder = new IndexBuilder<AlaLocationClassification, AlaLocationInferencer, AlaLocationFactory, LuceneClassifier>(config);
        Source source = Source.create(AlaLocationBuilderTest.class.getResource("/location-1.zip"), AlaLocationFactory.instance(), AlaLocationFactory.instance().getObservables(), config.getTypes());
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
        this.factory = AlaLocationFactory.instance();
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
        Classifier doc = parameterised.get(AlaLocationFactory.CONCEPT, AlaLocationFactory.locationId, "http://vocab.getty.edu/tgn/1000107");
        assertNotNull(doc);
        assertEquals("Brunei", doc.get(AlaLocationFactory.locality));
    }

    @Test
    public void testLoadBuild2() throws Exception {
        Classifier doc = parameterised.get(AlaLocationFactory.CONCEPT, AlaLocationFactory.locationId, "http://vocab.getty.edu/tgn/7024196");
        assertNotNull(doc);
        assertEquals("FFFTTT", doc.getSignature());
        assertEquals("Banteay Meanchey", doc.get(AlaLocationFactory.locality));
        assertEquals("Cambodia", doc.get(AlaLocationFactory.country));
        assertEquals("KH", doc.get(AlaLocationFactory.countryCode));
        AlaLocationParameters_FFFTTT params = new AlaLocationParameters_FFFTTT();
        doc.loadParameters(params);
        AlaLocationInferencer_FFFTTT inference = new AlaLocationInferencer_FFFTTT();
        AlaLocationInferencer.Evidence evidence = new AlaLocationInferencer.Evidence();
        evidence.e$locality = true;
        Inference prob = inference.probability(evidence, params, null);
        assertEquals(0.04545, prob.getEvidence(), 0.00001);
        assertEquals(1.0, prob.getPosterior(), 0.00001);
        evidence.e$locality = null;
        evidence.e$soundexLocality = true;
        prob = inference.probability(evidence, params, null);
        assertEquals(0.04545, prob.getEvidence(), 0.00001);
        assertEquals(1.0, prob.getPosterior(), 0.00001);
    }

    @Test
    public void testLoadBuild3() throws Exception {
        Classifier doc = parameterised.get(AlaLocationFactory.CONCEPT, AlaLocationFactory.locationId, "http://vocab.getty.edu/tgn/7003833");
        assertNotNull(doc);
        assertEquals("FFFTTT", doc.getSignature());
        assertEquals("Azores", doc.get(AlaLocationFactory.locality));
        assertEquals("Portugal", doc.get(AlaLocationFactory.country));
        assertEquals("PT", doc.get(AlaLocationFactory.countryCode));
        AlaLocationParameters_FFFTTT params = new AlaLocationParameters_FFFTTT();
        doc.loadParameters(params);
        AlaLocationInferencer_FFFTTT inference = new AlaLocationInferencer_FFFTTT();
        AlaLocationInferencer.Evidence evidence = new AlaLocationInferencer.Evidence();
        evidence.e$soundexLocality = true;
        Inference prob = inference.probability(evidence, params, null);
        assertEquals(0.04545, prob.getEvidence(), 0.00001);
        assertEquals(1.0, prob.getPosterior(), 0.00001);
    }

    @Test
    public void testLoadBuild4() throws Exception {
        Classifier doc = parameterised.get(AlaLocationFactory.CONCEPT, AlaLocationFactory.locationId, "http://vocab.getty.edu/tgn/1000074");
        assertNotNull(doc);
        assertEquals("FFFFTT", doc.getSignature());
        assertEquals("Greece", doc.get(AlaLocationFactory.locality));
        assertEquals("Greece", doc.get(AlaLocationFactory.country));
        assertEquals("GR", doc.get(AlaLocationFactory.countryCode));
        AlaLocationParameters_FFFFTT params = new AlaLocationParameters_FFFFTT();
        doc.loadParameters(params);
        AlaLocationInferencer_FFFFTT inference = new AlaLocationInferencer_FFFFTT();
        AlaLocationInferencer.Evidence evidence = new AlaLocationInferencer.Evidence();
        evidence.e$country = true;
        Inference prob = inference.probability(evidence, params, null);
        assertEquals(0.09091, prob.getEvidence(), 0.00001);
        assertEquals(1.00000, prob.getConditional(), 0.00001);
        assertEquals(0.50000, prob.getPosterior(), 0.00001);
    }


    @Test
    public void testLoadBuild5() throws Exception {
        Classifier doc = parameterised.get(AlaLocationFactory.CONCEPT, AlaLocationFactory.locationId, "http://vocab.getty.edu/tgn/1000074");
        assertNotNull(doc);
        assertEquals("FFFFTT", doc.getSignature());
        assertEquals("Greece", doc.get(AlaLocationFactory.locality));
        assertEquals("Greece", doc.get(AlaLocationFactory.country));
        assertEquals("GR", doc.get(AlaLocationFactory.countryCode));
        AlaLocationParameters_FFFFTT params = new AlaLocationParameters_FFFFTT();
        doc.loadParameters(params);
        AlaLocationInferencer_FFFFTT inference = new AlaLocationInferencer_FFFFTT();
        AlaLocationInferencer.Evidence evidence = new AlaLocationInferencer.Evidence();
        evidence.e$continent = true;
        Inference prob = inference.probability(evidence, params, null);
        assertEquals(0.31818, prob.getEvidence(), 0.00001);
        assertEquals(1.00000, prob.getConditional(), 0.00001);
        assertEquals(0.14286, prob.getPosterior(), 0.00001);
    }

    @Test
    public void testSearch1() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Adriatic Sea";
        List<LuceneClassifier> classifiers = this.searcher.search(classification);
        assertNotNull(classifiers);
        assertEquals(1, classifiers.size());
        assertEquals("http://vocab.getty.edu/tgn/7016532", classifiers.get(0).get(AlaLocationFactory.locationId));
    }


    @Test
    public void testSearch2() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Asian";
        List<LuceneClassifier> classifiers = this.searcher.search(classification);
        assertNotNull(classifiers);
        assertEquals(1, classifiers.size());
        assertEquals("http://vocab.getty.edu/tgn/1000004", classifiers.get(0).get(AlaLocationFactory.locationId));
    }


    @Test
    public void testSearch3() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.soundexLocality = "A250";
        List<LuceneClassifier> classifiers = this.searcher.search(classification);
        assertNotNull(classifiers);
        assertEquals(1, classifiers.size());
        assertEquals("http://vocab.getty.edu/tgn/1000004", classifiers.get(0).get(AlaLocationFactory.locationId));
    }

}
