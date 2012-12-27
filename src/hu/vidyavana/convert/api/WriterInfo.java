package hu.vidyavana.convert.api;

import java.io.*;
import java.util.List;

public class WriterInfo
{
	public File xmlFile;
	public List<String> fileNames;
	public boolean forEbook;

	public Writer out;
	public int indentLevel;
	
	public Writer toc;
	public String tocDivision;
	public int tocOrdinal;
	public int paraOrdinal;
}
