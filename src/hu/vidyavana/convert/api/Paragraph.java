package hu.vidyavana.convert.api;

import static hu.vidyavana.convert.api.ParagraphClass.*;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Paragraph
{
	public static Pattern WORD = Pattern.compile("[A-Za-záéíóöőúüűÁÉÍÓÖŐÚÜŰāīūḍḥḷḹṁṅṇñṛṝṣśṭĀĪŪḌḤḶḸṀṄṆÑṚṜṢŚṬ]+");

	public boolean isInfo;
	public String srcStyle;
	public String xmlTagName;
	public ParagraphClass cls;
	public ParagraphStyle style;
	public Paragraph prev;
	public int indexLevel;
	public int indent;
	public StringBuilder text = new StringBuilder();
	public List<String> footnote = new ArrayList<String>();
	public String srcFileName;
	public int srcFileLine;

	
	public void addToDocument(Document doc, Element parent)
	{
		if(xmlTagName ==null) xmlTagName = "p";
		Element para = doc.createElement(xmlTagName);
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
		if(xmlTagName ==null) xmlTagName = "p";
		if("p".equals(xmlTagName))
			++writerInfo.paraOrdinal;
		Book.indent(writerInfo);
		int indentLevel = writerInfo.indentLevel;
		int len = indentLevel * Book.XML_INDENT.length();
		out.write('<');
		out.write(xmlTagName);
		len += xmlTagName.length()+1;
		if(cls != null)
		{
			out.write(" class=\"");
			if(cls == ParagraphClass.Index)
			{
				out.write("in");
				out.write('0'+indexLevel);
			}
			else
				out.write(cls.toString());
			out.write('"');
			len += cls.toString().length()+9;
		}
		if(style != null)
		{
			if(cls == null && style.basedOn != null && !style.basedOn.isEmpty())
			{
				out.write(" class=\"");
				out.write(style.basedOn);
				out.write('"');
				len += style.basedOn.length()+9;
			}
			/*
			String ss = style.toString();
			if(!ss.isEmpty())
			{
				out.write(" style=\"");
				out.write(ss);
				out.write('"');
				len += style.toString().length()+9;
			}
			*/
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
		out.write(xmlTagName);
		out.write(">\r\n");
	}


	private void writeText(String txt, WriterInfo writerInfo, int prefixLen) throws IOException
	{
		if(writerInfo.diacritics != null)
		{
			collectDiacritics(txt, writerInfo);
			return;
		}

		if(ProofreadWords.ACTIVE) {
			writerInfo.proofreadWords.collect(this, txt);
		}

		txt = txt.replace("&", "&amp;");
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


	static Set<ParagraphClass> excludedClasses = new HashSet<>(Arrays.asList(new ParagraphClass[]{
		Uvaca, Vers, Proza, Szavak, TorzsUvaca, TorzsVers, Kozepen, Index}));
	
	private void collectDiacritics(String txt, WriterInfo writerInfo)
	{
//		if(excludedClasses.contains(cls))
//			return;
		Matcher m = Paragraph.WORD.matcher(txt);
		int ix = 0;
		StringBuilder sb = new StringBuilder(200);
		while(true)
		{
			if(m.find(ix))
			{
				String w = m.group().toLowerCase();
				sb.setLength(0);
				int len = w.length();
				for(int j = 0; j < len; ++j)
				{
					char orig = w.charAt(j);
					int latin = DiacriticToLatinPairs.convert(orig);
					sb.append(latin == 0 ? (char) orig : (char) latin);
				}
				String latin = sb.toString().toLowerCase();
				if(cls == Vers || cls == Proza || cls == TorzsVers || !latin.equals(w))
				{
					Object stored = writerInfo.diacritics.get(latin);
					if(stored == null)
					{
						writerInfo.diacritics.put(latin, w);
					}
					else if(stored instanceof String)
					{
						if(!stored.equals(w))
						{
							TreeSet set = new TreeSet();
							set.add(stored);
							set.add(w);
							writerInfo.diacritics.put(latin, set);
						}
					}
					else
					{
						TreeSet set = (TreeSet) stored;
						set.add(w);
					}
				}
				ix = m.end();
			}
			else
				break;
		}
	}

}
