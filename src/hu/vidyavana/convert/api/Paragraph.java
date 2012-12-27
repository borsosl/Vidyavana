package hu.vidyavana.convert.api;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

public class Paragraph
{
	public boolean isInfo;
	public String tagName;
	public ParagraphClass cls;
	public ParagraphStyle style;
	public int indexLevel;
	public int indent;
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


	public void writeToFile(WriterInfo writerInfo) throws IOException
	{
		Writer out = writerInfo.out;
		if(tagName==null) tagName = "p";
		if("p".equals(tagName))
			++writerInfo.paraOrdinal;
		Book.indent(writerInfo);
		int indentLevel = writerInfo.indentLevel;
		int len = indentLevel * Book.XML_INDENT.length();
		out.write('<');
		out.write(tagName);
		len += tagName.length()+1;
		if(cls != null)
		{
			out.write(" class=\"");
			out.write(cls.toString());
			if(cls == ParagraphClass.Index)
			{
				out.write(" in");
				out.write('0'+indexLevel);
			}
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
		writeText(text.toString(), writerInfo, len);
		if(footnote != null && footnote.size() > 0)
		{
			out.write("\r\n");
			++writerInfo.indentLevel;
			for(String fn : footnote)
			{
				Book.indent(writerInfo);
				out.write("<footnote>");
				writeText(fn, writerInfo, (indentLevel+1) * Book.XML_INDENT.length()+10);
				out.write("</footnote>\r\n");
			}
			--writerInfo.indentLevel;
			Book.indent(writerInfo);
		}
		out.write("</");
		out.write(tagName);
		out.write(">\r\n");
	}


	private void writeText(String txt, WriterInfo writerInfo, int prefixLen) throws IOException
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
			writerInfo.out.write(txt.substring(start, end));
			if(end < len)
			{
				writerInfo.indentLevel += 2;
				writerInfo.out.write("\r\n");
				Book.indent(writerInfo);
				prefixLen = writerInfo.indentLevel * Book.XML_INDENT.length();
				writerInfo.indentLevel -= 2;
			}
			start = end + 1;
		}
	}

}
