package au.org.ala.location;

import au.org.ala.bayesian.ClassificationMatcherConfiguration;
import au.org.ala.bayesian.Match;
import au.org.ala.bayesian.MatchMeasurement;
import au.org.ala.bayesian.MatchOptions;
import au.org.ala.names.lucene.LuceneClassifierSearcher;
import au.org.ala.names.lucene.LuceneClassifierSearcherConfiguration;
import au.org.ala.vocab.GeographyType;
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
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test against a sample of locations drawn from ALA data
 */
public class PerformanceTest {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    public static final int SEARCH_QUERY_LIMIT = 15;
    public static final int SEARCH_CACHE_SIZE = 10000;
    public static final String LOCATION_INDEX = "/data/lucene/location-2025-1-3";


    private LuceneClassifierSearcher searcher;
    private ALALocationClassificationMatcher matcher;

    @Before
    public void setUp() throws Exception {
        File location = new File(LOCATION_INDEX);
         if (!location.exists())
            throw new IllegalStateException("Index " + location + " not present");
         LuceneClassifierSearcherConfiguration sConfig = LuceneClassifierSearcherConfiguration.builder()
                .queryLimit(SEARCH_QUERY_LIMIT)
                .cacheSize(SEARCH_CACHE_SIZE)
                .build();
        ClassificationMatcherConfiguration cConfig = ClassificationMatcherConfiguration.builder()
                .enableJmx(true)
                .statistics(true)
                .build();
        this.searcher = new LuceneClassifierSearcher(location, sConfig, AlaLocationFactory.locationId);
        this.matcher = new ALALocationClassificationMatcher(AlaLocationFactory.instance(), this.searcher, cConfig, null);
    }

    @After
    public void tearDown() throws Exception {
        this.matcher.close();
        this.searcher.close();
    }

