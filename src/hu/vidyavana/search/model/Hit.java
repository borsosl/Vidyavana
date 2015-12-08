package hu.vidyavana.search.model;

public class Hit
{
	public int docId;
	public int plainBookId;
	public int segment;
	public int ordinal;

	
	public Hit(int docId)
	{
		this.docId = docId;
	}
}
