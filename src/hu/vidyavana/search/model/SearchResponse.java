package hu.vidyavana.search.model;

import java.util.List;
import hu.vidyavana.db.model.DisplayBlock;

public class SearchResponse
{
	public int id;
	public int hitCount;
	public int hit;
	public DisplayBlock display;
	public List<HitResponse> hits;
	public int startHit;
}
