package hu.vidyavana.search.model;

import hu.vidyavana.db.model.DisplayBlock;

import java.util.List;

public class SearchResponse
{
	public int id;
	public int hitCount;
	public int startHit;
	public int endHit;
	public DisplayBlock display;
	public List<HitResponse> hits;
}
