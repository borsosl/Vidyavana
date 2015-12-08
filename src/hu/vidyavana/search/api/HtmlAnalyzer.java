package hu.vidyavana.search.api;

import java.io.Reader;
import java.util.Arrays;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;

public class HtmlAnalyzer extends Analyzer
{

	@Override
	protected TokenStreamComponents createComponents(String fieldName)
	{
		Tokenizer tokenizer = new TransliterationTokenizer();
		TokenStream filter = new StopFilter(tokenizer, new CharArraySet(Arrays.asList("a", "az", "és"), false));
		filter = new TransliterationSynonymFilter(filter);
		return new TokenStreamComponents(tokenizer, filter);
	}


	@Override
	protected Reader initReader(String fieldName, Reader reader)
	{
		return new DiscardingHtmlCharFilter(reader, null);
	}
}
