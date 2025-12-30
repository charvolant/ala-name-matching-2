package au.org.ala.names;

import au.org.ala.bayesian.*;
import au.org.ala.names.lucene.LuceneClassifierSearcher;
import au.org.ala.util.JsonUtils;
import au.org.ala.util.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gbif.nameparser.api.Rank;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class ALAClassificationMatcherTest {
    public static final String INDEX = "/data/lucene/linnaean-2025-1-3";

    private LuceneClassifierSearcher searcher;
    private ALAClassificationMatcher matcher;

    @Before
    public void setUp() throws Exception {
        File index = new File(INDEX);
        if (!index.exists())
            throw new IllegalStateException("Index " + index + " not present");
        this.searcher = new LuceneClassifierSearcher(index, null, AlaLinnaeanFactory.taxonId);
        this.matcher = new ALAClassificationMatcher(AlaLinnaeanFactory.instance(), this.searcher, null, null, null);
    }

    @After
    public void tearDown() throws Exception {
        if (this.searcher != null)
            this.searcher.close();
    }


    @Test
    public void testFindKingdom1() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        boolean match = this.matcher.findKingdom("Acanthamoebidae", Rank.FAMILY, classification);
        assertTrue(match);
        Hints<AlaLinnaeanClassification> hints = classification.getHints();
        assertNotNull(hints);
        Set<String> values = hints.getHints(AlaLinnaeanFactory.kingdom);
        assertEquals(Collections.singleton("Protista"), values);
    }

    @Test
    public void testFindKingdom2() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        boolean match = this.matcher.findKingdom("Xenococcus", Rank.GENUS, classification);
        assertFalse(match); // Bare homonym
    }

    @Test
    public void testFindKingdom3() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        boolean match = this.matcher.findKingdom("Zosteropidae", Rank.FAMILY, classification);
        assertTrue(match); // Synonym
        Hints<AlaLinnaeanClassification> hints = classification.getHints();
        assertNotNull(hints);
        Set<String> values = hints.getHints(AlaLinnaeanFactory.kingdom);
        assertEquals(Collections.singleton("Animalia"), values);
    }

    @Test
    public void testTrace1() throws Exception {
        TestUtils.assumeNotTravis(); // Causes travis to fail for some reason
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia dealbata";
        MatchOptions options = MatchOptions.ALL.withTrace(Trace.TraceLevel.TRACE);
        Match<AlaLinnaeanClassification, MatchMeasurement> match = this.matcher.findMatch(classification, options);
        assertNotNull(match);
        assertTrue(match.isValid());
        assertEquals("Acacia dealbata", match.getAccepted().scientificName);
        Trace trace = match.getTrace();
        assertNotNull(trace);
        ObjectMapper mapper = JsonUtils.createMapper();
        TestUtils.compareNoSpaces(TestUtils.getResource(this.getClass(), "trace-1.json"), mapper.writeValueAsString(trace));
    }

    @Test
    public void testTrace2() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia dealbata";
        classification.family = "Rando";
        MatchOptions options = MatchOptions.ALL.withTrace(Trace.TraceLevel.SUMMARY);
        Match<AlaLinnaeanClassification, MatchMeasurement> match = this.matcher.findMatch(classification, options);
        assertNotNull(match);
        assertTrue(match.isValid());
        assertEquals("Acacia dealbata", match.getAccepted().scientificName);
        Trace trace = match.getTrace();
        assertNotNull(trace);
        ObjectMapper mapper = JsonUtils.createMapper();
        // mapper.writeValue(new File("trace-2.json"), trace);
        TestUtils.compareNoSpaces(TestUtils.getResource(this.getClass(), "trace-2.json"), mapper.writeValueAsString(trace));
    }

    @Test
    public void testTrace3() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia dealbata";
        classification.family = "Rando";
        MatchOptions options = MatchOptions.ALL.withTrace(Trace.TraceLevel.INFO);
        Match<AlaLinnaeanClassification, MatchMeasurement> match = this.matcher.findMatch(classification, options);
        assertNotNull(match);
        assertTrue(match.isValid());
        assertEquals("Acacia dealbata", match.getAccepted().scientificName);
        Trace trace = match.getTrace();
        assertNotNull(trace);
        ObjectMapper mapper = JsonUtils.createMapper();
        // mapper.writeValue(new File("trace-2.json"), trace);
        TestUtils.compareNoSpaces(TestUtils.getResource(this.getClass(), "trace-3.json"), mapper.writeValueAsString(trace));
    }


    @Ignore("Unstable result")
    public void testTrace4() throws Exception {
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificName = "Acacia dealbata";
        classification.family = "Rando";
        MatchOptions options = MatchOptions.ALL.withTrace(Trace.TraceLevel.DEBUG);
        Match<AlaLinnaeanClassification, MatchMeasurement> match = this.matcher.findMatch(classification, options);
        assertNotNull(match);
        assertTrue(match.isValid());
        assertEquals("Acacia dealbata", match.getAccepted().scientificName);
        Trace trace = match.getTrace();
        assertNotNull(trace);
        ObjectMapper mapper = JsonUtils.createMapper();
        // mapper.writeValue(new File("trace-2.json"), trace);
        TestUtils.compareNoSpaces(TestUtils.getResource(this.getClass(), "trace-4.json"), mapper.writeValueAsString(trace));
    }

}