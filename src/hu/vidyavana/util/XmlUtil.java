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
}
