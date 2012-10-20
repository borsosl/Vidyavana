package hu.vidyavana.convert.api;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class Book
{
	public static int XML_LINE_LEN = 100;
	public static String XML_INDENT = "  ";
	
	public List<Paragraph> info = new ArrayList<Paragraph>();
	public List<Chapter> chapter = new ArrayList<Chapter>();


	public Document createDocument() throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.newDocument();

		Element book = document.createElement("book");
		document.appendChild(book);
		
		for(Paragraph inf : info)
			inf.addToDocument(document, book);
		for(Chapter ch : chapter)
			ch.addToDocument(document, book);
		
		return document;
	}
	
	
	public void writeToFile(File xmlFile) throws IOException
	{
		Writer out = new OutputStreamWriter(new FileOutputStream(xmlFile), "UTF-8");
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		out.write("<?xml-stylesheet href=\"ed.xsl\" type=\"text/xsl\"?>\r\n");
		out.write("<book>\r\n");
		
		for(Paragraph inf : info)
			inf.writeToFile(out, 1);
		for(Chapter ch : chapter)
			ch.writeToFile(out, 1);
		
		out.write("</book>\r\n");
		out.close();
	}
	
	
	public static void indent(Writer out, int indentLevel) throws IOException
	{
		while(indentLevel > 0)
		{
			out.write(XML_INDENT);
			--indentLevel;
		}
	}
}
