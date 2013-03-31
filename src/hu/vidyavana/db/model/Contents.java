package hu.vidyavana.db.model;

import hu.vidyavana.db.api.Db;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.*;

@Entity
public class Contents
{
	@PrimaryKey
	public BookOrdinalKey key;

	public int level;
	public String division;
	public String title;
	public int bookParaOrdinal;
	
	
	public Contents()
	{
	}


	public Contents(int bookId, int bookTocOrdinal, int level, 
	                String division, String title, int bookParaOrdinal)
	{
		this.key = new BookOrdinalKey(bookId, bookTocOrdinal);
		this.level = level;
		this.division = division;
		this.title = title;
		this.bookParaOrdinal = bookParaOrdinal;
	}
	
	
	public static PrimaryIndex<BookOrdinalKey, Contents> pkIdx()
	{
		return Db.store().getPrimaryIndex(BookOrdinalKey.class, Contents.class);
	}
}
