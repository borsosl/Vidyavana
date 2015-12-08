package hu.vidyavana.search.api;

import java.util.Arrays;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;

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
}
