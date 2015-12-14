package hu.vidyavana.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Conf
{
	private static Properties prop;


	static
	{
		prop = new Properties();

		try (InputStream input = new FileInputStream(new File(Globals.cwd, "config.properties")))
		{
			prop.load(input);
		}
		catch(IOException ex)
		{
			// no log: this class is pre-requisite of Log
			ex.printStackTrace();
		}
	}


	public static Properties instance()
	{
		return prop;
	}


	public static String get(String key)
	{
		return prop.getProperty(key);
	}
}
