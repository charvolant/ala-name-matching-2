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

public class AlaVernacularAnalyserTest {
    private AlaVernacularAnalyser analyser;

    @Before
    public void setUp() throws Exception {
        AnalyserConfig config = AnalyserConfig.builder().build();
        this.analyser = new AlaVernacularAnalyser(config);
    }


    @Test
    public void testAnalyseForSearch1() throws Exception {
        AlaVernacularClassification classification = new AlaVernacularClassification();
        classification.vernacularName = "Dingo";
        this.analyser.analyseForSearch(classification, MatchOptions.ALL);
        assertEquals("Dingo", classification.vernacularName);
        assertTrue(classification.getIssues().isEmpty());
    }

}
