package hu.vidyavana.search.task;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import hu.vidyavana.db.api.QueryAnalyzer;

public class VedabaseQueryParser
{
	private static Analyzer analyzer = new QueryAnalyzer();
	
	
	public static Query parse(String q)
	{
		List<String> words = analyze(q);
		BooleanQuery.Builder bqb = new BooleanQuery.Builder();
		for(String word : words)
			bqb.add(new TermQuery(new Term("text", word)), BooleanClause.Occur.MUST);
		return bqb.build();
	}
	
	
	static List<String> analyze(String q)
	{
		List<String> list = new ArrayList<>();
		try(TokenStream ts = analyzer.tokenStream("foo", new StringReader(q)))
		{
			CharTermAttribute termAttr = ts.addAttribute(CharTermAttribute.class);
			ts.reset();
			while(ts.incrementToken())
				list.add(termAttr.toString());
			ts.end();
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Analyzing: "+q, ex);
		}
		return list;
	}
}
