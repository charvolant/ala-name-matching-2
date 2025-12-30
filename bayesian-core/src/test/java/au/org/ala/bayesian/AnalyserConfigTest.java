package au.org.ala.bayesian;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import static org.junit.Assert.*;

public class AnalyserConfigTest {
    private static final File BASE = new File("/data/test");

    @Test
    public void testRelative1() throws Exception {
        AnalyserConfig config = AnalyserConfig.builder().build();
        AnalyserConfig relative = config.relative(BASE);
        assertNull(relative.getParameter("foo", null));
    }

    @Test
    public void testRelative2() throws Exception {
        URL special = new URL("https://somewhere.com/x.txt");
        AnalyserConfig config = AnalyserConfig.builder().build().with("foo", special);
        AnalyserConfig relative = config.relative(BASE);
        URL rebased = relative.getURL("foo", null);
        assertNotNull(rebased);
        assertEquals(special.toString(), rebased.toString());
    }

    @Test
    public void testRelative3() throws Exception {
        URL special = new URL("file:///notdata/test/x.txt");
        AnalyserConfig config = AnalyserConfig.builder().build().with("foo", special);
        AnalyserConfig relative = config.relative(BASE);
        URL rebased = relative.getURL("foo", null);
        assertNotNull(rebased);
        assertEquals(special.toString(), rebased.toString());
    }

    @Test
    public void testRelative4() throws Exception {
        URL special = new URL("file:///data/test/x.txt");
        AnalyserConfig config = AnalyserConfig.builder().build().with("foo", special);
        AnalyserConfig relative = config.relative(BASE);
        URL rebased = relative.getURL("foo", null);
        assertNotNull(rebased);
        assertEquals("file:x.txt", rebased.toString());
    }

    public void testAbsolute1() throws Exception {
        AnalyserConfig config = AnalyserConfig.builder().build();
        AnalyserConfig relative = config.absolute(BASE);
        assertNull(relative.getParameter("foo", null));
    }

    public void testAbsolute2() throws Exception {
        URL special = new URL("https://somewhere.com/x.txt");
        AnalyserConfig config = AnalyserConfig.builder().build().with("foo", special);
        AnalyserConfig absolute = config.absolute(BASE);
        URL rebased = absolute.getURL("foo", null);
        assertNotNull(rebased);
        assertEquals(special.toString(), rebased.toString());
    }

    public void testAbsolute3() throws Exception {
        URL special = new URL("file:///somewhere/x.txt");
        AnalyserConfig config = AnalyserConfig.builder().build().with("foo", special);
        AnalyserConfig absolute = config.absolute(BASE);
        URL rebased = absolute.getURL("foo", null);
        assertNotNull(rebased);
        assertEquals(special.toString(), rebased.toString());
    }

    public void testAbsolute4() throws Exception {
        URL special = new URL("file:x.txt");
        AnalyserConfig config = AnalyserConfig.builder().build().with("foo", special);
        AnalyserConfig absolute = config.absolute(BASE);
        URL rebased = absolute.getURL("foo", null);
        assertNotNull(rebased);
        assertEquals("file:///data/test/x.txt", rebased.toString());
    }

}
