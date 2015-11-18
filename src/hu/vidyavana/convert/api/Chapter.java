package hu.vidyavana.convert.api;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import hu.vidyavana.util.XmlUtil;

public class Chapter
{
	public List<Paragraph> info = new ArrayList<Paragraph>();
	public List<Paragraph> para = new ArrayList<Paragraph>();
	public String footnotes;

	
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
			if(par.cls == ParagraphClass.Fejezetszam)
			{
				writerInfo.tocDivision = XmlUtil.noMarkup(par.text.toString().trim());
				writerInfo.tocDivisionParaOrdinal = writerInfo.paraOrdinal;
			}
			else if(par.cls == ParagraphClass.Fejezetcim)
			{
				tocEntry(writerInfo, 1, XmlUtil.noMarkup(par.text.toString().trim()));
				writerInfo.tocDivision = null;
			}
			else if(par.cls == ParagraphClass.Versszam || par.cls == ParagraphClass.Szakaszcim)
			{
				if(writerInfo.tocDivision != null)
					tocEntry(writerInfo, 1, null);
				tocEntry(writerInfo, 2, XmlUtil.noMarkup(par.text.toString().trim()));
			}
		}
		if(footnotes != null)
			writerInfo.out.write(footnotes);
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
		String title;
		if(writerInfo.tocDivision == null)
			title = "";
		else
			title = writerInfo.tocDivision + 'ǀ';	// 01c0
		if(tocTitle != null)
			title += tocTitle;
		o.write("      <title>");
		o.write(title);
		o.write("</title>\r\n");
//		if(writerInfo.tocDivision != null)
//		{
//			o.write("      <title>");
//			o.write(tocTitle);
//			o.write("</title>\r\n");
//		}
//		o.write("      <toc_ordinal>");
//		o.write(Integer.toString(writerInfo.tocOrdinal));
//		o.write("</toc_ordinal>\r\n");
		o.write("      <para_ordinal>");
		o.write(Integer.toString(writerInfo.tocDivision == null ? writerInfo.paraOrdinal : writerInfo.tocDivisionParaOrdinal));
		o.write("</para_ordinal>\r\n");
		o.write("    </entry>\r\n");
		
		writerInfo.tocDivision = null;
	}
}
