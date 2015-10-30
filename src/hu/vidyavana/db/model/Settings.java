package hu.vidyavana.db.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Settings
{
	public static final int CURRENT_VERSION = 1;
	public static Settings INSTANCE = new Settings();
	public static String FNAME = "settings.dat";
	
	public short version;
	public String createdAt;
	public String dbMigrate;
	public String booksVersion;

	// derived
	public File settingsFile;
	
	
	private Settings()
	{
		settingsFile = new File(FNAME);		// TODO abs path for android
	}

	public void write() throws IOException
	{
		RandomAccessFile out = new RandomAccessFile(settingsFile, "rw");
		out.writeShort(version);
		out.close();
	}


	public void read() throws IOException
	{
		RandomAccessFile in = null;
		try
		{
			in = new RandomAccessFile(FNAME, "r");
		}
		catch(FileNotFoundException ex)
		{
			version = CURRENT_VERSION;
			return;
		}
		version = in.readShort();
		in.close();
	}
}
