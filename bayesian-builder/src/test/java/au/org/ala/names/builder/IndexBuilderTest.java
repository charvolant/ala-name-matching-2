package au.org.ala.names.builder;

import au.org.ala.bayesian.Classifier;
import au.org.ala.bayesian.Network;
import au.org.ala.bayesian.Observable;
import au.org.ala.names.generated.SimpleLinnaeanBuilder;
import au.org.ala.names.generated.SimpleLinnaeanFactory;
import au.org.ala.names.generated.SimpleLinnaeanParameters_FT;
import au.org.ala.names.lucene.LuceneClassifier;
import au.org.ala.names.lucene.LuceneClassifierSearcher;
import au.org.ala.names.lucene.LuceneLoadStore;
import au.org.ala.util.Metadata;
import org.gbif.dwc.terms.DwcTerm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;

public class IndexBuilderTest {
    private IndexBuilderConfiguration config;
    private IndexBuilder builder;

    @Before
    public void setUp() throws Exception {
        this.config = new IndexBuilderConfiguration();
        this.config.setWork(new File("target/test-work"));
        this.config.setBuilderClass(SimpleLinnaeanBuilder.class);
        this.config.setBuildAnalyserClass(NullBuildAnalyser.class);
        this.config.setLoadStoreClass(LuceneLoadStore.class);
        this.config.setNetwork(this.getClass().getResource("../lucene/simple-network.json"));
        this.config.setFactoryClass(SimpleLinnaeanFactory.class);
        this.builder = new IndexBuilder(this.config);
    }

    @After
    public void tearDown() throws Exception {
        if (this.builder != null)
            this.builder.close();
    }

    @Test
    public void testLoad1() throws Exception {
        Observable taxonID = this.builder.network.getObservable(DwcTerm.taxonID);
        Observable taxonomicStatus = this.builder.network.getObservable(DwcTerm.taxonomicStatus);
        URL surl = this.getClass().getResource("source-1.csv");
        CSVSource source = new CSVSource(surl, this.builder.getFactory(), this.builder.getNetwork().getObservables());
        this.builder.load(source);
        Classifier classifier = this.builder.getLoader().get(DwcTerm.Taxon, taxonID, "S-1");
        assertEquals("accepted", classifier.get(taxonomicStatus));
    }

    @Test
    public void testInterpret1() throws Exception {
        Observable taxonID = this.builder.network.getObservable(DwcTerm.taxonID);
        Observable scientificName = this.builder.network.getObservable(DwcTerm.scientificName);
        Observable taxonRank = this.builder.network.getObservable(DwcTerm.taxonRank);
        URL surl = this.getClass().getResource("source-1.csv");
        CSVSource source = new CSVSource(surl, this.builder.getFactory(), this.builder.getNetwork().getObservables());
        this.builder.load(source);
        LoadStore interpreted = this.builder.interpret(this.builder.getLoader());
        Classifier classifier = interpreted.get(DwcTerm.Taxon, taxonID, "S-1");
        assertEquals("S-1", classifier.get(taxonID));
        assertEquals("Artemia franciscana", classifier.get(scientificName));
        assertEquals("species", classifier.get(taxonRank));

    }


    @Test
    public void testSynonymise1() throws Exception {
        Observable taxonID = this.builder.network.getObservable(DwcTerm.taxonID);
        Observable synonymScientificName = this.builder.network.getObservable("synonymScientificName");
        URL surl = this.getClass().getResource("source-1.csv");
        CSVSource source = new CSVSource(surl, this.builder.getFactory(), this.builder.getNetwork().getObservables());
        this.builder.load(source);
        LoadStore interpreted = this.builder.interpret(this.builder.getLoader());
        LoadStore synonymised = this.builder.synonymise(interpreted);
        Classifier classifier = synonymised.get(DwcTerm.Taxon, taxonID, "S-1");
        assertEquals(Collections.singleton("Artemia salina"), classifier.getAll(synonymScientificName));
    }

