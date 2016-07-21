package hu.vidyavana.search.api;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryAnalyzer extends Analyzer
{
	@Override
	protected TokenStreamComponents createComponents(String fieldName)
	{
		Tokenizer tokenizer = new QueryTokenizer();
		TokenStream filter = new SeparateQueryOperatorFilter(tokenizer);
		filter = new StopFilter(filter, new CharArraySet(Arrays.asList("a", "az", "és"), false));
		return new TokenStreamComponents(tokenizer, filter);
	}


	public static List<String> getTokens(String query) {
		List<String> list = new ArrayList<>();
		try(TokenStream ts = new QueryAnalyzer().tokenStream("foo", new StringReader(query))) {
			CharTermAttribute termAttr = ts.getAttribute(CharTermAttribute.class);
			ts.reset();
			while (ts.incrementToken()) {
				list.add(termAttr.toString());
			}
			ts.end();   // Perform end-of-stream operations, e.g. set the final offset.
		} catch (IOException e) {
			// empty list
		}
		return list;
	}
}
