package au.org.ala.names;

import au.org.ala.bayesian.Issues;
import au.org.ala.bayesian.Match;
import au.org.ala.bayesian.MatchMeasurement;
import au.org.ala.location.AlaLocationClassification;
import au.org.ala.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.gbif.dwc.terms.Term;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that well-known and common species are correctly found
 */
public class IconicSpeciesTest {
    private static final Logger logger = LoggerFactory.getLogger(IconicSpeciesTest.class);

    public static final String INDEX = "/data/lucene/linnaean-2025-1-3";
    public static final String VERNACULAR_INDEX = "/data/lucene/vernacular-2025-1-3";
    public static final String LOCATION_INDEX = "/data/lucene/location-2025-1-3";
    public static final String SUGGESTER_INDEX = "/data/tmp/suggester-2025-1-3";

    private ALANameSearcher searcher;
    private AlaNameAnalyser analyser;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = JsonUtils.createMapper();
        ALANameSearcherConfiguration config = mapper.readValue(IconicSpeciesTest.class.getResource("searcher-config-test.json"), ALANameSearcherConfiguration.class);
        this.searcher = new ALANameSearcher(config);
     }

    @After
    public void tearDown() throws Exception {
        this.searcher.close();
    }

    private int testIssues(Match<?, ?> match, String name, int line, boolean testLocation) {
        Issues issues = match.getIssues();
        if (issues.isEmpty())
            return 0;
        Set<Term> values = new HashSet<>(issues);
        values.remove(AlaLinnaeanFactory.ACCEPTED_AND_SYNONYM);
        values.remove(AlaLinnaeanFactory.PARTIALLY_MISAPPLIED_NAME);
        values.remove(AlaLinnaeanFactory.PARTIALLY_EXCLUDED_NAME);
        values.remove(AlaLinnaeanFactory.MULTIPLE_MATCHES);
        values.remove(AlaLinnaeanFactory.PARENT_CHILD_SYNONYM);
        if (!testLocation)
            values.remove(AlaLinnaeanFactory.REMOVED_LOCATION);
        if (values.isEmpty())
            return 0;
        logger.error("Issues for " + name + " line " + line + " " + issues);
        return 1;
    }
    
    private int compare(String expected, String actual, String element, String name, int line) {
        if (expected != null && !expected.equalsIgnoreCase(actual)) {
            logger.error("Mismatched " + element + " on " + name + " line " + line + " expected " + expected + " got " + actual);
            return 1;
        }
        return 0;
    }
    
    private int testMatch(AlaLinnaeanClassification template, Match<AlaLinnaeanClassification, MatchMeasurement> match, int line, boolean testLocation) {
        int errors = 0;
        String scientificName = template.scientificName;
        if (!match.isValid()) {
            logger.error("No valid match for " + scientificName + " line " + line);
            return 1;
        }
        errors += this.testIssues(match, scientificName, line, testLocation);
        if (match.getProbability().getPosterior() < 0.98) {
            logger.error("Inaccurate match for " + scientificName + " of " + match.getProbability() + " line " + line);
            errors += 1;
        }
        errors += this.compare(template.kingdom, match.getAccepted().kingdom, "kingdom", scientificName, line);
        errors += this.compare(template.phylum, match.getAccepted().phylum, "phylum", scientificName, line);
        errors += this.compare(template.class_, match.getAccepted().class_, "class", scientificName, line);
        errors += this.compare(template.order, match.getAccepted().order, "order", scientificName, line);
        errors += this.compare(template.family, match.getAccepted().family, "family", scientificName, line);
        errors += this.compare(template.genus, match.getAccepted().genus, "genus", scientificName, line);
        String matchedName = match.getAccepted().scientificName;
        // Remove subgenus if present
        matchedName = matchedName.replaceAll("\\([A-Z][a-z]*\\)", "").replaceAll("\\s+", " ");
        errors += this.compare(scientificName, matchedName, "scientific name", scientificName, line);
        return errors;
    }


    private int testMatch(AlaVernacularClassification template, Match<AlaVernacularClassification, MatchMeasurement> match, Match<AlaLinnaeanClassification, MatchMeasurement> scientific, int line, boolean matchVernacular, boolean testLocation) throws Exception  {
        int errors = 0;
        String vernacularName = template.vernacularName;
        if (!match.isValid()) {
            logger.error("No valid match for " + vernacularName + " line " + line);
            return 1;
        }
        errors += this.testIssues(match,vernacularName,  line, testLocation);
        String matchedName = match.getAccepted().vernacularName;
        errors += this.compare(vernacularName, matchedName, "vernacular name", vernacularName, line);
        if (!matchVernacular) {
            logger.info("Skipping taxon match for " + vernacularName + " line " + line);
        } else {
            String taxonId = match.getAccepted().taxonId;
            AlaLinnaeanClassification accepted = scientific.getAccepted();
            AlaLinnaeanClassification matchAccepted = this.searcher.get(taxonId);
            if (matchAccepted == null) {
                logger.error("Unable to get taxonId " + taxonId + " for " + vernacularName + " ... maybe out of date indexes?");
                errors += 1;
            } else if (!taxonId.equals(accepted.taxonId)) {
                if (matchAccepted.speciesId != null && matchAccepted.speciesId.equals(accepted.speciesId)) {
                    logger.info("Match for " + vernacularName + " at species level for " + matchAccepted.scientificName + " vs accepted " + accepted.scientificName + " on line " + line);
                } else if (matchAccepted.genusId != null && matchAccepted.genusId.equals(accepted.genusId)) {
                    logger.info("Match for " + vernacularName + " at genus level for " + matchAccepted.scientificName + " vs accepted " + accepted.scientificName + " on line " + line);
                } else if (matchAccepted.familyId != null && matchAccepted.familyId.equals(accepted.familyId)) {
                    logger.warn("Match for " + vernacularName + " at family level for " + matchAccepted.scientificName + " vs accepted " + accepted.scientificName + " on line " + line);
                } else {
                    logger.error("Mismatch on " + vernacularName + " for " + matchAccepted.scientificName + " vs accepted " + accepted.scientificName + " on line " + line);
                    errors += 1;

                }
            }
        }
        return errors;
    }

    private void testFile(String name, boolean locality) throws Exception {
        InputStream s = null;
        int errors = 0;
        try {
            s = this.getClass().getResourceAsStream(name);
            CSVReader reader = CSVReaderFactory.build(s, StandardCharsets.UTF_8.name(), ",", '"', 1);
            int line = 1;
            while (reader.hasNext()) {
                String row[] = reader.next();
                line++;
                AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
                AlaLocationClassification location = null;
                String species;
                String subspecies;
                String scientificName;
                try {
                    if (row[0].startsWith("#")) // Skip comment
                        continue;
                    if (locality) {
                        location = new AlaLocationClassification();
                        location.stateProvince = StringUtils.trimToNull(row[1]);
                        location.country = "Australia";
                    }
                    classification.kingdom = StringUtils.trimToNull(row[2]);
                    classification.phylum = StringUtils.trimToNull(row[3]);
                    classification.class_ = StringUtils.trimToNull(row[4]);
                    classification.order = StringUtils.trimToNull(row[5]);
                    classification.family = StringUtils.trimToNull(row[6]);
                    classification.genus = StringUtils.trimToNull(row[7]);
                    species = StringUtils.trim(row[8]);
                    subspecies = StringUtils.trim(row[9]);
                    if (classification.genus != null)
                        scientificName = (classification.genus + " " + species + " " + subspecies).trim();
                    else if (classification.family != null)
                        scientificName = classification.family;
                    else if (classification.order != null)
                        scientificName = classification.order;
                    else {
                        logger.warn("No scientific name available on line " + line);
                        scientificName = null;
                    }
                    classification.scientificName = scientificName;
                } catch (Exception ex) {
                    throw new IllegalStateException("Error on line " + line, ex);
                }
                if (location != null) {
                    Match<AlaLocationClassification, MatchMeasurement> locMatch = this.searcher.search(location);
                    if (!locMatch.isValid()) {
                        logger.info("Unable to match location " + location.country + "/" + location.stateProvince);
                        errors += 1;
                    } else {
                        classification.locationId = locMatch.getAllIdentifiers();
                    }
                }
                Match<AlaLinnaeanClassification, MatchMeasurement> match = this.searcher.search(classification);
                boolean testLocation = Boolean.parseBoolean(row.length >= 12 ? row[11] : "true");
                errors += this.testMatch(classification, match, line, testLocation);
                AlaLinnaeanClassification nameOnly = new AlaLinnaeanClassification();
                nameOnly.scientificName = scientificName;
                match = this.searcher.search(nameOnly);
                this.testMatch(classification, match, line, testLocation);

                String vernacularName = StringUtils.trimToNull(row[0]);
                boolean matchVernacular = Boolean.parseBoolean(row.length >= 11 ? row[10] : "true");
                if (vernacularName != null) {
                    AlaVernacularClassification vernacularClassification = new AlaVernacularClassification();
                    vernacularClassification.vernacularName = vernacularName;
                    Match<AlaVernacularClassification, MatchMeasurement> vernacularMatch = this.searcher.search(vernacularClassification);
                    errors += testMatch(vernacularClassification, vernacularMatch, match, line, matchVernacular, testLocation);
                }
            }
        } finally {
            if (s != null)
                s.close();
        }
        assertEquals(0, errors);
    }

    @Test
    public void testProblems() throws Exception {
        this.testFile("problems.csv", false);
    }

    @Test
    public void testBirds() throws Exception {
        this.testFile("birds.csv", false);
    }

    @Test
    public void testFish() throws Exception {
        this.testFile("fish.csv", false);
    }

    @Test
    public void testFrogs() throws Exception {
        this.testFile("frogs.csv", false);
    }

    @Test
    public void testMammals() throws Exception {
        this.testFile("mammals.csv", false);
    }

    @Test
    public void testReptiles() throws Exception {
        this.testFile("reptiles.csv", false);
    }

    @Test
    public void testInvertebrates() throws Exception {
        this.testFile("invertebrates.csv", false);
    }

    @Test
    public void testFloweringPlants() throws Exception {
        this.testFile("flowering-plants.csv", false);
    }

    @Test
    public void testFungi() throws Exception {
        this.testFile("fungi.csv", false);
    }

    @Test
    public void testFerns() throws Exception {
        this.testFile("ferns.csv", false);
    }

    @Test
    public void testEmblems() throws Exception {
        this.testFile("emblems.csv", true);
    }

}
