package hu.vidyavana.util;

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.xml.sax.*;

public class XmlUtil
{
	public static Document domFromString(String in)
	{
		try
		{
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(in));
			return db.parse(is);
		}
		catch(ParserConfigurationException | SAXException | IOException ex)
		{
			throw new RuntimeException("Unable to parse xml.", ex);
		}
	}
	
	
	public static String noMarkup(String text)
	{
		StringBuilder sb = new StringBuilder(text.length());
		boolean inMarkup = false;
		for(int i=0, len=text.length(); i<len; ++i)
		{
			char c = text.charAt(i);
			if(inMarkup)
			{
				if(c == '>')
					inMarkup = false;
			}
			else
			{
				if(c == '<')
					inMarkup = true;
				else
					sb.append(c);
			}
		}
		return sb.toString();
	}


	public static String readFromFile(File file)
	{
		try(Reader in = new InputStreamReader(new FileInputStream(file), "UTF-8"))
		{
			char[] arr = new char[(int) file.length()];
			int len = in.read(arr);
			return new String(arr, 0, len);
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Unable to read file " + file.getName(), ex);
		}
	}
}
