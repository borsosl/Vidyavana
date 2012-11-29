package hu.vidyavana.db;

import hu.vidyavana.db.api.*;
import java.sql.*;

public class DatabaseMigration
{
	String lastScript;
	boolean updateSettings;
	
	
	public DatabaseMigration()
	{
		try
		{
			Database.System.query("select db_migrate from settings", new ResultSetCallback()
			{
				@Override
				public void useResultSet(ResultSet rs) throws SQLException
				{
					rs.next();
					lastScript = rs.getString(1);
				}
			});
		}
		catch(Exception e)
		{
			lastScript = "";
		}
	}

	
	public void migrate(String scriptId, String sql)
	{
		if(lastScript.compareTo(scriptId) >= 0)
			return;

		try
		{
			Database.System.execute(sql);
			lastScript = scriptId;
			updateSettings = true;
		}
		catch(Exception ex)
		{
			updateSettings();
			throw new RuntimeException("DB migration error: " + scriptId, ex);
		}
	}

	
	public void updateSettings()
	{
		if(updateSettings)
			Database.System.execute("update settings set db_migrate='"+lastScript+"'");
	}
}
