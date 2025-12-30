package au.org.ala.names;

import au.org.ala.util.JsonUtils;
import au.org.ala.util.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ALANameSearcherConfigurationTest {
    @Test
    public void testToJSON1() throws Exception {
        ObjectMapper mapper = JsonUtils.createMapper();
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder().build();
        String expected = TestUtils.getResource(ALANameSearcherConfigurationTest.class, "searcher-config-1.json");
        String actual = mapper.writeValueAsString(configuration);
        TestUtils.compareNoSpaces(expected, actual);
    }

    @Test
    public void testToJSON2() throws Exception {
        ObjectMapper mapper = JsonUtils.createMapper();
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .version("20221017")
                .index(new File("/data/lucene"))
                .work(new File("/data/tmp/namematching"))
                .localities(Arrays.asList("Australia", "New Zealand", "Antarctica"))
                .build();
        String expected = TestUtils.getResource(ALANameSearcherConfigurationTest.class, "searcher-config-2.json");
        String actual = mapper.writeValueAsString(configuration);
        TestUtils.compareNoSpaces(expected, actual);
    }
    
    @Test
    public void testFromJSON1() throws Exception {
        ObjectMapper mapper = JsonUtils.createMapper();
        URL source = this.getClass().getResource("searcher-config-2.json");
        ALANameSearcherConfiguration configuration = mapper.readValue(source, ALANameSearcherConfiguration.class);
        assertEquals("20221017", configuration.getVersion());
        assertEquals(new File("/data/lucene"), configuration.getIndex());
        assertEquals(new File("/data/tmp/namematching"), configuration.getWork());
        assertEquals(Arrays.asList("Australia", "New Zealand", "Antarctica"), configuration.getLocalities());
        assertNotNull(configuration.getSearcherConfiguration());
        assertNotNull(configuration.getMatcherConfiguration());
    }

    @Test
    public void testGetLinnaean1() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder().build();
        assertEquals(new File("/data/namematching/linnaean"), configuration.getLinnaean());
    }

    @Test
    public void testGetLinnaean2() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .version("5")
                .build();
        assertEquals(new File("/data/namematching/linnaean-5"), configuration.getLinnaean());
    }

    @Test
    public void testGetLinnaean3() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .index(new File("/data/tmp"))
                .version("2022")
                .build();
        assertEquals(new File("/data/tmp/linnaean-2022"), configuration.getLinnaean());
    }

    @Test
    public void testGetLinnaean4() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .linnaean(new File("/data/somewhere"))
                .version("2022")
                .build();
        assertEquals(new File("/data/somewhere"), configuration.getLinnaean());
    }

    @Test
    public void testGetLinnaean5() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .linnaean(new File("somewhere"))
                .version("2022")
                .build();
        assertEquals(new File("/data/namematching/somewhere"), configuration.getLinnaean());
    }


    @Test
    public void testGetVernacular1() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder().build();
        assertEquals(new File("/data/namematching/vernacular"), configuration.getVernacular());
    }

    @Test
    public void testGetVernacular2() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .version("1.1")
                .build();
        assertEquals(new File("/data/namematching/vernacular-1.1"), configuration.getVernacular());
    }

    @Test
    public void testGetVernacular3() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .index(new File("/data/tmp"))
                .version("2022")
                .build();
        assertEquals(new File("/data/tmp/vernacular-2022"), configuration.getVernacular());
    }

    @Test
    public void testGetVernacular4() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .vernacular(new File("/data/v"))
                .version("2022")
                .build();
        assertEquals(new File("/data/v"), configuration.getVernacular());
    }

    @Test
    public void testGetVernacular5() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .vernacular(new File("sub/v"))
                .version("2022")
                .build();
        assertEquals(new File("/data/namematching/sub/v"), configuration.getVernacular());
    }


    @Test
    public void testGetLocation1() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder().build();
        assertEquals(new File("/data/namematching/location"), configuration.getLocation());
    }

    @Test
    public void testGetLocation2() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .version("2025-1-3")
                .build();
        assertEquals(new File("/data/namematching/location-2025-1-3"), configuration.getLocation());
    }

    @Test
    public void testGetLocation3() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .index(new File("/data/tmp"))
                .version("2025-1-3")
                .build();
        assertEquals(new File("/data/tmp/location-2025-1-3"), configuration.getLocation());
    }

    @Test
    public void testGetLocation4() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .location(new File("/tmp/loc"))
                .build();
        assertEquals(new File("/tmp/loc"), configuration.getLocation());
    }

    @Test
    public void testGetLocation5() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .index(new File("/data/elsewhere"))
                .location(new File("loc"))
                .build();
        assertEquals(new File("/data/elsewhere/loc"), configuration.getLocation());
    }

    @Test
    public void testGetSuggester1() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder().build();
        assertEquals(new File("/data/tmp/suggester"), configuration.getSuggester());
    }

    @Test
    public void testGetSuggester2() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .version("5")
                .build();
        assertEquals(new File("/data/tmp/suggester-5"), configuration.getSuggester());
    }

    @Test
    public void testGetSuggester3() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .work(new File("/tmp"))
                .version("2022")
                .build();
        assertEquals(new File("/tmp/suggester-2022"), configuration.getSuggester());
    }

    @Test
    public void testGetSuggester4() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .suggester(new File("/tmp/suggest"))
                .build();
        assertEquals(new File("/tmp/suggest"), configuration.getSuggester());
    }

    @Test
    public void testGetSuggester5() throws Exception {
        ALANameSearcherConfiguration configuration = ALANameSearcherConfiguration.builder()
                .suggester(new File("ss"))
                .build();
        assertEquals(new File("/data/tmp/ss"), configuration.getSuggester());
    }
}