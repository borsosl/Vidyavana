package hu.vidyavana.db.model;

import hu.vidyavana.db.api.Db;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.*;

@Entity
public class Settings
{
	@PrimaryKey
	public String createdAt;

	public String dbMigrate;
	public String booksVersion;
	
	
	public static PrimaryIndex<String, Settings> pkIdx()
	{
		return Db.store().getPrimaryIndex(String.class, Settings.class);
	}
}
