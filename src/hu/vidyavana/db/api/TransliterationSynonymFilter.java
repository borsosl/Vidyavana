package hu.vidyavana.db.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

public class TransliterationSynonymFilter extends TokenStream
{
	public static final Pattern HAS_TRANS = Pattern.compile("[āīūṣśṇḍḥṅñṁṛṭḷṝḹ]");
	public static final String TRANS = "āīūḍḥḷḹṁṅṇñṛṝṣśṭ";
	public static final String PLAIN = "aiudhllmnnnrrsst";
	
	private TokenStream input;
	private CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
	private PositionIncrementAttribute incrAttr = addAttribute(PositionIncrementAttribute.class);
	private Map<Character, Character> plainMap = new HashMap<>(23);
	
	private String synonym;
	private AttributeSource.State synonymState;


	public TransliterationSynonymFilter(TokenStream input)
	{
		super(input);
		this.input = input;
		for(int i=0; i<TRANS.length(); ++i)
			plainMap.put(TRANS.charAt(i), PLAIN.charAt(i));
	}

	
	@Override
	public boolean incrementToken() throws IOException
	{
		if(synonym != null)
		{
			restoreState(synonymState);
			termAttr.setEmpty().append(synonym);
			incrAttr.setPositionIncrement(0);
			synonym = null;
			return true;
		}

		if(!input.incrementToken())
			return false;

		String term = termAttr.toString();
		if(HAS_TRANS.matcher(term).find())
		{
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<term.length(); ++i)
			{
				char c = term.charAt(i);
				Character ch = plainMap.get(c);
				if(ch != null)
					c = ch;
				sb.append(c);
			}
			synonym = sb.toString();
			synonymState = captureState();
		}
		return true;
	}

	
	@Override
	public void reset() throws IOException
	{
		super.reset();
		input.reset();
		synonym = null;
	}
	
	
	@Override
	public void close() throws IOException
	{
		super.close();
		input.close();
	}
}
