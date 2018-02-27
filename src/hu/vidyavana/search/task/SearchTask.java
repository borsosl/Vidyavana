package hu.vidyavana.search.task;

import hu.vidyavana.search.api.Lucene;
import hu.vidyavana.search.model.Hit;
import hu.vidyavana.search.model.Search;
import hu.vidyavana.search.model.Search.Order;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;

public class SearchTask implements Runnable
{
	private Search details;


	public SearchTask(Search details)
	{
		this.details = details;
	}


	@Override
	public void run()
	{
		details.hitCount = 0;
		try
		{
			IndexSearcher sr = Lucene.SYSTEM.searcher();
			Query query = VedabaseQueryParser.parse(details.queryStr, details.bookAccess, details.order);
			TopDocs res = sr.search(query, details.fetchHits,
				details.order == Order.Score ? Sort.RELEVANCE : Sort.INDEXORDER);
			if(res.totalHits > 0)
			{
				details.hitCount = res.totalHits;
				details.hits = new ArrayList<>(Math.min(details.fetchHits, res.totalHits));
				for(ScoreDoc sd : res.scoreDocs)
				{
					Hit hit = new Hit(sd.doc);
					if(details.hits.size() < details.reqHits)
					{
						Document doc = sr.doc(sd.doc);
						hitDataFromDoc(doc, hit);
					}
					details.hits.add(hit);
				}
			}
		}
		catch(BooleanQuery.TooManyClauses ex) {
			details.errorCode = Search.ErrorCode.TOO_MANY_WILDCARD_HITS;
		}
		catch(IOException ex) {
			throw new RuntimeException("Lucene search: "+details.queryStr, ex);
		}
		
	}
	
	
	public static void hitDataFromDoc(Document doc, Hit hit)
	{
		hit.plainBookId = (Integer) doc.getField("bookId").numericValue();
		hit.segment = (Integer) doc.getField("segment").numericValue();
		hit.ordinal = (Integer) doc.getField("ordinal").numericValue();
	}
}
