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
import hu.vidyavana.db.api.QueryAnalyzer;

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
		checkIndexedTerms("a b c", new String[]{"b", "c"});
		checkIndexedTerms("<i>dharma-kṣetre kuru-kṣetre  samavetā yuyutsavaḥ<br/>māmakāḥ pāṇḍavāś caiva  kim\r\n" + 
			"        akurvata sañjaya</i>", new String[]{"dharma", "kṣetre", "ksetre", "kuru", "kṣetre", "ksetre",
				"samavetā", "samaveta", "yuyutsavaḥ", "yuyutsavah", "māmakāḥ", "mamakah", "pāṇḍavāś", "pandavas", "caiva",
				"kim", "akurvata", "sañjaya", "sanjaya"});
		checkIndexedTerms("<b>Dhṛtarāṣṭra így szólt: Óh, Sañjaya, mit tettek fiaim és Pāṇḍu fiai",
			new String[]{"dhṛtarāṣṭra", "dhrtarastra", "így", "szólt", "óh", "sañjaya", "sanjaya", "mit", "tettek", "fiaim",
				"pāṇḍu", "pandu", "fiai"});
	}
	
	
	@Test
	public void queries() throws Exception
	{
		checkQueryTerms("a b c", new String[]{"b", "c"});
		checkQueryTerms("a be* c?d", new String[]{"be*", "c?d"});
		checkQueryTerms("a -be* c?d", new String[]{"-", "be*", "c?d"});
		checkQueryTerms("-be*|a|c?d", new String[]{"-", "be*", "|", "|", "c?d"});
	}
	
	
	private void checkIndexedTerms(String in, String[] out) throws Exception
	{
		try(Analyzer analyzer = new HtmlAnalyzer())
		{
			checkTerms(analyzer, in, out);
		}
	}
	
	
	private void checkQueryTerms(String in, String[] out) throws Exception
	{
		try(Analyzer analyzer = new QueryAnalyzer())
		{
			checkTerms(analyzer, in, out);
		}
	}
	
	
	private void checkTerms(Analyzer analyzer, String in, String[] out) throws Exception
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
