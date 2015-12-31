package hu.vidyavana.search.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Search implements Serializable
{
	public static enum Order
	{
		BookId,
		Score
	}
	
	public int id;
	public String user;
	public Date lastAccess = new Date();
	public String queryStr;
	public int startHit;
	public int reqHits = 20;
	public int fetchHits = 100;
	public Order order;
	public transient BookAccess bookAccess;

	public transient List<Hit> hits;
	public int hitCount;
}
