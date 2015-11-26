package hu.vidyavana.db.api;

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
		Tokenizer tokenizer = new TransliterationTokenizer();
		TokenStream filter = new StopFilter(tokenizer, new CharArraySet(Arrays.asList("a", "az", "és"), false));
		return new TokenStreamComponents(tokenizer, filter);
	}
}
