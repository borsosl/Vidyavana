package hu.vidyavana.search.api;

import hu.vidyavana.convert.api.DiacriticLowercase;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

public class TransliterationTokenizer extends CharTokenizer
{
	public TransliterationTokenizer()
	{
	}


	public TransliterationTokenizer(AttributeFactory factory)
	{
		super(factory);
	}


	@Override
	protected boolean isTokenChar(int c)
	{
		return DiacriticLowercase.chr(c) != 0;
	}
	
	
	@Override
	protected int normalize(int c)
	{
		int ch = DiacriticLowercase.chr(c);
		if(ch == 0)
			ch = c;
		return ch;
	}
}
