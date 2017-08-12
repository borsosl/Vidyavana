package hu.vidyavana.convert.api;

import hu.vidyavana.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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
		writerInfo.tocDivisionParaOrdinal = -100;
		for(Paragraph par : para)
		{
			par.writeToFile(writerInfo);
			if(par.tocLevel != 0)
			{
				if(par.tocLevel > 0)
					tocEntry(writerInfo, par.tocLevel,
							par.tocText != null ? par.tocText : XmlUtil.noMarkup(par.text.toString().trim()));
			}
			else if(par.cls == ParagraphClass.Konyvcim)
				writerInfo.tocDivisionParaOrdinal = writerInfo.paraOrdinal;
			else if(par.cls == ParagraphClass.Fejezetszam)
			{
				writerInfo.tocDivision = XmlUtil.noMarkup(par.text.toString().trim());
				if(writerInfo.paraOrdinal - writerInfo.tocDivisionParaOrdinal > 3)
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
		if(writerInfo.numberedAbbrevOnTocLevels != null) {
			if(writerInfo.numberedAbbrevOnTocLevels.contains(level)) {
				o.write("      <abbrev>");
				o.write(Integer.toString(++writerInfo.tocAbbrevLevelOrdinals[level]));
				o.write("</abbrev>\r\n");
            }
            writerInfo.tocAbbrevLevelOrdinals[level+1] = 0;
		}
		o.write("      <para_ordinal>");
		int ord = writerInfo.tocDivisionParaOrdinal;
		if(writerInfo.paraOrdinal - ord > 3)
			ord = writerInfo.paraOrdinal;
		o.write(Integer.toString(ord));
		o.write("</para_ordinal>\r\n");
		o.write("    </entry>\r\n");
		
		writerInfo.tocDivision = null;
		writerInfo.tocDivisionParaOrdinal = -100;
	}
}
