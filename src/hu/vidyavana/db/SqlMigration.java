package hu.vidyavana.db;

import java.io.File;
import hu.vidyavana.db.api.Sql;
import hu.vidyavana.db.dao.SettingsDao;
import hu.vidyavana.util.Log;

public class SqlMigration
{
	String lastScript;
	boolean updateSettings;
	
	
	public SqlMigration()
	{
		if(new File(Sql.System.path()+".h2.db").exists())
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
			Log.info("Running migration "+scriptId);
			Sql.System.execute(sql);
			Log.info("Finished migration "+scriptId);
			lastScript = scriptId;
			updateSettings = true;
		}
		catch(Exception ex)
		{
			updateSettings();
			Log.info("Failed migration "+scriptId);
			throw new RuntimeException("DB migration error: " + scriptId, ex);
		}
	}

	
	public void updateSettings()
	{
		if(updateSettings)
			SettingsDao.setDbMigrate(lastScript);
	}
}
