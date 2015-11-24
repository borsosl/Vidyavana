package test.db.lucene;

import static org.junit.Assert.*;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import hu.vidyavana.db.api.DiscardingHtmlCharFilter;
import hu.vidyavana.db.api.HtmlAnalyzer;

public class AnalysisTest
{
	@Test
	public void testHtmlReader() throws Exception
	{
		checkHtmlReader("a b c", "a b c");
		checkHtmlReader("abc<b>inrg&nbsp;gnero", "abc inrg gnero");
		checkHtmlReader("<i>abc<b>inrg&nbsp;gnero", " abc inrg gnero");
		checkHtmlReader("abc<b>inrg&nbsp;gnero<i>", "abc inrg gnero ");
		checkHtmlReader("&frg;abc<b>&amp;inrg&nbsp;gnero", " abc &inrg gnero");
		checkHtmlReader("abc<b>inrg&nbsp;gnero&grt;", "abc inrg gnero ");
		checkHtmlReader("<i>abc<b> inrg&nbsp;gnero</i>", " abc  inrg gnero ");
	}
	
	
	private void checkHtmlReader(String in, String out) throws Exception
	{
		Map<String, Character> entityMap = new HashMap<String, Character>() {{
			put("amp", '&');
			put("lt", '<');
			put("gt", '>');
		}};
		try(DiscardingHtmlCharFilter r = new DiscardingHtmlCharFilter(new StringReader(in), entityMap))
		{
			StringBuilder sb = new StringBuilder();
			int c;
			while((c = r.read()) != -1)
				sb.append((char) c);
			assertEquals(out, sb.toString());
		}
	}
	
	
	@Test
	public void simpleWords() throws Exception
	{
		checkIndexedTerms("a b c", new String[]{"a", "b", "c"});
		checkIndexedTerms("<i>dharma-kṣetre kuru-kṣetre  samavetā yuyutsavaḥ<br/>māmakāḥ pāṇḍavāś caiva  kim\r\n" + 
			"        akurvata sañjaya</i>", new String[]{"dharma", "kṣetre", "kuru", "kṣetre", "samavetā", "yuyutsavaḥ", "māmakāḥ", "pāṇḍavāś", "caiva", "kim", "akurvata", "sañjaya"});
		checkIndexedTerms("<b>Dhṛtarāṣṭra így szólt: Óh, Sañjaya, mit tettek fiaim és Pāṇḍu fiai",
			new String[]{"dhṛtarāṣṭra", "így", "szólt", "óh", "sañjaya", "mit", "tettek", "fiaim", "és", "pāṇḍu", "fiai"});
	}
	
	
	private void checkIndexedTerms(String in, String[] out) throws Exception
	{
		try(Analyzer analyzer = new HtmlAnalyzer())
		{
			try(TokenStream ts = analyzer.tokenStream("foo", new StringReader(in)))
			{
				CharTermAttribute termAttr = ts.addAttribute(CharTermAttribute.class);
				ts.reset();
				int ix = 0;
				while(ts.incrementToken())
					assertEquals(termAttr.toString(), out[ix++]);

				assertEquals(ix, out.length);
				ts.end();
			}
		}
	}
}
