package hu.vidyavana.search.api;

import hu.vidyavana.convert.api.DiacriticLowercase;

public class QueryTokenizer extends TransliterationTokenizer
{
	public static final String queryCharStr = "*\"-!|?()/~^";
	
	static boolean isQueryChar(int c)
	{
		for(int i=0; i<queryCharStr.length(); ++i)
			if(queryCharStr.charAt(i) == c)
				return true;
		return false;
	}
	
	@Override
	protected boolean isTokenChar(int c)
	{
		return DiacriticLowercase.chr(c) != 0 || isQueryChar(c);
	}

}
