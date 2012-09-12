package hu.vidyavana.convert.api;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

public class Paragraph
{
	public String tagName;
	public ParagraphClass cls;
	public ParagraphStyle style;
	public StringBuilder text = new StringBuilder();
	public List<String> footnote = new ArrayList<String>();
	
	
	public void addToDocument(Document doc, Element parent)
	{
		if(tagName==null) tagName = "p";
		Element para = doc.createElement(tagName);
		parent.appendChild(para);
		
		if(cls != null)
			para.setAttribute("class", cls.toString());
		if(style != null)
			para.setAttribute("style", style.toString());
		para.setTextContent(text.toString());
		if(footnote != null)
			for(String fn : footnote)
			{
				Element fnElem = doc.createElement("footnote");
				fnElem.setTextContent(fn);
				para.appendChild(fnElem);
			}
	}


	public void writeToFile(Writer out, int indentLevel) throws IOException
	{
		if(tagName==null) tagName = "p";
		Book.indent(out, indentLevel);
		int len = indentLevel * Book.XML_INDENT.length();
		out.write('<');
		out.write(tagName);
		len += tagName.length()+1;
		if(cls != null)
		{
			out.write(" class=\"");
			out.write(cls.toString());
			out.write('"');
			len += cls.toString().length()+9;
		}
		if(style != null)
		{
			out.write(" style=\"");
			out.write(style.toString());
			out.write('"');
			len += style.toString().length()+9;
		}
		out.write('>');
		++len;
		writeText(out, text.toString(), indentLevel, len);
		if(footnote != null && footnote.size() > 0)
		{
			out.write("\r\n");
			for(String fn : footnote)
			{
				Book.indent(out, indentLevel+1);
				out.write("<footnote>");
				writeText(out, fn, indentLevel+1, (indentLevel+1) * Book.XML_INDENT.length()+10);
				out.write("</footnote>\r\n");
			}
			Book.indent(out, indentLevel);
		}
		out.write("</");
		out.write(tagName);
		out.write(">\r\n");
	}


	private void writeText(Writer out, String txt, int indentLevel, int prefixLen) throws IOException
	{
		int len = txt.length();
		int start = 0;
		while(start < len)
		{
			int end = start + Book.XML_LINE_LEN - prefixLen;
			if(end > len)
				end = len;
			while(end < len && txt.charAt(end)!=' ')
				++end;
			out.write(txt.substring(start, end));
			if(end < len)
			{
				out.write("\r\n");
				Book.indent(out, indentLevel+2);
				prefixLen = (indentLevel+2) * Book.XML_INDENT.length();
			}
			start = end + 1;
		}
	}

}
