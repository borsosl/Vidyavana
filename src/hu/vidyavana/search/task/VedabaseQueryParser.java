package hu.vidyavana.search.task;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import hu.vidyavana.db.api.QueryAnalyzer;
import hu.vidyavana.db.api.SeparateQueryOperatorFilter;

public class VedabaseQueryParser
{
	private static Analyzer analyzer = new QueryAnalyzer();
	
	
	public static Query parse(String s)
	{
		List<String> words = analyze(s);
		BooleanQuery.Builder bqb = new BooleanQuery.Builder();
		for(String word : words)
		{
			if(SeparateQueryOperatorFilter.OPERATOR_CHAR.matcher(word).find())
				continue;
			Term t = new Term("text", word);
			Query q;
			int ixAst = word.indexOf('*');
			int ixQm = word.indexOf('?');
			if((ixAst > 1 && (ixQm == -1 || ixQm > 1)) || ixQm > 1 && ixAst == -1)
				q = new WildcardQuery(t);
			else
				q = new TermQuery(t);
			bqb.add(q, BooleanClause.Occur.MUST);
		}
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
