package hu.vidyavana.convert.api;

import hu.vidyavana.util.XmlUtil;
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


	public void writeToFile(WriterInfo writerInfo) throws IOException
	{
		Book.indent(writerInfo);
		writerInfo.out.write("<chapter>\r\n");
		
		++writerInfo.indentLevel;
		for(Paragraph inf : info)
			inf.writeToFile(writerInfo);
		for(Paragraph par : para)
		{
			par.writeToFile(writerInfo);
			++writerInfo.paraOrdinal;
			if(par.cls == ParagraphClass.Fejezetszam)
				writerInfo.tocDivision = XmlUtil.noMarkup(par.text.toString().trim());
			else if(par.cls == ParagraphClass.Fejezetcim)
				tocEntry(writerInfo, 1, XmlUtil.noMarkup(par.text.toString().trim()));
			else if(par.cls == ParagraphClass.Versszam)
				tocEntry(writerInfo, 2, XmlUtil.noMarkup(par.text.toString().trim()));
		}
		--writerInfo.indentLevel;
		
		Book.indent(writerInfo);
		writerInfo.out.write("</chapter>\r\n");
	}


	private void tocEntry(WriterInfo writerInfo, int level, String tocTitle) throws IOException
	{
		Writer o = writerInfo.toc;
		if(o == null)
			return;
		
		++writerInfo.tocOrdinal;
		o.write("    <entry>\r\n");
		o.write("      <level>");
		o.write(Integer.toString(level));
		o.write("</level>\r\n");
		o.write("      <division>");
		o.write(writerInfo.tocDivision == null ? tocTitle : writerInfo.tocDivision);
		o.write("</division>\r\n");
		if(writerInfo.tocDivision != null)
		{
			o.write("      <title>");
			o.write(tocTitle);
			o.write("</title>\r\n");
		}
		o.write("      <toc_ordinal>");
		o.write(Integer.toString(writerInfo.tocOrdinal));
		o.write("</toc_ordinal>\r\n");
		o.write("      <para_ordinal>");
		o.write(Integer.toString(writerInfo.paraOrdinal));
		o.write("</para_ordinal>\r\n");
		o.write("    </entry>\r\n");
		
		writerInfo.tocDivision = null;
	}
}
