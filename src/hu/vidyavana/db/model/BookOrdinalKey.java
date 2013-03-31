package hu.vidyavana.db.model;

import com.sleepycat.persist.model.*;

@Persistent
public class BookOrdinalKey
{
	@KeyField(1)
	public int bookId;
	@KeyField(2)
	public int ordinal;


	public BookOrdinalKey()
	{
	}


	public BookOrdinalKey(int bookId, int ordinal)
	{
		this.bookId = bookId;
		this.ordinal = ordinal;
	}
}
