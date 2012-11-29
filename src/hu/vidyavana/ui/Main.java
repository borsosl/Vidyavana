package hu.vidyavana.ui;

import hu.vidyavana.db.DatabaseMigration;
import hu.vidyavana.util.*;

public class Main
{
	public static final String PRIMARY_JAR_NAME = "Vidyavana.jar";
	
	
	public static void main(String[] args)
	{
		try
		{
			databaseMigration();
		}
		catch(Throwable t)
		{
			Log.error(null, t);
			System.out.println(t.getMessage());
			System.exit(1);
		}
	}


	private static void databaseMigration()
	{
		DatabaseMigration dbm = new DatabaseMigration();
		if(!ResourceUtil.dbMigrationUsingJar(dbm))
			if(!ResourceUtil.dbMigrationUsingFiles(dbm))
			{
				System.out.println("SQL file olvasasi hiba.");
				System.exit(1);
			}
	}
}
