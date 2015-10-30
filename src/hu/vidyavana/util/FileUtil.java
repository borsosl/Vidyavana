package hu.vidyavana.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
}
