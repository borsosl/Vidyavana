package hu.vidyavana.db;

import hu.vidyavana.db.api.Database;
import hu.vidyavana.db.data.SettingsDao;
import java.io.File;

public class DatabaseMigration
{
	String lastScript;
	boolean updateSettings;
	
	
	public DatabaseMigration()
	{
		if(new File("System.h2.db").exists())
		{
			try
			{
				lastScript = SettingsDao.getDbMigrate();
			}
			catch(Exception e)
			{
				lastScript = "";
			}
		}
		else
			lastScript = "";
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
			SettingsDao.setDbMigrate(lastScript);
	}
}
