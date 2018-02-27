package hu.vidyavana.search.model;

import hu.vidyavana.db.model.BookAccess;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Search implements Serializable
{
	public enum Order
	{
		Index,
		Score
	}

	public enum ErrorCode {
		TOO_MANY_WILDCARD_HITS("A helyettesítő karakterhez túl sok változat tartozik, fontossági sorrend nem választható.");

		public String text;

		ErrorCode(String text) {
			this.text = text;
		}
	}


	public int id;
	public String user;
	public Date lastAccess = new Date();
	public String queryStr;
	public int startHit;
	public int reqHits = 20;
	public int fetchHits = 100;
	public Order order = Order.Score;
	public int page = 1;
	public transient BookAccess bookAccess;

	public transient List<Hit> hits;
	public int hitCount;
	public ErrorCode errorCode;
}
