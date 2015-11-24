package hu.vidyavana.db.api;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

public class HtmlAnalyzer extends Analyzer
{

	@Override
	protected TokenStreamComponents createComponents(String fieldName)
	{
		Tokenizer tokenizer = new TransliterationTokenizer();
		return new TokenStreamComponents(tokenizer, tokenizer);
	}


	@Override
	protected Reader initReader(String fieldName, Reader reader)
	{
		return new DiscardingHtmlCharFilter(reader, null);
	}
}
