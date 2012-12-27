package hu.vidyavana.db.data;

import hu.vidyavana.db.api.*;
import java.sql.*;

public class SettingsDao
{
	public static String getDbMigrate()
	{
		final String[] dbMigrate = new String[1];
		Database.System.query("select db_migrate from settings", new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				rs.next();
				dbMigrate[0] = rs.getString(1);
			}
		});
		return dbMigrate[0];
	}

	
	public static void setDbMigrate(String dbMigrate)
	{
		Database.System.execute("update settings set db_migrate='" + dbMigrate + "'");
	}

	
	public static String getBooksVersion()
	{
		final String[] dbVersion = new String[1];
		Database.System.query("select books_version from settings", new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				rs.next();
				dbVersion[0] = rs.getString(1);
			}
		});
		return dbVersion[0];
	}


	public static void setBooksVersion(String version)
	{
		Database.System.execute("update settings set books_version='" + version + "'");
	}
}
