package hu.vidyavana.convert.api;

import java.io.IOException;
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


	public void writeToFile(WriterInfo writerInfo) throws IOException
	{
		Book.indent(writerInfo);
		writerInfo.out.write("<chapter>\r\n");
		
		++writerInfo.indentLevel;
		for(Paragraph inf : info)
			inf.writeToFile(writerInfo);
		for(Paragraph par : para)
			par.writeToFile(writerInfo);
		--writerInfo.indentLevel;
		
		Book.indent(writerInfo);
		writerInfo.out.write("</chapter>\r\n");
	}
}
