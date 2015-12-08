package hu.vidyavana.db;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.db.model.BookSegment;
import hu.vidyavana.db.model.Storage;
import hu.vidyavana.db.model.StoragePara;
import hu.vidyavana.db.model.StorageTocItem;
import hu.vidyavana.search.api.Lucene;
import hu.vidyavana.util.FileUtil;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.util.XmlUtil;

public class AddBook
{
	public static Pattern XML_LINE = Pattern.compile("^\\s*(<p( class=\"(.*?)\")?.*?>)?(.*?)(</p>|$)");
	
	private String bookPath;
	private File bookDir;
	private IndexBooks ib;
	private ArrayList<String> bookFileNames;

	private Storage store;
	private BookSegment bs;

	private List<StoragePara> paraList;


	
	public AddBook(String bookPath, Storage store, IndexBooks ib)
	{
		this.bookPath = bookPath;
		this.store = store;
		this.ib = ib;
		bookDir = new File(this.bookPath);
	}
	
	
	public void run()
	{
		long startPos = -1;
		try
		{
			store.openForWrite();
			store.setEncrypted(false);
			startPos = store.handle.length();
			bs = new BookSegment();
			addHead();
			ib.initBook(bs.plainBookId, bs.segment);
			addContent();
			bs.write(store);
			bs.para = null;
		}
		catch(IOException ex)
		{
			if(startPos > -1)
				try
				{
					store.handle.setLength(startPos);
				}
				catch(IOException ex1)
				{
				}
			throw new RuntimeException("Error writing content file.", ex);
		}
	}


