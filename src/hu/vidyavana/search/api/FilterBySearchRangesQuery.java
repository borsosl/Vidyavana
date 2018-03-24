package hu.vidyavana.search.api;

import hu.vidyavana.search.model.SearchRange;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RandomAccessWeight;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.List;

public class FilterBySearchRangesQuery extends Query
{
	protected String numericDocValueFieldName;
	protected long[] rangeStart;
	protected long[] rangeEnd;


	public FilterBySearchRangesQuery(String numericDocValueFieldName, List<SearchRange> ranges)
	{
		this.numericDocValueFieldName = numericDocValueFieldName;
		rangeStart = new long[ranges.size()];
		rangeEnd = new long[ranges.size()];
		int ix = 0;
		for(SearchRange sr : ranges) {
			rangeStart[ix] = sr.fromRangeFilterOrdinal();
			rangeEnd[ix++] = sr.toRangeFilterOrdinal();
		}
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
						long val = values.get(index);
						for (int i = 0; i < rangeStart.length; i++) {
							if(val >= rangeStart[i] && val < rangeEnd[i])
								return true;
						}
						return false;
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
