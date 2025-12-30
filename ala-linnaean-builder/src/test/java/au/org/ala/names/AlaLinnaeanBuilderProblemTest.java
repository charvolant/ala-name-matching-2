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
import au.org.ala.vocab.TaxonomicStatus;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.nameparser.api.Rank;
import org.junit.*;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class AlaLinnaeanBuilderProblemTest extends TestUtils {
    private File work;
    private File output;
    private IndexBuilder<AlaLinnaeanClassification, AlaLinnaeanInferencer, AlaLinnaeanFactory, LuceneClassifier> builder;
    private LoadStore<LuceneClassifier> parameterised;

    private AlaLinnaeanFactory factory;
    private LuceneClassifierSearcher searcher;
    private ClassificationMatcher<AlaLinnaeanClassification, AlaLinnaeanInferencer, AlaLinnaeanFactory, MatchMeasurement> matcher;


    @Before
    public void setUp() throws Exception {
        this.factory = AlaLinnaeanFactory.instance();
    }

    @After
    public void tearDown() throws Exception {
        if (this.searcher != null)
            this.searcher.close();
        if (this.builder != null)
            this.builder.close();
        if (this.output != null)
            FileUtils.deleteAll(output);
        if (this.work != null)
            FileUtils.deleteAll(work);
    }

    protected void load(URL sourceUrl, boolean write) throws Exception {
        this.work = FileUtils.makeTmpDir("work");
        IndexBuilderConfiguration config = new IndexBuilderConfiguration();
        config.setBuilderClass(AlaLinnaeanBuilder.class);
        config.setNetwork(AlaLinnaeanBuilder.class.getResource("/ala-linnaean.json"));
        config.setWork(work);
        config.setFactoryClass(AlaLinnaeanFactory.class);
        config.setWeightAnalyserClass(AlaWeightAnalyser.class);
        config.setBuildAnalyserClass(AlaNameBuildAnalyser.class);
        this.builder = new IndexBuilder<AlaLinnaeanClassification, AlaLinnaeanInferencer, AlaLinnaeanFactory, LuceneClassifier>(config);
        Source source = Source.create(sourceUrl, AlaLinnaeanFactory.instance(), AlaLinnaeanFactory.instance().getObservables(), config.getTypes());
        this.builder.load(source);
        this.parameterised = this.builder.build();
        if (write) {
            this.output = FileUtils.makeTmpDir("output");
            this.builder.buildIndex(this.output, this.parameterised);
        }
    }

    protected void makeSearcher() throws Exception {
        this.searcher = new LuceneClassifierSearcher(this.output, null, this.factory.getIdentifier().get());
        this.matcher = this.factory.createMatcher(this.searcher, null, AnalyserConfig.load(this.output));
    }

    @Test
    public void testLoadBuild1() throws Exception {
        this.load(this.getClass().getResource("problem-1.csv"), false);
        Classifier doc = parameterised.get(DwcTerm.Taxon, AlaLinnaeanFactory.taxonId, "https://id.biodiversity.org.au/instance/apni/819303");
        assertNotNull(doc);
        Collection<String> names = doc.getNames();
        assertEquals(3, names.size());
        assertTrue(names.contains("Eucalyptus subser. Corymbosae"));
        assertTrue(names.contains("Eucalyptus Corymbosae"));
        assertTrue(names.contains("Corymbosae"));
        assertFalse(names.contains("Eucalyptus"));
        Set<String> scientificNames = doc.getAll(AlaLinnaeanFactory.scientificName);
        assertEquals(3, scientificNames.size());
        assertTrue(scientificNames.contains("Eucalyptus subser. Corymbosae"));
        assertTrue(scientificNames.contains("Eucalyptus Corymbosae"));
        assertTrue(scientificNames.contains("Corymbosae"));
        assertFalse(scientificNames.contains("Eucalyptus"));
    }
}
