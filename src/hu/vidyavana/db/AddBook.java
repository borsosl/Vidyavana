package hu.vidyavana.db;

import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.db.data.*;
import hu.vidyavana.util.XmlUtil;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.w3c.dom.*;

public class AddBook
{
	public static Pattern XML_LINE = Pattern.compile("^\\s*(<p( class=\"(.*?)\")?.*?>)?(.*?)(</p>|$)");
	
	private int bookId;
	private String bookPath;
	private File bookDir;
	private ArrayList<String> bookFileNames;

	
	public AddBook(int bookId, String bookPath)
	{
		this.bookId = bookId;
		this.bookPath = bookPath;
		bookDir = new File(this.bookPath);
	}
	
	
	public void run()
	{
		addToc();
		addChapters();
	}


	private void addToc()
	{
		Element docElem = getXmlRoot("toc.xml");
		// String xmlVersion = docElem.getElementsByTagName("version").item(0).getTextContent();

		List<Contents> contents = new ArrayList<>();
		NodeList entries = docElem.getElementsByTagName("entries");
		if(entries.getLength() > 0)
		{
			NodeList entryList = entries.item(0).getChildNodes();
			for(int i=0, len=entryList.getLength(); i<len; ++i)
			{
				Node entry = entryList.item(i);
				if(!"entry".equals(entry.getNodeName()))
					continue;
				NodeList children = entry.getChildNodes();
				Contents xmlContents = new Contents();
				for(int j=0, len2 = children.getLength(); j<len2; ++j)
				{
					Node n = children.item(j);
					String txt = n.getTextContent().trim();
					if("level".equals(n.getNodeName()))
						xmlContents.level = Integer.parseInt(txt);
					else if("division".equals(n.getNodeName()))
						xmlContents.division = txt;
					else if("title".equals(n.getNodeName()))
						xmlContents.title = txt;
					else if("toc_ordinal".equals(n.getNodeName()))
						xmlContents.bookTocOrdinal = Integer.parseInt(txt);
					else if("para_ordinal".equals(n.getNodeName()))
						xmlContents.bookParaOrdinal = Integer.parseInt(txt);
				}
				if(xmlContents.title == null)
					xmlContents.title = "";
				contents.add(xmlContents);
			}
		}

		bookFileNames = new ArrayList<>();
		NodeList files = docElem.getElementsByTagName("files");
		if(files.getLength() > 0)
		{
			NodeList fileList = files.item(0).getChildNodes();
			for(int i=0, len=fileList.getLength(); i<len; ++i)
			{
				Node entry = fileList.item(i);
				if("file".equals(entry.getNodeName()))
					bookFileNames.add(entry.getTextContent().trim());
			}
			
		}
		
		ContentsDao.updateBookContents(bookId, contents);
	}


	private void addChapters()
	{
		List<Para> paras = new ArrayList<>();
		int bookParaOrdinal = 0;
		for(String fname : bookFileNames)
		{
			File f = new File(bookDir, fname);
			BufferedReader in = null;
			try
			{
				in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
				Para xmlPara = null;
				StringBuilder paraSB = new StringBuilder(10000);
				boolean inPara = false;
				while(true)
				{
					String line = in.readLine();
					if(line == null)
						break;
					Matcher m = XML_LINE.matcher(line);
					m.find();
					if(m.group(1) != null)
					{
						if(inPara)
							xmlPara.text = paraSB.toString();
						
						String className = m.group(3);
						ParagraphClass cls;
						try
						{
							cls = ParagraphClass.valueOf(className);
						}
						catch(Exception ex)
						{
							cls = ParagraphClass.TorzsKoveto;
						}
						
						xmlPara = new Para();
						xmlPara.bookParaOrdinal = ++bookParaOrdinal;
						xmlPara.style = cls.code;
						paras.add(xmlPara);
						
						String txt = m.group(4);
						paraSB.setLength(0);
						paraSB.append(txt);
						
						inPara = true;
					}
					else if(inPara)
						paraSB.append(' ').append(m.group(4));

					if(inPara)
					{
						inPara = m.group(5).length() == 0;
						if(!inPara)
							xmlPara.text = paraSB.toString();
					}
				}
			}
			catch(Exception ex)
			{
				throw new RuntimeException(ex);
			}
			finally
			{
				if(in != null)
					try
					{
						in.close();
					}
					catch(IOException ex)
					{
						throw new RuntimeException(ex);
					}
			}
		}
		ParaDao.updateBookParagraphs(bookId, paras);
	}


	private Element getXmlRoot(String fileName)
	{
		File f = new File(bookDir, fileName);
		String xml = XmlUtil.readFromFile(f);
		Document doc = XmlUtil.domFromString(xml);
		return doc.getDocumentElement();
	}
}
