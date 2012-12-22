package hu.vidyavana.convert.api;

import java.io.*;

public class FileListProcessor
{
	public void process(File listfile, FileProcessor processor) throws Exception
	{
		BufferedReader fileList = new BufferedReader(new FileReader(listfile));
		
		int actualLineNum = 0;
		File srcDir=null, destDir=null;
		
		try
		{
			while(true)
			{
				String fname = fileList.readLine();
				if(fname == null) break;
				fname = fname.trim();
				if(fname.length() == 0 || fname.charAt(0)=='#') continue;
				++actualLineNum;
				
				if(actualLineNum == 1)
				{
					srcDir = new File(fname);
					continue;
				}
				if(actualLineNum == 2)
				{
					destDir = new File(fname);
					processor.init(srcDir, destDir);
					continue;
				}
				
				File srcFile = new File(srcDir.getAbsolutePath() + "/" + fname);
				
				try
				{
					System.out.println(srcFile.getName());
					processor.process(srcFile, fname);
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
			fileList.close();
		}
		
	}
}
