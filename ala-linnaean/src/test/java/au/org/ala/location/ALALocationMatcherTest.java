package au.org.ala.location;

import au.org.ala.bayesian.Issues;
import au.org.ala.bayesian.Match;
import au.org.ala.bayesian.MatchMeasurement;
import au.org.ala.bayesian.MatchOptions;
import au.org.ala.names.lucene.LuceneClassifierSearcher;
import au.org.ala.vocab.GeographyType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ALALocationMatcherTest {
    public static final String INDEX = "/data/lucene/location-2025-1-3";

    private LuceneClassifierSearcher searcher;
    private ALALocationClassificationMatcher matcher;

    @Before
    public void setUp() throws Exception {
        File index = new File(INDEX);
        if (!index.exists())
            throw new IllegalStateException("Index " + index + " not present");
        this.searcher = new LuceneClassifierSearcher(index, null, AlaLocationFactory.locationId);
        this.matcher = new ALALocationClassificationMatcher(AlaLocationFactory.instance(), this.searcher, null, null);
    }

    @After
    public void tearDown() throws Exception {
        if (this.searcher != null)
            this.searcher.close();
    }


    @Test
    public void testSimpleMatch1() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Europe";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/1000003", match.getAccepted().locationId);
        assertEquals(GeographyType.continent, match.getAccepted().geographyType);
        assertEquals("Europe", match.getAccepted().locality);
        assertEquals("Europe", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().country);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testSimpleMatch2() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "France";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/1000070", match.getAccepted().locationId);
        assertEquals(GeographyType.country, match.getAccepted().geographyType);
        assertEquals("France", match.getAccepted().locality);
        assertEquals("Europe", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("France", match.getAccepted().country);
        assertEquals("FR", match.getAccepted().countryCode);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(0.999, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testSimpleMatch3() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "New South Wales";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7001828", match.getAccepted().locationId);
        assertEquals(GeographyType.stateProvince, match.getAccepted().geographyType);
        assertEquals("New South Wales", match.getAccepted().locality);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("AU", match.getAccepted().countryCode);
        assertEquals("New South Wales", match.getAccepted().stateProvince);
        assertEquals(0.999, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testSimpleMatch4() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Austral Islands";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/1009851", match.getAccepted().locationId);
        assertEquals(GeographyType.islandGroup, match.getAccepted().geographyType);
        assertEquals("Austral Islands", match.getAccepted().locality);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertEquals("Austral Islands", match.getAccepted().islandGroup);
        assertEquals("French Polynesia", match.getAccepted().country);
        assertEquals("PF", match.getAccepted().countryCode);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testSimpleMatch5() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Heard Island";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/1007365", match.getAccepted().locationId);
        assertEquals(GeographyType.island, match.getAccepted().geographyType);
        assertEquals("Heard Island", match.getAccepted().locality);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("AU", match.getAccepted().countryCode);
        assertEquals("Heard Island and McDonald Islands", match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testSimpleMatch6() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Atlantic Ocean";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7014206", match.getAccepted().locationId);
        assertEquals(GeographyType.waterBody, match.getAccepted().geographyType);
        assertEquals("Atlantic Ocean", match.getAccepted().locality);
        assertNull(match.getAccepted().continent);
        assertEquals("Atlantic Ocean", match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().country);
        assertNull(match.getAccepted().countryCode);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(0.999, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testSimpleMatch7() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Tasman Sea";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/1112316", match.getAccepted().locationId);
        assertEquals(GeographyType.waterBody, match.getAccepted().geographyType);
        assertEquals("Tasman Sea", match.getAccepted().locality);
        assertNull(match.getAccepted().continent);
        assertEquals("Tasman Sea", match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().country);
        assertNull(match.getAccepted().countryCode);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testConfirmedMatch1() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "New South Wales";
        classification.country = "Australia";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7001828", match.getAccepted().locationId);
        assertEquals(GeographyType.stateProvince, match.getAccepted().geographyType);
        assertEquals("New South Wales", match.getAccepted().locality);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("AU", match.getAccepted().countryCode);
        assertEquals("New South Wales", match.getAccepted().stateProvince);
        assertEquals(0.999, match.getProbability().getPosterior(), 0.001);
    }


    @Test
    public void testConfirmedMatch2() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Twin Islands";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        classification.continent = "Oceania";
        match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        classification.stateProvince = "Western Australia";
        match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testConfirmedMatch3() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "North Province";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals(0.529, match.getProbability().getPosterior(), 0.001);
        classification.continent = "Oceania";
        match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals(0.646, match.getProbability().getPosterior(), 0.001);
        classification.continent = "Asia";
        match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertEquals(0.847, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testCapitalisation1() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "new south wAles";
        classification.country = "AUSTRALIA";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7001828", match.getAccepted().locationId);
        assertEquals(GeographyType.stateProvince, match.getAccepted().geographyType);
        assertEquals("New South Wales", match.getAccepted().locality);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("AU", match.getAccepted().countryCode);
        assertEquals("New South Wales", match.getAccepted().stateProvince);
        assertEquals(0.999, match.getProbability().getPosterior(), 0.001);
    }


    @Test
    public void testAlternateName1() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "NSW";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7001828", match.getAccepted().locationId);
        assertEquals(GeographyType.stateProvince, match.getAccepted().geographyType);
        assertEquals("New South Wales", match.getAccepted().locality);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("AU", match.getAccepted().countryCode);
        assertEquals("New South Wales", match.getAccepted().stateProvince);
        assertEquals(0.999, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testAlternateName2() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "NSW";
        classification.countryCode = "AU";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7001828", match.getAccepted().locationId);
        assertEquals(GeographyType.stateProvince, match.getAccepted().geographyType);
        assertEquals("New South Wales", match.getAccepted().locality);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("AU", match.getAccepted().countryCode);
        assertEquals("New South Wales", match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
    }


    @Test
    public void testAlternateName3() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Territorial Collectivity of Mayotte";
         Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7001669", match.getAccepted().locationId);
        assertEquals(GeographyType.country, match.getAccepted().geographyType);
        assertEquals("Mayotte", match.getAccepted().locality);
        assertEquals("Africa", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Mayotte", match.getAccepted().country);
        assertEquals("YT", match.getAccepted().countryCode);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(0.989, match.getProbability().getPosterior(), 0.001);
    }

    @Test
    public void testProblemMatch1() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.waterBody = "Southern Ocean";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/1112234", match.getAccepted().locationId);
        assertEquals(GeographyType.waterBody, match.getAccepted().geographyType);
        assertEquals("Southern Ocean", match.getAccepted().locality);
        assertNull(match.getAccepted().country);
        assertNull(match.getAccepted().continent);
        assertEquals("Southern Ocean", match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(), match.getIssues());
    }

    @Test
    public void testProblemMatch2() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.country = "Germany";
        classification.countryCode = "DE";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7000084", match.getAccepted().locationId);
        assertEquals(GeographyType.country, match.getAccepted().geographyType);
        assertEquals("Federal Republic of Germany", match.getAccepted().locality);
        assertEquals("Federal Republic of Germany", match.getAccepted().country);
        assertEquals("Europe", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(), match.getIssues());
    }

    @Test
    public void testProblemMatch3() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.continent = "ASIA";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/1000004", match.getAccepted().locationId);
        assertEquals(GeographyType.continent, match.getAccepted().geographyType);
        assertEquals("Asia", match.getAccepted().locality);
        assertNull(match.getAccepted().country);
        assertEquals("Asia", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(), match.getIssues());
    }

    @Test
    public void testProblemMatch4() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.island = "Adele";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/1006179", match.getAccepted().locationId);
        assertEquals(GeographyType.island, match.getAccepted().geographyType);
        assertEquals("Adele Island", match.getAccepted().locality);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertEquals("Adele Island", match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Western Australia", match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(AlaLocationFactory.MISSPELLED_LOCALITY), match.getIssues());
    }


    // Ensure we don't get a US county rather than an island
    @Test
    public void testProblemMatch5() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.island = "Alexander";
        classification.islandGroup = "Houtman Abrolhos Easter Group";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7854458", match.getAccepted().locationId);
        assertEquals(GeographyType.islandGroup, match.getAccepted().geographyType);
        assertEquals("Easter Group", match.getAccepted().locality);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertEquals("Western Australia", match.getAccepted().stateProvince);
        assertEquals("Easter Group", match.getAccepted().islandGroup);
        assertNull(match.getAccepted().island);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(AlaLocationFactory.HIGHER_LOCALITY), match.getIssues());
    }


    // Invalid country code
    @Test
    public void testProblemMatch6() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.country = "United Kingdom of Great Britain and Northern Ireland";
        classification.countryCode = "GB";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7008591", match.getAccepted().locationId);
        assertEquals(GeographyType.country, match.getAccepted().geographyType);
        assertEquals("United Kingdom", match.getAccepted().locality);
        assertEquals("United Kingdom", match.getAccepted().country);
        assertEquals("Europe", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(AlaLocationFactory.INCONSISTENT_COUNTRY), match.getIssues());
    }


    // Vague island group
    @Test
    public void testProblemMatch7() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.island = "Angle";
        classification.islandGroup = "Passage";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7796565", match.getAccepted().locationId);
        assertEquals(GeographyType.islandGroup, match.getAccepted().geographyType);
        assertEquals("Passage Islands", match.getAccepted().locality);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertEquals("Passage Islands", match.getAccepted().islandGroup);
        assertEquals("Western Australia", match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(AlaLocationFactory.MISSPELLED_LOCALITY, AlaLocationFactory.HIGHER_LOCALITY), match.getIssues());
    }

    // The united kingdom is going to love this ...
    @Test
    public void testProblemMatch8() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.stateProvince = "England";
        classification.country = "United Kingdom of Great Britain and Northern Ireland";
        classification.countryCode = "GB";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7002445", match.getAccepted().locationId);
        assertEquals(GeographyType.country, match.getAccepted().geographyType);
        assertEquals("England", match.getAccepted().locality);
        assertEquals("England", match.getAccepted().country);
        assertEquals("Europe", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(AlaLocationFactory.INCONSISTENT_COUNTRY), match.getIssues());
    }


    // UNknown values should be removes
    @Test
    public void testProblemMatch9() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.stateProvince = "Queensland";
        classification.country = "unknown or invalid";
        classification.countryCode = "ZZ";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7001830", match.getAccepted().locationId);
        assertEquals(GeographyType.stateProvince, match.getAccepted().geographyType);
        assertEquals("Queensland", match.getAccepted().locality);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Queensland", match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(), match.getIssues());
    }

    @Test
    public void testProblemMatch10() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.continent = "AFRICA";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7001242", match.getAccepted().locationId);
        assertEquals(GeographyType.continent, match.getAccepted().geographyType);
        assertEquals("Africa", match.getAccepted().locality);
        assertNull(match.getAccepted().country);
        assertEquals("Africa", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(), match.getIssues());
    }


    @Test
    public void testProblemMatch11() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.country = "Falkland Islands (Malvinas)";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7005152", match.getAccepted().locationId);
        assertEquals(GeographyType.country, match.getAccepted().geographyType);
        assertEquals("Falkland Islands", match.getAccepted().locality);
        assertEquals("Falkland Islands", match.getAccepted().country);
        assertEquals("South America", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(), match.getIssues());
    }


    @Test
    public void testProblemMatch12() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.island = "Ashmore Reef  (East)";
        classification.islandGroup = "Sahul Shelf";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/7785992", match.getAccepted().locationId);
        assertEquals(GeographyType.island, match.getAccepted().geographyType);
        assertEquals("Ashmore Reef", match.getAccepted().locality);
        assertEquals("Australia", match.getAccepted().country);
        assertEquals("Oceania", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertEquals("Ashmore Reef", match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertEquals("Queensland", match.getAccepted().stateProvince);
        assertEquals(1.0, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(AlaLocationFactory.REMOVED_ISLAND_GROUP), match.getIssues());
    }


    @Test
    public void testProblemMatch13() throws Exception {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = "Southern Italy";
        Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification, MatchOptions.ALL);
        assertTrue(match.isValid());
        assertSame(match.getMatch(), match.getAccepted());
        assertEquals("http://vocab.getty.edu/tgn/4005365", match.getAccepted().locationId);
        assertEquals(GeographyType.region, match.getAccepted().geographyType);
        assertEquals("Italy, Southern", match.getAccepted().locality);
        assertEquals("Italy", match.getAccepted().country);
        assertEquals("Europe", match.getAccepted().continent);
        assertNull(match.getAccepted().waterBody);
        assertNull(match.getAccepted().island);
        assertNull(match.getAccepted().islandGroup);
        assertNull(match.getAccepted().stateProvince);
        assertEquals(0.999, match.getProbability().getPosterior(), 0.001);
        assertEquals(Issues.of(), match.getIssues());
    }

}