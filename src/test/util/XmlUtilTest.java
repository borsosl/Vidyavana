package test.util;

import hu.vidyavana.util.XmlToken;
import hu.vidyavana.util.XmlUtil;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class XmlUtilTest {

    @Test
    public void tokenizeTest() {
        String para = " <i>a bc a- b-c -d<b>c&nbsp;</b> d</i> ";
        String[] text = {"<i>", "a", "bc", "a", "-", "b", "-", "c", "-", "d", "<b>", "c", "&nbsp;", "</b>", "d", "</i>"};
        int[] start = {1, 4, 6, 9, 10, 12, 13, 14, 16, 17, 18, 21, 22, 28, 33, 34};
        int[] printPos = {0, 0, 2, 5, 6, 8, 9, 10, 12, 13, 14, 14, 15, 16, 17, 18};
        int[] len = {0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0};
        int[] flen = {0, 2, 3, 1, 2, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1};
        List<XmlToken> tokens = XmlUtil.tokenize(para);
        assertEquals(16, tokens.size());
        for(int i=0; i<text.length; ++i) {
            assertEquals(text[i], tokens.get(i).text);
            assertEquals(start[i], tokens.get(i).sourcePos);
            assertEquals(printPos[i], tokens.get(i).printPos);
            assertEquals(len[i], tokens.get(i).printLength);
            assertEquals(flen[i], tokens.get(i).fullLength());
        }
        assertEquals(true, tokens.get(0).isTag);
        assertEquals(false, tokens.get(1).isTag);
        assertEquals(true, tokens.get(12).isEntity);
        assertEquals(false, tokens.get(0).isEntity);
    }
}
