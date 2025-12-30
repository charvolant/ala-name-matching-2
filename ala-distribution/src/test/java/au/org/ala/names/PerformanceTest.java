package au.org.ala.names;

import au.org.ala.bayesian.Match;
import au.org.ala.bayesian.MatchMeasurement;
import au.org.ala.bayesian.MatchOptions;
import au.org.ala.location.AlaLocationClassification;
import au.org.ala.util.JsonUtils;
import au.org.ala.vocab.TaxonomicStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gbif.dwc.terms.Term;
import org.gbif.nameparser.api.Rank;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that well-known and common species are correctly found
 */
public class PerformanceTest {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    public static final int SEARCH_QUERY_LIMIT = 15;
    public static final int SEARCH_CACHE_SIZE = 10000;
    public static final String INDEX = "/data/lucene/linnaean-2025-1-3" ;
    public static final String VERNACULAR_INDEX = "/data/lucene/vernacular-2025-1-3" ;
    public static final String LOCATION_INDEX = "/data/lucene/location-20230725-5";
    public static final String SUGGESTER_INDEX = "/data/tmp/suggester-2025-1-3";

    /**
     * The potential columns in the source data.
     * Not all are used but are present so that more sophisticated matching algorithms can be tested in the future.
     */
    private static final String[] COLUMNS = new String[]{
            "count", // Number of instances with this raw input
            "match", // Expected match name
            "accepted", // Expected accepted name
            "valid", // Expected validity
            "scientificName",
            "scientificNameAuthorship",
            "vernacularName",
            "taxonRank",
            "kingdom",
            "phylum",
            "class",
            "order",
            "family",
            "genus",
            "continent",
            "waterBody",
            "islandGroup",
            "island",
            "country",
            "countryCode",
            "stateProvince",
            "cl2013",  // ASGS Australian States and Territories
            "cl927", // States including coastal waters
            "cl2116", // NZ Land Registration Districts
            "cl22", // Australian States and Territories
            "cl1048", // IBRA 7 Regions
            "cl21", // IMCRA 4 Regions
            "cl932", // World Country Boundaries
            "cl914", // IBRA 6 Sub Regions
            "cl20", // IBRA 6 Regions
            "cl913", // Upper Hunter Focus Area v2
            "cl1049", // IBRA 7 Subregions
            "cl2117", // NZ Land Provinces
            "cl966" // IMCRA Meso-scale Bioregions
    };

