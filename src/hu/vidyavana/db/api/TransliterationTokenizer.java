package hu.vidyavana.db.api;

import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

public class TransliterationTokenizer extends CharTokenizer
{
	public static final String LOWER = "áéíóöőúüűāīūḍḥḷḹṁṅṇñṛṝṣśṭ";
	public static final String UPPER = "ÁÉÍÓÖŐÚÜŰĀĪŪḌḤḶḸṀṄṆÑṚṜṢŚṬ";
	
	static Map<Integer, Integer> lowerMap = new HashMap<>(71);
	
	static
	{
		for(int i=0; i<LOWER.length(); ++i)
		{
			int low = LOWER.charAt(i);
			int upp = UPPER.charAt(i);
			lowerMap.put(low, low);
			lowerMap.put(upp, low);
		}
	}
	
	
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
		return lowercase(c) != 0;
	}
	
	
	@Override
	protected int normalize(int c)
	{
		int ch = lowercase(c);
		if(ch == 0)
			ch = c;
		return ch;
	}
	
	
	public static int lowercase(int c)
	{
		if(c >= 'a' && c <= 'z' || c >= '0' && c <= '9');
		else if(c >= 'A' && c <= 'Z')
			c += 32;
		else
		{
			Integer ch = lowerMap.get(c);
			if(ch == null)
				if(Character.isAlphabetic(c))
					c = Character.toLowerCase(c);
				else
					c = 0;
			else
				c = ch;
		}
		return c;
	}
}
