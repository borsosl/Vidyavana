package test.convert;

import hu.vidyavana.convert.epub.BbtXhtmlFileProcessor;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class BbtXhtmlFileProcessorTest {

    @Test
    public void parseAttributesOneAttr() {
        Map<String, String> map = BbtXhtmlFileProcessor.parseAttributes("a= \"fzrh feij\" fwueifb");
        assertEquals(1, map.size());
        assertEquals("fzrh feij", map.get("a"));
    }

    @Test
    public void parseAttributesTwoAttr() {
        Map<String, String> map = BbtXhtmlFileProcessor.parseAttributes("a=\"fzrh feij\" fwueifb class =\"fbiweb beuiwbf\"");
        assertEquals(2, map.size());
        assertEquals("fzrh feij", map.get("a"));
        assertEquals("fbiweb beuiwbf", map.get("class"));
    }

    @Test
    public void classNamesSetOneClass() {
        Set<String> cls = BbtXhtmlFileProcessor.classNamesSet("  ufbe ");
        assertEquals(1, cls.size());
        assertEquals(true, cls.contains("ufbe"));
    }

    @Test
    public void classNamesSetTwoClasses() {
        Set<String> cls = BbtXhtmlFileProcessor.classNamesSet("  ufbe  fweiu ");
        assertEquals(2, cls.size());
        assertEquals(true, cls.contains("ufbe"));
        assertEquals(true, cls.contains("fweiu"));
    }
}