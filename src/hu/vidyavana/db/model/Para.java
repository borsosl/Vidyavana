package hu.vidyavana.db.model;

import hu.vidyavana.db.api.Db;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.*;

@Entity
public class Para
{
	@PrimaryKey
	public BookOrdinalKey key;

	public int style;
	public byte[] text;


	public Para()
	{
	}
	
	
	public Para(int bookId, int bookParaOrdinal, int style, byte[] text)
	{
		this.key = new BookOrdinalKey(bookId, bookParaOrdinal);
		this.style = style;
		this.text = text;
	}
	
	
	public static PrimaryIndex<BookOrdinalKey, Para> pkIdx()
	{
		return Db.store().getPrimaryIndex(BookOrdinalKey.class, Para.class);
	}
}