    @Test
    public void testExpandTree1() throws Exception {
        Observable taxonID = this.builder.network.getObservable(DwcTerm.taxonID);
        Observable family = this.builder.network.getObservable(DwcTerm.family);
        Observable kingdom = this.builder.network.getObservable(DwcTerm.kingdom);
        URL surl = this.getClass().getResource("source-1.csv");
        CSVSource source = new CSVSource(surl, this.builder.getFactory(), this.builder.getNetwork().getObservables());
        this.builder.load(source);
        LoadStore interpreted = this.builder.interpret(this.builder.getLoader());
        LoadStore synonymised = this.builder.synonymise(interpreted);
        LoadStore expanded = this.builder.expand(synonymised);
        Classifier classifier = expanded.get(DwcTerm.Taxon, taxonID, "S-1");
        assertNotNull(classifier);
        assertEquals("ARTEMIIDAE", classifier.get(family));
        assertEquals(Collections.singleton("ARTEMIIDAE"), classifier.getAll(family));
        assertEquals("Animalia", classifier.get(kingdom));
        assertEquals(Collections.singleton("Animalia"), classifier.getAll(kingdom));
        int[] indexes = classifier.getIndex();
        assertEquals(2, indexes.length);
        classifier = expanded.get(DwcTerm.Taxon, taxonID, "S-S-1");
        assertNotNull(classifier);
        assertEquals("ARTEMIIDAE", classifier.get(family));
        assertEquals(Collections.singleton("ARTEMIIDAE"), classifier.getAll(family));
        assertEquals("Animalia", classifier.get(kingdom));
        assertEquals(Collections.singleton("Animalia"), classifier.getAll(kingdom));
    }


    @Test
    public void testInfer1() throws Exception {
        Observable taxonID = this.builder.network.getObservable(DwcTerm.taxonID);
        Observable family = this.builder.network.getObservable(DwcTerm.family);
        Observable kingdom = this.builder.network.getObservable(DwcTerm.kingdom);
        Observable soundexScientificName = this.builder.network.getObservable("soundexScientificName");
        URL surl = this.getClass().getResource("source-1.csv");
        CSVSource source = new CSVSource(surl, this.builder.getFactory(), this.builder.getNetwork().getObservables());
        this.builder.load(source);
        LoadStore interpreted = this.builder.interpret(this.builder.getLoader());
        LoadStore synonymised = this.builder.synonymise(interpreted);
        LoadStore expanded = this.builder.expand(synonymised);
        LoadStore inferred = this.builder.infer(expanded);
        Classifier classifier = inferred.get(DwcTerm.Taxon, taxonID, "S-1");
        assertNotNull(classifier);
        assertEquals("ARTEMIIDAE", classifier.get(family));
        assertEquals(Collections.singleton("ARTEMIIDAE"), classifier.getAll(family));
        assertEquals("Animalia", classifier.get(kingdom));
        assertEquals(Collections.singleton("Animalia"), classifier.getAll(kingdom));
        assertEquals(Collections.singleton("A635F652"), classifier.getAll(soundexScientificName));
    }

    @Test
    public void testBuildParameters1() throws Exception {
        Observable taxonID = this.builder.network.getObservable(DwcTerm.taxonID);
        URL surl = this.getClass().getResource("source-1.csv");
        CSVSource source = new CSVSource(surl, this.builder.getFactory(), this.builder.getNetwork().getObservables());
        this.builder.load(source);
        LoadStore interpreted = this.builder.interpret(this.builder.getLoader());
        LoadStore synonymised = this.builder.synonymise(interpreted);
        LoadStore expanded = this.builder.expand(synonymised);
        LoadStore inferred = this.builder.infer(expanded);
        LoadStore parameterised = this.builder.parameterise(inferred);
        Classifier classifier = parameterised.get(DwcTerm.Taxon, taxonID, "S-S-1");
        assertEquals("FT", classifier.getSignature());
        SimpleLinnaeanParameters_FT parameters = new SimpleLinnaeanParameters_FT();
        classifier.loadParameters(parameters);
        assertEquals(0.0909, parameters.prior_taxonId_t, 0.0001);
        assertEquals(0.0, parameters.inf_kingdom_f$t_t, 0.0001);
        assertEquals(0.6667, parameters.inf_phylum_t$f_f, 0.0001);
        assertEquals(0.0, parameters.inf_phylum_t$t_f, 0.0001);
    }


