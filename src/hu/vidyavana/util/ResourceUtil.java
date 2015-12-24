package hu.vidyavana.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import hu.vidyavana.db.SqlMigration;

public class ResourceUtil
{
	public static final Pattern DBMIGRATE_FILES_IN_JAR = Pattern.compile("^hu/resource/dbmigrate/([^/]+)\\.sql");

	public static File DBMIGRATE_JAR_PATH;
	public static String DBMIGRATE_JAR_NAME;
	public static String DBMIGRATE_FILE_PATH = "src\\hu\\resource\\dbmigrate";

	
	public static InputStream getResource(String name)
	{
		return ResourceUtil.class.getResourceAsStream(name);
	}

	
	public static boolean dbMigrationUsingJar(SqlMigration dbm)
	{
		if(DBMIGRATE_JAR_NAME == null)
			return false;
		File[] files = DBMIGRATE_JAR_PATH.listFiles();
		ArrayList<File> jars = new ArrayList<>();
		for(File f : files)
		{
			if(DBMIGRATE_JAR_NAME.equals(f.getName()))
				jars.add(0, f);
			else if(f.getName().endsWith(".jar"))
				jars.add(f);
		}
		
		Map<String, ZipEntry> entries = null;
		for(File jar : jars)
		{
			try
			{
				ZipFile zf = new ZipFile(jar);
				entries = findMigrateScriptsInZip(zf);
				if(entries == null || entries.isEmpty())
					continue;
				
				for(String scriptId : entries.keySet())
				{
					String sql = readZippedFile(zf, entries.get(scriptId));
					if(sql == null)
						continue;
					dbm.migrate(scriptId, sql);
				}
				dbm.updateSettings();
				return true;
			}
			catch(IOException ex)
			{
				Log.error(null, ex);
				return false;
			}
		}
		
		return false;
	}
	
	
	private static Map<String, ZipEntry> findMigrateScriptsInZip(ZipFile zf)
	{
		Enumeration entries = zf.entries();
		Map<String, ZipEntry> entriesMap = new TreeMap<String, ZipEntry>();
		while(entries.hasMoreElements())
		{
			ZipEntry ze = (ZipEntry) entries.nextElement();
			Matcher m = DBMIGRATE_FILES_IN_JAR.matcher(ze.getName());
			if(m.find())
				entriesMap.put(m.group(1), ze);
		}
		return entriesMap;
	}


	public static boolean dbMigrationUsingFiles(SqlMigration dbm)
	{
		if(DBMIGRATE_FILE_PATH == null)
			return false;
		File dir = new File(DBMIGRATE_FILE_PATH);
		if(!dir.exists())
			throw new RuntimeException("DB migration path broken.");
		
		try
		{
			File[] files = dir.listFiles();
			Map<String, File> entries = new TreeMap<String, File>();
			for(File f : files)
			{
				if(f.isDirectory()) continue;
				String fileName = f.getName();
				if(!fileName.endsWith(".sql")) continue;
				String scriptId = fileName.substring(0, fileName.length()-4);
				entries.put(scriptId, f);
			}
			for(String scriptId : entries.keySet())
			{
				String sql = readTextFile(entries.get(scriptId));
				dbm.migrate(scriptId, sql);
			}
			dbm.updateSettings();
			return true;
		}
		catch(IOException ex)
		{
			Log.error(null, ex);
			return false;
		}
	}


	public static String readTextFile(File file) throws IOException
	{
		return readTextStream(new FileInputStream(file));
	}


	public static String readZippedFile(ZipFile zf, ZipEntry ze) throws IOException
	{
		if(ze.getSize() > 0)
			return readTextStream(zf.getInputStream(ze));
	
		return null;
	}


	public static String readTextStream(InputStream is) throws IOException
	{
		StringBuilder sb = new StringBuilder(1000);
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = br.readLine()) != null)
				sb.append(line).append('\n');
		}
		finally
		{
			if(br != null)
				br.close();
		}
		return sb.toString();
	}
}
