package hu.vidyavana.search.api;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

public class SeparateQueryOperatorFilter extends TokenStream
{
	public static final Pattern OPERATOR_CHAR = Pattern.compile("[-\"!|()/~^]");
	public static final String OPERATOR_TOKENS_STR = "\"-!|()/~^";
	
	private TokenStream input;
	private CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
	private PositionIncrementAttribute incrAttr = addAttribute(PositionIncrementAttribute.class);
	
	private String remaining;
	private AttributeSource.State prevState;


	public SeparateQueryOperatorFilter(TokenStream input)
	{
		super(input);
		this.input = input;
	}

	
	@Override
	public boolean incrementToken() throws IOException
	{
		if(remaining != null)
		{
			restoreState(prevState);
			int ix = operatorIndex(remaining);
			if(ix > -1)
			{
				int nextIx = ix == 0 ? 1 : ix;
				termAttr.setEmpty().append(remaining.substring(0, nextIx));
				incrAttr.setPositionIncrement(0);
				remaining = remaining.substring(nextIx);
			}
			else
			{
				termAttr.setEmpty().append(remaining);
				incrAttr.setPositionIncrement(0);
				remaining = null;
			}
			return true;
		}

		if(!input.incrementToken())
			return false;

		String term = termAttr.toString();
		int ix = operatorIndex(term);
		if(ix > -1)
		{
			int nextIx = ix == 0 ? 1 : ix;
			termAttr.setEmpty().append(term.substring(0, nextIx));
			incrAttr.setPositionIncrement(0);
			remaining = term.substring(nextIx);
			prevState = captureState();
		}
		return true;
	}

	
	private int operatorIndex(String token)
	{
		Matcher m = OPERATOR_CHAR.matcher(token);
		if(m.find())
			return m.start();
		return -1;
	}


	@Override
	public void reset() throws IOException
	{
		super.reset();
		input.reset();
		remaining = null;
	}
	
	
	@Override
	public void close() throws IOException
	{
		super.close();
		input.close();
	}
}