    @Test
    public void testBuildParameters2() throws Exception {
        Observable taxonID = this.builder.network.getObservable(DwcTerm.taxonID);
        URL surl = this.getClass().getResource("source-1.csv");
        CSVSource source = new CSVSource(surl, this.builder.getFactory(), this.builder.getNetwork().getObservables());
        this.builder.load(source);
        LoadStore interpreted = this.builder.interpret(this.builder.getLoader());
        LoadStore synonymised = this.builder.synonymise(interpreted);
        LoadStore expanded = this.builder.expand(synonymised);
        LoadStore inferred = this.builder.infer(expanded);
        LoadStore parameterised = this.builder.parameterise(inferred);
        Classifier classifier = parameterised.get(DwcTerm.Taxon, taxonID, "S-1");
        assertEquals("FT", classifier.getSignature());
        SimpleLinnaeanParameters_FT parameters = new SimpleLinnaeanParameters_FT();
        classifier.loadParameters(parameters);
        assertEquals(0.0909, parameters.prior_taxonId_t, 0.0001);
        assertEquals(0.0, parameters.inf_kingdom_f$t_t, 0.0001);
        assertEquals(0.0, parameters.inf_genus_t$t_fff, 0.0001);
        assertEquals(0.0, parameters.inf_genus_t$f_ftf, 0.0001);
    }

    @Test
    public void testMetadata1() throws Exception {
        Observable taxonID = this.builder.network.getObservable(DwcTerm.taxonID);
        Observable taxonomicStatus = this.builder.network.getObservable(DwcTerm.taxonomicStatus);
        URL surl = this.getClass().getResource("source-1.csv");
        CSVSource source = new CSVSource(surl, this.builder.getFactory(), this.builder.getNetwork().getObservables());
        this.builder.load(source);
        Metadata metadata = this.builder.createMetadata();
        assertNotNull(metadata);
        assertTrue(!metadata.getIdentifier().isEmpty());
        assertEquals(this.builder.network.getId(), metadata.getTitle());
        assertNotNull(metadata.getSources());
        assertEquals(2, metadata.getSources().size());
        assertFalse(metadata.getCreated().after(new Date()));
    }

    public void testBuild1() throws Exception {
        URL surl = this.getClass().getResource("source-1.csv");
        CSVSource source = new CSVSource(surl, this.builder.getFactory(), this.builder.getNetwork().getObservables());
        this.builder.load(source);
        LoadStore parameterised = this.builder.build();
        File output = new File(this.config.getWork(), "output");
        this.builder.buildIndex(output, parameterised);
        try (LuceneClassifierSearcher searcher = new LuceneClassifierSearcher(output, null, this.builder.getNetwork().getIdentifierObservable())) {
            Metadata metadata = searcher.getMetadata();
            assertNotNull(metadata);
            assertTrue(!metadata.getIdentifier().isEmpty());
            assertEquals(this.builder.network.getId(), metadata.getTitle());
            assertNotNull(metadata.getSources());
            assertEquals(2, metadata.getSources().size());
            assertFalse(metadata.getCreated().after(new Date()));
            File nw = new File(output, "network.json");
            assertTrue(nw.exists());
            Network saved = Network.read(nw.toURI().toURL());
            assertNotNull(saved);
            assertEquals(this.builder.network.getId(), saved.getId());
            LuceneClassifier classifier = searcher.get(SimpleLinnaeanFactory.CONCEPT, SimpleLinnaeanFactory.taxonId, "S-1");
            assertNotNull(classifier);
            assertEquals("Artemia franciscana", classifier.get(SimpleLinnaeanFactory.scientificName));
        }
    }

}
