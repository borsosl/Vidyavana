package hu.vidyavana.search.model;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.lucene.search.Query;

public class Search
{
	public static enum Order
	{
		BookId,
		Score
	}
	
	private static final AtomicInteger SEARCH_ID = new AtomicInteger((int)((System.currentTimeMillis()/100)%100000));
	
	public final int id = SEARCH_ID.incrementAndGet();
	public String user;
	public Date lastAccess = new Date();
	public String queryStr;
	public int startHit;
	public int reqHits = 20;
	public int fetchHits = 100;
	public Order order;
	public int[] includedBooks;
	public int[] excludedBooks;

	public Query query;
	public List<Hit> hits;
	public int hitCount;
}