    private ALANameSearcher searcher;
    private RankAnalysis rankAnalysis = new RankAnalysis();

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = JsonUtils.createMapper();
        ALANameSearcherConfiguration config = mapper.readValue(PerformanceTest.class.getResource("searcher-config-performance.json"), ALANameSearcherConfiguration.class);
        this.searcher = new ALANameSearcher(config);
    }

    @After
    public void tearDown() throws Exception {
        this.searcher.close();
    }

    private Map<String, Integer> mapHeader(String[] header) {
        return IntStream.range(0, header.length).mapToObj(i -> new Integer(i)).collect(Collectors.toMap(i -> header[i], i -> i));
    }

    private String getValue(String[] row, Map<String, Integer> headerMap, String... columns) {
        for (String column: columns) {
            Integer p = headerMap.get(column);
            if (p == null || p < 0 || p >= row.length)
                continue;
            String value = StringUtils.trimToNull(row[p]);
            if (value != null)
                return value;
        }
        return null;
    }

    private void testFile(String name) throws Exception {
        InputStream s = null;
        int errors = 0;
        int matched = 0;
        int succcess = 0;
        int expected = 0;
        int accurate = 0;
        int clean = 0;
        int errorsWeighted = 0;
        int matchedWeighted = 0;
        int succcessWeighted = 0;
        int expectedWeighted = 0;
        int accurateWeighted = 0;
        int cleanWeighted = 0;
        Map<Term, Integer> issueCount = new HashMap<>();
        Map<Pair<TaxonomicStatus, TaxonomicStatus>, Integer> statusCount = new HashMap<>();
        long startTime, endTime;
        try {
            s = this.getClass().getResourceAsStream(name);
            CSVReader reader = CSVReaderFactory.build(s, StandardCharsets.UTF_8.name(), ",", '"', 0);
            String[] header = reader.next();
            Map<String, Integer> headerMap = this.mapHeader(header);
            int line = 1;
            startTime = System.currentTimeMillis();
            while (reader.hasNext()) {
                String row[] = reader.next();
                line++;
                if (row[0].startsWith("#")) // Skip comment
                    continue;

                // Get count, validity and expected match
                String cv = this.getValue(row, headerMap, "count");
                int count = cv == null ? 1 : Integer.parseInt(cv);
                String valid = this.getValue(row, headerMap, "valid");
                boolean expectValid = false;
                boolean flag = false;
                expectValid = valid != null && !valid.equalsIgnoreCase("false") && !valid.equalsIgnoreCase("problem");
                flag = valid != null && (valid.equalsIgnoreCase("flag") || valid.equalsIgnoreCase("problem"));
                String expectedScientificName = this.getValue(row, headerMap, "match");

                // First see if we can get a location
                AlaLocationClassification location = new AlaLocationClassification();
                location.stateProvince = this.getValue(row, headerMap, "stateProvince", "cl2013", "cl2117", "cl22", "cl927");
                location.country = this.getValue(row, headerMap, "country", "cl932");
                location.continent = this.getValue(row, headerMap, "continent");
                location.island = this.getValue(row, headerMap, "island");
                location.islandGroup = this.getValue(row, headerMap, "islandGroup");
                location.waterBody = this.getValue(row, headerMap, "waterBody");
                Set<String> locationIds = null;
                if (location.locality != null || location.stateProvince != null || location.country != null || location.continent != null || location.island != null || location.islandGroup != null || location.waterBody != null) {
                    Match<AlaLocationClassification, MatchMeasurement> locationMatch = this.searcher.search(location);
                    if (locationMatch.isValid()) {
                        locationIds = locationMatch.getAllIdentifiers();
                    }
                }

                // Then get a name match
                AlaLinnaeanClassification linnaean = new AlaLinnaeanClassification();
                linnaean.locationId = locationIds;
                try {
                    linnaean.scientificName = this.getValue(row, headerMap, "scientificName");
                    linnaean.scientificNameAuthorship = this.getValue(row, headerMap, "scientificNameAuthorship");
                    linnaean.kingdom = this.getValue(row, headerMap, "kingdom");
                    linnaean.phylum = this.getValue(row, headerMap, "phylum");
                    linnaean.class_ = this.getValue(row, headerMap, "class");
                    linnaean.order = this.getValue(row, headerMap, "order");
                    linnaean.family = this.getValue(row, headerMap, "family");
                    linnaean.genus = this.getValue(row, headerMap, "genus");
                    linnaean.taxonRank = this.rankAnalysis.fromString(this.getValue(row, headerMap, "taxonRank"), null);
                    expectedScientificName = this.getValue(row, headerMap, "match");
                } catch (Exception ex) {
                    throw new IllegalStateException("Error on line " + line, ex);
                }
                matched++;
                matchedWeighted += count;
                try {
                    linnaean.inferForSearch(this.searcher.getMatcher().getAnalyser(), MatchOptions.ALL);
                    Rank expectedRank = linnaean.taxonRank;
                    Match<AlaLinnaeanClassification, MatchMeasurement> match = this.searcher.search(linnaean.clone());
                    for (Term issue : match.getIssues()) {
                        int ic = issueCount.getOrDefault(issue, 0);
                        issueCount.put(issue, ic + 1);
                    }
                    if (match.isValid()) {
                        succcess++;
                        succcessWeighted += count;
                        final Pair<TaxonomicStatus, TaxonomicStatus> sp = Pair.of(match.getMatch().taxonomicStatus, match.getAccepted().taxonomicStatus);
                        int sc = statusCount.getOrDefault(sp, 0);
                        statusCount.put(sp, sc + 1);
                        String cleanName = linnaean.scientificName;
                        if (cleanName.equalsIgnoreCase(match.getMatch().scientificName)) {
                            clean++;
                            cleanWeighted += count;
                        }
                        final String searchName;
                        final Collection<String> names = match.getCandidate().getNames();
                        if (expectedScientificName != null) {
                            searchName = expectedScientificName;
                        } else {
                             if (match.getIssues().contains(AlaLinnaeanFactory.HIGHER_ORDER_MATCH)) {
                                Rank matchRank = match.getAccepted().taxonRank;
                                if (matchRank == Rank.SPECIES) {
                                    searchName = linnaean.genus + " " + linnaean.specificEpithet;
                                } else if (matchRank == Rank.GENUS) {
                                    searchName = linnaean.genus;
                                } else if (matchRank == Rank.FAMILY) {
                                    searchName = linnaean.family;
                                } else if (matchRank == Rank.ORDER) {
                                    searchName = linnaean.order;
                                } else {
                                    searchName = linnaean.scientificName;
                                }
                            } else {
                                 if (linnaean.scientificName != null && linnaean.scientificNameAuthorship != null && linnaean.scientificName.endsWith(linnaean.scientificNameAuthorship) && !linnaean.scientificName.equals(linnaean.scientificNameAuthorship)) {
                                     searchName = linnaean.scientificName.substring(0, linnaean.scientificName.length() - linnaean.scientificNameAuthorship.length()).trim();
                                 } else {
                                     searchName = linnaean.scientificName;
                                 }
                              }
                        }
                        if (searchName != null && names.stream().anyMatch(n -> searchName.equalsIgnoreCase(n))) {
                            accurate++;
                            accurateWeighted += count;
                        } else
                            logger.warn("Unexpected match on line {}, got <<{}>> - {} - <<{}>> not <<{}>> on {}", line, match.getMatch().scientificName, match.getMatch().taxonId, match.getAccepted().scientificName, searchName, row);
                    }
                    if (expectValid == match.isValid()) {
                        expected++;
                        expectedWeighted += count;
                    } else {
                        logger.info("Unexpected validity line {}, values {}", line, row);
                        if (match.isValid())
                            logger.warn("Matched line {} to {}: {}", line, match.getAccepted().taxonId, match.getAccepted().scientificName);
                    }
                    if (flag) {
                        logger.info("Flag line {} as {}: {}", line, match.isValid() ? match.getMatch().scientificName : "invalid", row);
                    }
                } catch (Exception ex) {
                    logger.warn("Error on line " + line, ex);
                    errors++;
                    errorsWeighted += count;
                }
            }
            endTime = System.currentTimeMillis();
            double rate = (matched * 1000.0) / (endTime - startTime);
            double successRate = (succcess * 100.0) / matched;
            double expectedRate = (expected * 100.0) / matched;
            double accurateRate = (accurate * 100.0) / matched;
            double cleanRate = (clean * 100.0) / matched;
            double successRateWeighted = (succcessWeighted * 100.0) / matchedWeighted;
            double expectedRateWeighted = (expectedWeighted * 100.0) / matchedWeighted;
            double accurateRateWeighted = (accurateWeighted * 100.0) / matchedWeighted;
            double cleanRateWeighted = (cleanWeighted * 100.0) / matchedWeighted;
            logger.info("Processed " + matched + " entries, " + succcess + " successful, " + expected + " expected, " + accurate + " accurate, " + clean + " clean, " + errors + " errors");
            logger.info("Processed weighted " + matchedWeighted + " entries, " + succcessWeighted + " successful, " + expectedWeighted + " expected, " + accurateWeighted + " accurate, " + cleanWeighted + " clean, " + errorsWeighted + " errors");
            logger.info("Processing rate " + rate + " macthes per second");
            logger.info("Successful match rate " + successRate + " weighted " + successRateWeighted);
            logger.info("Expected match rate " + expectedRate + " weighted " + expectedRateWeighted);
            logger.info("Accurate match rate " + accurateRate + " weighted " + accurateRateWeighted);
            logger.info("Clean match rate " + cleanRate + " weighted " + cleanRateWeighted);
            logger.info("Match Taxonomic Status");
            for (Map.Entry<Pair<TaxonomicStatus, TaxonomicStatus>, Integer> entry : statusCount.entrySet()) {
                logger.info("  " + entry.getKey().toString() + ": " + entry.getValue());
            }
            logger.info("Issues");
            for (Map.Entry<Term, Integer> entry : issueCount.entrySet()) {
                logger.info(entry.getKey().toString() + ": " + entry.getValue());
            }
            StringWriter sw = new StringWriter();
            this.searcher.getMatcher().reportStatistics(sw);
            logger.info(sw.toString());
        } finally {
            if (s != null)
                s.close();
        }
        assertEquals(0, errors);
        assertEquals(matched, expected);
        assertEquals(succcess, accurate);
    }

    @Test
    public void testPerfomance1() throws Exception {
        this.testFile("sampled_names_2021-1.csv");
    }

    @Test
    public void testPerfomance2() throws Exception {
        this.testFile("sampled_names_2021-2.csv");
    }

    @Test
    public void testPerfomance3() throws Exception {
        this.testFile("sampled-names-locations-2022-1.csv");
    }

    @Test
    public void testPerfomance4() throws Exception {
        this.testFile("sampled-names-locations-problems.csv");
    }
}
