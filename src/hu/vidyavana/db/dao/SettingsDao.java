package hu.vidyavana.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import hu.vidyavana.db.api.ResultSetCallback;
import hu.vidyavana.db.api.Sql;

public class SettingsDao
{
	public static String getDbMigrate()
	{
		final String[] dbMigrate = new String[1];
		Sql.System.query("select db_migrate from settings", new ResultSetCallback()
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
		Sql.System.execute("update settings set db_migrate='" + dbMigrate + "'");
	}
}
