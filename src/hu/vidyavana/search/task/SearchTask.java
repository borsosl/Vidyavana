package hu.vidyavana.search.task;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import hu.vidyavana.search.api.Lucene;
import hu.vidyavana.search.model.Hit;
import hu.vidyavana.search.model.Search;

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
		try
		{
			IndexSearcher sr = Lucene.SYSTEM.searcher();
			details.query = VedabaseQueryParser.parse(details.queryStr);
			TopDocs res = sr.search(details.query, details.fetchHits);
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
		catch(IOException ex)
		{
			throw new RuntimeException("Lucene search: "+details.queryStr, ex);
		}
		
	}
	
	
	public static void hitDataFromDoc(Document doc, Hit hit)
	{
		hit.bookId = (Integer)((StoredField) doc.getField("bookId")).numericValue();
		hit.segment = (Integer)((StoredField) doc.getField("segment")).numericValue();
		hit.ordinal = (Integer)((StoredField) doc.getField("ordinal")).numericValue();
	}
}
