package hu.vidyavana.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil
{
	public static List<String> readTextFile(File f)
	{
		BufferedReader br = null;
		List<String> list = new ArrayList<String>();
		try
		{
			br = new BufferedReader(new FileReader(f));
			while(true)
			{
				String line = br.readLine();
				if(line == null)
					break;
				list.add(line);
			}
			return list;
		}
		catch(IOException ex)
		{
			return null;
		}
		finally
		{
			if(br != null)
				try
				{
					br.close();
				}
				catch(IOException ex)
				{
				}
		}
	}
	
	
	public static void serializeToFile(Serializable s, File f)
	{
		try (
			OutputStream file = new FileOutputStream(f);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);)
		{
			output.writeObject(s);
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	
	
	
	public static Serializable deserializeFromFile(File f)
	{
		try (
			InputStream file = new FileInputStream(f);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);)
		{
			return (Serializable) input.readObject();
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
}
