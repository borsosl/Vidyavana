package hu.vidyavana.db;

import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.db.api.Db;
import hu.vidyavana.db.model.*;
import hu.vidyavana.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.*;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.*;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import com.sleepycat.persist.PrimaryIndex;

public class AddBook
{
	public static Pattern XML_LINE = Pattern.compile("^\\s*(<p( class=\"(.*?)\")?.*?>)?(.*?)(</p>|$)");
	
	private int bookId;
	private String bookPath;
	private File bookDir;
	private final IndexWriter iw;
	private FieldType txtFieldType;
	private ArrayList<String> bookFileNames;

	
	public AddBook(int bookId, String bookPath, IndexWriter writer)
	{
		this.bookId = bookId;
		this.bookPath = bookPath;
		bookDir = new File(this.bookPath);
		this.iw = writer;
		txtFieldType = new FieldType();
		txtFieldType.setIndexed(true);
		txtFieldType.setTokenized(true);
		txtFieldType.setStored(false);
		txtFieldType.setStoreTermVectors(false);
		txtFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		txtFieldType.freeze();
	}
	
	
	public void run()
	{
		Db.openForWrite();
		addToc();
		addChapters();
		Db.openForRead();
	}


	private void addToc()
	{
		Element docElem = getXmlRoot("toc.xml");
		// String xmlVersion = docElem.getElementsByTagName("version").item(0).getTextContent();

		PrimaryIndex<BookOrdinalKey, Contents> idx = Contents.pkIdx();
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
				int tocOrdinal = -1;
				Contents contents = new Contents();
				for(int j=0, len2 = children.getLength(); j<len2; ++j)
				{
					Node n = children.item(j);
					String txt = n.getTextContent().trim();
					if("level".equals(n.getNodeName()))
						contents.level = Integer.parseInt(txt);
					else if("division".equals(n.getNodeName()))
						contents.division = txt;
					else if("title".equals(n.getNodeName()))
						contents.title = txt;
					else if("toc_ordinal".equals(n.getNodeName()))
						tocOrdinal = Integer.parseInt(txt);
					else if("para_ordinal".equals(n.getNodeName()))
						contents.bookParaOrdinal = Integer.parseInt(txt);
				}
				contents.key = new BookOrdinalKey(bookId, tocOrdinal);
				if(contents.title == null)
					contents.title = "";
				idx.put(contents);
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
	}


	private void addChapters()
	{
		PrimaryIndex<BookOrdinalKey, Para> idx = Para.pkIdx();
		int bookParaOrdinal = 0;
		for(String fname : bookFileNames)
		{
			File f = new File(bookDir, fname);
			BufferedReader in = null;
			try
			{
				in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
				Para para = null;
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
							addPara(para, paraSB, idx);
						
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
						
						para = new Para(bookId, ++bookParaOrdinal, cls.code, null);
						
						String txt = m.group(4);
						paraSB.setLength(0);
						paraSB.append(txt);
						
						inPara = true;
					}
					else if(inPara)
						paraSB.append(' ').append(m.group(4));

					if(inPara)
					{
						// if <p> is started or cont'd, look for </p>
						inPara = m.group(5).length() == 0;
						if(!inPara)
							addPara(para, paraSB, idx);
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
	}


	private void addPara(Para para, StringBuilder paraSB, PrimaryIndex<BookOrdinalKey, Para> idx)
	{
		String paraTxt = paraSB.toString();
		para.text = Encrypt.getInstance().encrypt(paraTxt);
		idx.put(para);
		
		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
		doc.add(new IntField("bid", bookId, Store.YES));
		doc.add(new IntField("ord", para.key.ordinal, Store.YES));
		doc.add(new Field("text", paraTxt, txtFieldType));
		try
		{
			// TODO transaction
			iw.addDocument(doc);
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}


	private Element getXmlRoot(String fileName)
	{
		File f = new File(bookDir, fileName);
		String xml = XmlUtil.readFromFile(f);
		Document doc = XmlUtil.domFromString(xml);
		return doc.getDocumentElement();
	}
}
