package hu.vidyavana.convert.epub;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EpubParser
{
	public static final String infn = "d:\\temp\\2\\Lord Caitanya's Associates.epub";
	public static final String outfn = "d:\\temp\\2\\Lord Caitanya's Associates.html.trans";
	public static final int BIGGEST_FILESIZE = 1_700_000;
	static boolean markForFolio = false;
	static boolean convertSanskrit = true;
	
	interface HtmlContentAction
	{
		public void init();
		public void process(String html);
		public void finish();
	}


	private void process(HtmlContentAction action)
	{
		try
		{
			ZipInputStream zis = is();
			String txt = readText(zis, "META-INF/container.xml");
			zis.close();
			Matcher m = Pattern.compile("full-path=\"([^\"]+)\"").matcher(txt);
			m.find();
			String opfPath = m.group(1);
			int ix = opfPath.lastIndexOf('/');
			String opfRelPath = ix==-1 ? "" : opfPath.substring(0, ix+1);
			zis = is();
			txt = readText(zis, opfPath);
			zis.close();
			Document xml = xmlDoc(txt);
			Node spine = xml.getDocumentElement().getElementsByTagName("spine").item(0);
			Node man = xml.getDocumentElement().getElementsByTagName("manifest").item(0);
			NodeList manCh = man.getChildNodes();
			/*
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList)xPath.evaluate("/mainfest/spine", xml.getDocumentElement(), XPathConstants.NODESET);
			Node spine = nodes.item(0);
			*/
			NodeList spineCh = spine.getChildNodes();
			for(int i=0, len=spineCh.getLength(); i<len; ++i)
			{
				Node item = spineCh.item(i);
				if(!(item instanceof Element))
					continue;
				Element itemref = (Element) item;
				String idref = itemref.getAttribute("idref");
				if(idref == null)
					continue;
				for(int j=0, len2=manCh.getLength(); j<len2; ++j)
				{
					Node mit = manCh.item(j);
					if(!(mit instanceof Element))
						continue;
					Element it = (Element) mit;
					if(!idref.equals(it.getAttribute("id")))
						continue;
					String mt = it.getAttribute("media-type");
					if(!"application/xhtml+xml".equals(mt))
						continue;
					String href = it.getAttribute("href");
					if(href == null)
						continue;
					href = opfRelPath+href;
					System.out.println(href);
					zis = is();
					txt = readText(zis, href);
					action.process(txt);
					zis.close();
				}
				
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}


	private ZipInputStream is() throws FileNotFoundException
	{
		return new ZipInputStream(new BufferedInputStream(new FileInputStream(infn)));
	}


	private static String readText(ZipInputStream zis, String path) throws IOException
	{
		while(true)
		{
			ZipEntry e = zis.getNextEntry();
			if(e == null)
				break;
			if(e.getName().equals(path))
			{
				final byte[] inbuf = new byte[10000], outbuf = new byte[BIGGEST_FILESIZE];
				int ptr = 0;
				int length;
				while((length = zis.read(inbuf, 0, inbuf.length)) > 0)
				{
					System.arraycopy(inbuf, 0, outbuf, ptr, length);
					ptr += length;
				}
				return new String(outbuf, 0, ptr, "UTF-8");
			}
		}
		return null;
	}


	public static Document xmlDoc(String xmlString)
	{
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document ret = null;

		try
		{
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		}
		catch(ParserConfigurationException e)
		{
			e.printStackTrace();
		}

		try
		{
			ret = builder.parse(new InputSource(new StringReader(xmlString)));
		}
		catch(SAXException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return ret;
	}


	public static void main(String[] args)
	{
		EpubMerge em = new EpubMerge();
		em.init();
		new EpubParser().process(em);
		em.finish();
	}
	
	
	static class EpubMerge implements HtmlContentAction
	{
		BufferedWriter of;
		int filenum = 0;
		Map<Character, String> cmap = new HashMap<>();

		@Override
		public void init()
		{
			try
			{
				of = new BufferedWriter(new FileWriter(outfn));
				utf8RevMap();
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}

		private void utf8RevMap()
		{
			cmap.put('ā', "@`#a");
			cmap.put('ī', "@`#i");
			cmap.put('ū', "@`#u");
			cmap.put('ḍ', "@`#d");
			cmap.put('ḥ', "@`#h");
			cmap.put('ḷ', "@`#l");
			cmap.put('ḹ', "@`#lrii@");
			cmap.put('ṁ', "@`#m");
			cmap.put('ṅ', "@`#ng@");
			cmap.put('ṇ', "@`#n");
			cmap.put('ñ', "@`#ny@");
			cmap.put('ṛ', "@`#r");
			cmap.put('ṝ', "@`#rii@");
			cmap.put('ṣ', "@`#s");
			cmap.put('ś', "@`#sh@");
			cmap.put('ṭ', "@`#t");
			
			cmap.put('Ā', "@`#A");
			cmap.put('Ī', "@`#I");
			cmap.put('Ū', "@`#U");
			cmap.put('Ḍ', "@`#D");
			cmap.put('Ḥ', "@`#H");
			cmap.put('Ḷ', "@`#L");
			cmap.put('Ḹ', "@`#LRII@");
			cmap.put('Ṁ', "@`#M");
			cmap.put('Ṅ', "@`#NG@");
			cmap.put('Ṇ', "@`#N");
			cmap.put('Ñ', "@`#NY@");
			cmap.put('Ṛ', "@`#R");
			cmap.put('Ṝ', "@`#RII@");
			cmap.put('Ṣ', "@`#S");
			cmap.put('Ś', "@`#SH@");
			cmap.put('Ṭ', "@`#T");
		}

		@Override
		public void process(String html)
		{
			boolean in = filenum == 0;
			++filenum;
			String[] split = html.split("\n");
			for(String line : split)
			{
				if(line.endsWith("\r"))
					line = line.substring(0, line.length()-1);
				if(!in)
				{
					int ix = line.indexOf("<body");
					if(ix > -1)
					{
						in = true;
						ix = line.indexOf('>', ix+5);
						if(ix == -1)
						{
							System.out.println("body tag not on one line");
						}
						line = line.substring(ix+1).trim();
						if(line.isEmpty())
							continue;
					}
				}
				else
				{
					int ix = line.indexOf("</body");
					if(ix > -1)
					{
						in = false;
						line = line.substring(0, ix).trim();
						if(line.isEmpty())
							continue;
					}
					if(markForFolio )
					{
						ix = line.indexOf("</p");
						if(ix == -1)
							ix = line.indexOf("</div");
						if(ix > -1)
							line = line.substring(0, ix) + "`@recbr" + line.substring(ix);
					}
				}
				if(in)
					try
					{
						of.write(convert(line));
						of.write("\r\n");
					}
					catch(IOException ex)
					{
						System.out.println("write error");
						System.exit(1);
					}
			}
			if(markForFolio)
				try
				{
					of.write("`@chapbr\r\n");
				}
				catch(IOException ex)
				{
					System.out.println("write error");
					System.exit(1);
				}
		}

		private String convert(String line)
		{
			if(!convertSanskrit)
				return line;
			StringBuilder sb = new StringBuilder(500);
			for(int i=0, len=line.length(); i<len; ++i)
			{
				char c = line.charAt(i);
				String conv = cmap.get(c);
				if(conv == null)
					sb.append(c);
				else
					sb.append(conv);
			}
			return sb.toString();
		}

		@Override
		public void finish()
		{
			try
			{
				of.write("</body>\r\n</html>\r\n");
				of.close();
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
		
	}
}
