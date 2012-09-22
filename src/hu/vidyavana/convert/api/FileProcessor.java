package hu.vidyavana.convert.api;

import java.io.File;

public interface FileProcessor
{
	void init(File srcDir, File destDir);
	
	void process(File srcFile, String fileName) throws Exception;
	
	void finish();
}
