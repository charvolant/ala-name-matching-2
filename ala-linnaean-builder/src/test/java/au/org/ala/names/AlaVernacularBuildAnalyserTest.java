package au.org.ala.names;

import au.org.ala.bayesian.AnalyserConfig;
import au.org.ala.bayesian.Classifier;
import au.org.ala.bayesian.MatchOptions;
import au.org.ala.names.lucene.LuceneClassifier;
import au.org.ala.vocab.TaxonomicStatus;
import au.org.ala.vocab.VernacularStatus;
import org.gbif.nameparser.api.Rank;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class AlaVernacularBuildAnalyserTest {
    private AlaVernacularBuildAnalyser analyser;

    @Before
    public void setUp() throws Exception {
        AnalyserConfig config = AnalyserConfig.builder().build();
        this.analyser = new AlaVernacularBuildAnalyser(config);
    }

    @Test
    public void testAnalyseForIndex1() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaVernacularFactory.vernacularName, "Common Wombat", false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Common Wombat", classifier.get(AlaVernacularFactory.vernacularName));
        assertEquals(1.0, classifier.get(AlaVernacularFactory.weight), 0.00001);
    }

    @Test
    public void testAnalyseForIndex2() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaVernacularFactory.vernacularName, "Common Wombat", false, false);
        classifier.add(AlaVernacularFactory.vernacularStatus, VernacularStatus.preferred, false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Common Wombat", classifier.get(AlaVernacularFactory.vernacularName));
        assertEquals(20.0, classifier.get(AlaVernacularFactory.weight), 0.00001);
    }

    @Test
    public void testAnalyseForIndex3() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaVernacularFactory.vernacularName, "Common Wombat", false, false);
        classifier.add(AlaVernacularFactory.taxonRank, Rank.SPECIES, false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Common Wombat", classifier.get(AlaVernacularFactory.vernacularName));
        assertEquals(10.0, classifier.get(AlaVernacularFactory.weight), 0.00001);
    }


    @Test
    public void testAnalyseForIndex4() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaVernacularFactory.vernacularName, "Common Wombat", false, false);
        classifier.add(AlaVernacularFactory.taxonomicStatus, TaxonomicStatus.homotypicSynonym, false, false);
        this.analyser.analyseForIndex(classifier);
        assertEquals("Common Wombat", classifier.get(AlaVernacularFactory.vernacularName));
        assertEquals(1.0, classifier.get(AlaVernacularFactory.weight), 0.00001);
    }

    @Test
    public void testAnalyseNames1() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaVernacularFactory.vernacularName, "King Island Dunnart", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaVernacularFactory.vernacularName, Optional.empty(), Optional.empty(), false);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertTrue(names.contains("King Island Dunnart"));
    }

    @Test
    public void testAnalyseNames2() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaVernacularFactory.vernacularName, "Mārū", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaVernacularFactory.vernacularName, Optional.empty(), Optional.empty(), false);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Mārū"));
        assertTrue(names.contains("Maru"));
    }

    @Test
    public void testAnalyseNames3() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaVernacularFactory.vernacularName, "Yellow-fin Tuna", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaVernacularFactory.vernacularName, Optional.empty(), Optional.empty(), false);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Yellow-fin Tuna"));
        assertTrue(names.contains("Yellow fin Tuna"));
        assertTrue(names.contains("Yellowfin Tuna"));
    }

    @Test
    public void testAnalyseNames4() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaVernacularFactory.vernacularName, "Yellow - footed Hopping-mouse", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaVernacularFactory.vernacularName, Optional.empty(), Optional.empty(), false);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Yellow - footed Hopping-mouse"));
        assertTrue(names.contains("Yellow footed Hopping mouse"));
        assertTrue(names.contains("Yellowfooted Hoppingmouse"));
    }


    @Test
    public void testAnalyseNames5() throws Exception {
        Classifier classifier = new LuceneClassifier();
        classifier.add(AlaVernacularFactory.vernacularName, "Sturt's Desert-Pea", false, false);
        Set<String> names = this.analyser.analyseNames(classifier, AlaVernacularFactory.vernacularName, Optional.empty(), Optional.empty(), false);
        assertNotNull(names);
        assertEquals(6, names.size());
        assertTrue(names.contains("Sturt's Desert-Pea"));
        assertTrue(names.contains("Sturts Desert-Pea"));
        assertTrue(names.contains("Sturt's Desert Pea"));
        assertTrue(names.contains("Sturts Desert Pea"));
        assertTrue(names.contains("Sturt's DesertPea"));
        assertTrue(names.contains("Sturts DesertPea"));
    }

}
