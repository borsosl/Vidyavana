package hu.vidyavana.convert.api;

import java.io.*;

public class WriterInfo
{
	public File xmlFile;
	public boolean forEbook;

	public Writer out;
	public int indentLevel;
	
	public Writer toc;
	public String tocDivision;
	public int tocOrdinal;
	public int paraOrdinal;
}
