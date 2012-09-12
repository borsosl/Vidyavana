package hu.vidyavana.convert.api;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

public class Chapter
{
	public List<Paragraph> info = new ArrayList<Paragraph>();
	public List<Paragraph> para = new ArrayList<Paragraph>();

	
	public void addToDocument(Document doc, Element parent)
	{
		Element chapt = doc.createElement("chapter");
		parent.appendChild(chapt);
		
		for(Paragraph inf : info)
			inf.addToDocument(doc, chapt);
		for(Paragraph par : para)
			par.addToDocument(doc, chapt);
	}


	public void writeToFile(Writer out, int indentLevel) throws IOException
	{
		Book.indent(out, indentLevel);
		out.write("<chapter>\r\n");
		
		for(Paragraph inf : info)
			inf.writeToFile(out, indentLevel+1);
		for(Paragraph par : para)
			par.writeToFile(out, indentLevel+1);
		
		Book.indent(out, indentLevel);
		out.write("</chapter>\r\n");
	}
}