    private void testFile(String name) throws Exception {
        InputStream s = null;
        int errors = 0;
        int errorsWeighted = 0;
        int matched = 0;
        int matchedWeighted = 0;
        int success = 0;
        int successWeighted = 0;
        int expected = 0;
        int expectedWeighted = 0;
        int accurate = 0;
        int accurateWeighted = 0;
        int clean = 0;
        int cleanWeighted = 0;
        Map<Term, Integer> issueCount = new HashMap<>();
        Map<Term, Integer> issueCountWeighted = new HashMap<>();
        long startTime, endTime;
        try {
            s = this.getClass().getResourceAsStream(name);
            CSVReader reader = CSVReaderFactory.build(s, StandardCharsets.UTF_8.name(), ",", '"', 0);
            String[] header = reader.next();
            int validIndex = this.indexOf(header, "valid");
            int stateProvinceIndex = this.indexOf(header, "stateProvince");
            int countryIndex = this.indexOf(header, "country");
            int countryCodeIndex = this.indexOf(header, "countryCode");
            int continentIndex = this.indexOf(header, "continent");
            int islandIndex = this.indexOf(header, "island");
            int islandGroupIndex = this.indexOf(header, "islandGroup");
            int waterBodyIndex = this.indexOf(header, "waterBody");
            int countIndex = this.indexOf(header, "count");
            int matchIndex = this.indexOf(header, "match");
            int line = 1;
            startTime = System.currentTimeMillis();
            while (reader.hasNext()) {
                String row[] = reader.next();
                line++;
                AlaLocationClassification classification = new AlaLocationClassification();
                boolean expectValid = false;
                boolean flag = false;
                String expectedLocality = null;
                int count = 1;
                try {
                    if (row[0].startsWith("#")) // Skip comment
                        continue;
                    String valid = validIndex < 0 ? "true" : StringUtils.trimToNull(row[validIndex]);
                    expectValid = !(valid == null || valid.equalsIgnoreCase("false"));
                    flag = valid != null && valid.equalsIgnoreCase("flag");
                    classification.stateProvince = stateProvinceIndex < 0 ? null : StringUtils.trimToNull(row[stateProvinceIndex]);
                    classification.country = countryIndex < 0 ? null : StringUtils.trim(row[countryIndex]);
                    classification.countryCode = countryCodeIndex < 0 ? null : StringUtils.trimToNull(row[countryCodeIndex]);
                    classification.continent = continentIndex < 0 ? null : StringUtils.trimToNull(row[continentIndex]);
                    classification.island = islandIndex < 0 ? null : StringUtils.trimToNull(row[islandIndex]);
                    classification.islandGroup = islandGroupIndex < 0 ? null : StringUtils.trimToNull(row[islandGroupIndex]);
                    classification.waterBody = waterBodyIndex < 0 ? null : StringUtils.trimToNull(row[waterBodyIndex]);
                    expectedLocality = matchIndex < 0 ? null : StringUtils.trimToNull(row[matchIndex]);
                    String cv = countIndex < 0 ? null : StringUtils.trimToNull(row[countIndex]);
                    count = cv == null || cv.isEmpty() ? 1 : Integer.parseInt(cv);
                } catch (Exception ex) {
                    throw new IllegalStateException("Error on line " + line, ex);
                }
                matched++;
                matchedWeighted += count;
                try {
                    Match<AlaLocationClassification, MatchMeasurement> match = this.matcher.findMatch(classification.clone(), MatchOptions.ALL);
                    for (Term issue : match.getIssues()) {
                        int ic = issueCount.getOrDefault(issue, 0);
                        issueCount.put(issue, ic + 1);
                        ic = issueCountWeighted.getOrDefault(issue, 0);
                        issueCountWeighted.put(issue, ic + count);
                    }
                    if (match.isValid()) {
                        success++;
                        successWeighted += count;
                        classification.inferForSearch(this.matcher.getAnalyser(), MatchOptions.ALL);
                        String cleanLocality = classification.locality;
                        if (cleanLocality.equalsIgnoreCase(match.getMatch().locality)) {
                            clean++;
                            cleanWeighted += count;
                        }
                        final String searchName;
                        final Collection<String> names = match.getAcceptedCandidate().getNames();
                        if (expectedLocality != null) {
                            searchName = expectedLocality;
                        } else {
                            if (match.getIssues().contains(AlaLocationFactory.HIGHER_LOCALITY)) {
                                GeographyType geographyType = match.getAccepted().geographyType;
                                if (geographyType == GeographyType.island) {
                                    searchName = classification.island;
                                } else if (geographyType == GeographyType.islandGroup) {
                                    searchName = classification.islandGroup;
                                } else if (geographyType == GeographyType.stateProvince) {
                                    searchName = classification.stateProvince;
                                } else if (geographyType == GeographyType.country) {
                                    searchName = classification.country;
                                } else if (geographyType == GeographyType.continent) {
                                    searchName = classification.continent;
                                } else {
                                    searchName = classification.locality;
                                }
                            } else {
                                searchName = classification.locality;
                            }
                        }
                        if (searchName != null && names.stream().anyMatch(n -> searchName.equalsIgnoreCase(n))) {
                            accurate++;
                            accurateWeighted += count;
                        } else
                            logger.warn("Unexpected match on line {}, got {} {} not {} on {}", line, match.getAccepted().locality, match.getAccepted().locationId, searchName, row);
                    }
                    if (expectValid == match.isValid()) {
                        expected++;
                        expectedWeighted += count;
                    } else {
                        logger.info("Unexpected validity line {}, values {}", line, row);
                        if (match.isValid())
                            logger.warn("Matched line {} to {}: {}", line, match.getAccepted().locationId, match.getAccepted().locality);
                    }
                    if (flag) {
                        logger.info("Flag line {} as {}: {}", line, match.isValid() ? match.getMatch().locality : "invalid", row);
                    }
                } catch (Exception ex) {
                    logger.warn("Error on line " + line, ex);
                    errors++;
                    errorsWeighted += count;
                }
            }
            endTime = System.currentTimeMillis();
            double rate = (matched * 1000.0) / (endTime - startTime);
            double successRate = (success * 100.0) / matched;
            double expectedRate = (expected * 100.0) / matched;
            double accurateRate = (accurate * 100.0) / matched;
            double cleanRate = (clean * 100.0) / matched;
            logger.info("Processed " + matched + " entries, " + success + " successful, " + expected + " expected, " + accurate + " accurate, " + clean + " clean, " + errors + " errors");
            logger.info("Processing rate " + rate + " macthes per second");
            logger.info("Successful match rate " + successRate);
            logger.info("Expected match rate " + expectedRate);
            logger.info("Accurate match rate " + accurateRate);
            logger.info("Clean match rate " + cleanRate);
            logger.info("Issues");
            for (Map.Entry<Term, Integer> entry : issueCount.entrySet()) {
                Term issue = entry.getKey();
                Integer ic = entry.getValue();
                logger.info(issue.toString() + ": " + ic + ", " + (ic * 100.0) / matched + "%");
            }
            successRate = (successWeighted * 100.0) / matchedWeighted;
            expectedRate = (expectedWeighted * 100.0) / matchedWeighted;
            accurateRate = (accurateWeighted * 100.0) / matchedWeighted;
            cleanRate = (cleanWeighted * 100.0) / matchedWeighted;
            logger.info("Processed weighted " + matchedWeighted + " entries, " + successWeighted + " successful, " + expectedWeighted + " expected, " + accurateWeighted + " accurate, " + cleanWeighted + " clean, " + errorsWeighted + " errors");
            logger.info("Weighted successful match rate " + successRate);
            logger.info("Weighted expected match rate " + expectedRate);
            logger.info("Weighted accurate match rate " + accurateRate);
            logger.info("Weighted clean match rate " + cleanRate);
            logger.info("Weighted issues");
            for (Map.Entry<Term, Integer> entry : issueCountWeighted.entrySet()) {
                Term issue = entry.getKey();
                Integer ic = entry.getValue();
                logger.info(issue.toString() + ": " + ic + ", " + (ic * 100.0) / matchedWeighted + "%");
            }
            StringWriter sw = new StringWriter();
            this.matcher.reportStatistics(sw);
            logger.info(sw.toString());
        } finally {
            if (s != null)
                s.close();
        }
        assertEquals(0, errors);
        assertEquals(matched, expected);
        assertEquals(success, accurate);
    }

    protected int indexOf(String[] header, String column) {
        for (int i = 0; i < header.length; i++)
            if (column.equalsIgnoreCase(header[i]))
                return i;
        return -1;
    }

    // Sample locations from 1.6 million ALA records
    @Test
    public void testPerfomance1() throws Exception {
        this.testFile("sampled-locations-1.csv");
    }

    // Sample locations from 42 million ALA records
    @Test
    public void testPerfomance2() throws Exception {
        this.testFile("sampled-locations-2.csv");
    }

}
