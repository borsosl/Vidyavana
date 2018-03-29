package hu.vidyavana.search.api;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RandomAccessWeight;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;

import java.io.IOException;

public class FilterByParagraphTypesQuery extends Query
{
	protected String numericDocValueFieldName;
	int paraTypesBits;


	public FilterByParagraphTypesQuery(String numericDocValueFieldName, int paraTypesBits)
	{
		this.numericDocValueFieldName = numericDocValueFieldName;
		this.paraTypesBits = paraTypesBits;
	}

	@Override
	public Weight createWeight(IndexSearcher searcher, boolean needsScores)
	{
		return new RandomAccessWeight(this)
		{
			@Override
			protected Bits getMatchingDocs(LeafReaderContext context) throws IOException
			{
				final int len = context.reader().maxDoc();
				final NumericDocValues values = context.reader().getNumericDocValues(numericDocValueFieldName);
				return new Bits()
				{
					@Override
					public boolean get(int index)
					{
						int val = (int) values.get(index);
						return (val & paraTypesBits) > 0;
					}

					@Override
					public int length()
					{
						return len;
					}
				};
			}
		};
	}

	
	@Override
	public String toString(String field)
	{
		return "(filter "+numericDocValueFieldName+" by set)";
	}
}
