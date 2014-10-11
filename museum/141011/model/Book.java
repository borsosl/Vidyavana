package hu.vidyavana.db.model;

import hu.vidyavana.db.api.Db;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.*;

@Entity
public class Book
{
	@PrimaryKey
	public int id;
	
	public int parentId;
	public String title;
	public int systemPriority;
	public int userPriority;
	public String repoVersion;
	public String dbVersion;
	
	
	public static PrimaryIndex<Integer, Book> pkIdx()
	{
		return Db.store().getPrimaryIndex(Integer.class, Book.class);
	}
}
