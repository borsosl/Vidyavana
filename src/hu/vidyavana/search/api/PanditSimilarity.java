package hu.vidyavana.search.api;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.DefaultSimilarity;

public class PanditSimilarity extends DefaultSimilarity
{
	@Override
	public float lengthNorm(FieldInvertState state)
	{
		final int numTerms;
		if(discountOverlaps)
			numTerms = state.getLength() - state.getNumOverlap();
		else
			numTerms = state.getLength();
		return state.getBoost() * ((float) (1.0 / (4.0 + Math.sqrt(numTerms / 50))));
	}


	@Override
	public float tf(float freq)
	{
		return freq / 2;
	}


	@Override
	public float idf(long docFreq, long numDocs)
	{
		return (float) (Math.log(numDocs / (double) (docFreq + 1))/10.0 + 1.0);
	}
}