	private void addHead()
	{
		Element docElem = getXmlRoot("toc.xml");
		NodeList children = docElem.getChildNodes();
		String segmentTitle = null;
		int segmentTitleOfs = 0;
		for(int j=0, len2 = children.getLength(); j<len2; ++j)
		{
			Node n = children.item(j);
			if("title".equals(n.getNodeName()))
				bs.title = n.getTextContent().trim();
			else if("id".equals(n.getNodeName()))
				bs.plainBookId = Short.parseShort(n.getTextContent().trim());
			else if("segment".equals(n.getNodeName()))
				bs.segment = Byte.parseByte(n.getTextContent().trim());
			else if("segment_title".equals(n.getNodeName()))
				segmentTitle = n.getTextContent().trim();
			else if("priority".equals(n.getNodeName()))
				bs.priority = Integer.parseInt(n.getTextContent().trim());
			else if("version".equals(n.getNodeName()))
				bs.repoVersion = Integer.parseInt(n.getTextContent().trim());
		}
		
		NodeList entries = docElem.getElementsByTagName("entries");
		List<StorageTocItem> cts = new ArrayList<StorageTocItem>();

		if(segmentTitle != null)
		{
			// empty title: continuing of prev segment and level 1 title
			if(!segmentTitle.isEmpty())
			{
				StorageTocItem contents = new StorageTocItem();
				contents.level = 1;
				contents.title = segmentTitle;
				contents.paraOrdinal = 1;
				cts.add(contents);
			}
			segmentTitleOfs = 1;
		}
		
		if(entries.getLength() > 0)
		{
			NodeList entryList = entries.item(0).getChildNodes();
			for(int i=0, len=entryList.getLength(); i<len; ++i)
			{
				Node entry = entryList.item(i);
				if(!"entry".equals(entry.getNodeName()))
					continue;
				children = entry.getChildNodes();
				StorageTocItem contents = new StorageTocItem();
				for(int j=0, len2 = children.getLength(); j<len2; ++j)
				{
					Node n = children.item(j);
					String txt = n.getTextContent().trim();
					if("level".equals(n.getNodeName()))
						contents.level = (byte)(Integer.parseInt(txt) + segmentTitleOfs);
					else if("title".equals(n.getNodeName()))
						contents.title = txt;
					else if("para_ordinal".equals(n.getNodeName()))
						contents.paraOrdinal = Short.parseShort(txt);
				}
				if(contents.title == null)
					contents.title = "";
				cts.add(contents);
			}
			bs.contents = new StorageTocItem[cts.size()];
			cts.toArray(bs.contents);
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


	private void addContent()
	{
		paraList = new ArrayList<StoragePara>();
		for(String fname : bookFileNames)
		{
			File f = new File(bookDir, fname);
			BufferedReader in = null;
			StoragePara para = null;
			try
			{
				in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
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
						// add previous para
						if(inPara)
							addPara(para, paraSB);
						
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
						
						para = new StoragePara();
						para.cls = cls;
						
						String txt = m.group(4);
						paraSB.setLength(0);
						paraSB.append(txt);
						
						inPara = true;
					}
					else if(inPara)
						// not <p> start, but </p> not reached before: continuing line
						paraSB.append(' ').append(m.group(4));

					if(inPara)
					{
						// if <p> has started or cont'd, look for </p>
						inPara = m.group(5).length() == 0;
						if(!inPara)
							addPara(para, paraSB);
					}
				}
				bs.para = new StoragePara[paraList.size()];
				paraList.toArray(bs.para);
			}
			catch(Exception ex)
			{
				throw new RuntimeException("Adding XML file", ex);
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
						throw new RuntimeException("Closing XML file", ex);
					}
			}
		}
	}


	private void addPara(StoragePara para, StringBuilder paraSB)
	{
		String paraTxt = paraSB.toString();
		para.text = paraTxt;
		int ordinal = paraList.size();
		paraList.add(para);
		ib.addPara(ordinal, paraTxt);
	}


	private Element getXmlRoot(String fileName)
	{
		File f = new File(bookDir, fileName);
		String xml = XmlUtil.readFromFile(f);
		Document doc = XmlUtil.domFromString(xml);
		return doc.getDocumentElement();
	}

	
	public static void addFromStaticList() throws IOException
	{
		Storage store = Storage.SYSTEM;
		Lucene lucene = Lucene.SYSTEM;
		String[] paths = {
			"d:\\wk2\\Sastra\\BBT\\v2012\\xml\\bg",
			"d:\\wk2\\Sastra\\BBT\\v2012\\xml\\SB  1"
		};
		IndexBooks ib = new IndexBooks(lucene);
		ib.init();
		for(String path : paths)
		{
			Log.info(path);
			new AddBook(path, store, ib).run();
		}
		ib.finish();
		store.close();
	}

	
	public static void rebuildOnServer(String user, BlockingQueue responseQueue)
	{
		Storage store = Storage.SYSTEM;
		Lucene lucene = Lucene.SYSTEM;
		IndexBooks ib = null;
		try
		{
			File xmlRoot;
			if(Globals.localEnv)
				xmlRoot = new File("d:\\wk2\\Sastra\\BBT\\v2012\\xml");
			else if(Globals.serverEnv)
				xmlRoot = new File(Globals.cwd, user==null ? "system/xml" : "users/"+user+"/xml");
			else
				xmlRoot = null;
			
			// read dir list
			List<String> books = FileUtil.readTextFile(new File(xmlRoot, "booklist.txt"));
			if(books == null)
				throw new RuntimeException("book list missing from path " + xmlRoot.getAbsolutePath());
			
			store = Storage.forUser(user);
			store.close();

			ib = new IndexBooks(lucene);
			ib.init();
			for(String book : books)
			{
				File f = new File(xmlRoot, book);
				if(!f.exists())
					continue;
				String path = f.getAbsolutePath();
				responseQueue.put(path.trim());
				new AddBook(path, store, ib).run();
			}
			responseQueue.put(true);
			store.close();
		}
		catch(Exception ex)
		{
			try
			{
				responseQueue.put(ex);
			}
			catch(InterruptedException ex1)
			{
			}
		}
		finally
		{
			if(ib != null)
				ib.finish();
			if(user == null)
			{
				try
				{
					store.openForRead();
				}
				catch(IOException ex)
				{
				}
			}
		}
	}

	
	public static void main(String[] args) throws IOException
	{
		addFromStaticList();
	}
}
