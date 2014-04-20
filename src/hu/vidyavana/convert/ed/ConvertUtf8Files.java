package hu.vidyavana.convert.ed;

import hu.vidyavana.convert.api.FileProcessor;
import java.io.*;

public class ConvertUtf8Files
{
	public static void main(String[] args) throws Exception
	{
		File srcDir = new File(".");
		String[] list = srcDir.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith("utf8");
			}
		});
		FileProcessor processor = new Utf8FileProcessor();
		File destDir = new File(srcDir, "ed");
		destDir.mkdirs();
		processor.init(srcDir, destDir);
		for(String fname : list)
		{
			System.out.println(fname);
			processor.process(new File(srcDir, fname), fname);
		}
	}
}
