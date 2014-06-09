package hu.vidyavana.convert.api;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class DirFilesProcessor
{
	public void process(String srcDirName, String destDirName, FileProcessor processor) throws Exception
	{
		File srcDir = new File(srcDirName);
		File destDir = null;
		if(destDirName != null)
		{
			destDir = new File(srcDir, destDirName);
			destDir.mkdirs();
		}
		File[] files = srcDir.listFiles();
		Arrays.sort(files, new Comparator<File>()
		{
			@Override
			public int compare(File f1, File f2)
			{
				return f1.getName().compareTo(f2.getName());
			}
		});
		
		try
		{
			processor.init(srcDir, destDir);
			for(File srcFile : files)
			{
				if(srcFile.isDirectory())
					continue;
				try
				{
					System.out.println(srcFile.getName());
					processor.process(srcFile, srcFile.getName());
				}
				catch(IllegalStateException ex)
				{
					// this exception is used when we can go on to process the next files
					System.out.println(ex.getMessage());
				}
			}
		}
		finally
		{
			processor.finish();
		}
		
	}
}
