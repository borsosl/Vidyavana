package hu.vidyavana.util;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

	public static List<XmlToken> tokenize(String para) {
        List<XmlToken> tokens = new ArrayList<>();
        XmlToken current = new XmlToken();
        boolean inToken = false;
        StringBuilder sb = new StringBuilder(50);
		int printPos = 0;
		boolean tokenEndingAfterWhite = false;
        for(int i = 0; i < para.length(); ++i) {
            char c = para.charAt(i);
            boolean tokenEnd = false;
            if(tokenEndingAfterWhite) {
				if(Character.isWhitespace(c))
					current.spaceFollows = true;
				else
					--i;
				tokenEnd = true;
				tokenEndingAfterWhite = false;
			} else if(current.isTag) {
                sb.append(c);
                if(c == '>')
                    tokenEndingAfterWhite = true;
            } else if(current.isEntity) {
                sb.append(c);
                if(c == ';')
                    tokenEndingAfterWhite = true;
            } else if(Character.isWhitespace(c)) {
                if(inToken) {
                    tokenEnd = true;
					current.spaceFollows = true;
				}
            } else if(inToken && (c == '<' || c == '&' || c == '-')) {
                tokenEnd = true;
                --i;
            } else {
                sb.append(c);
                if(!inToken)
                    current.sourcePos = i;
                inToken = true;
				if(c == '-')
					tokenEndingAfterWhite = true;
				else if(c == '<')
                    current.isTag = true;
                else if(c == '&')
                    current.isEntity = true;
            }

            if(tokenEnd || inToken && i == para.length()-1) {
                current.text = sb.toString();
                current.printLength = current.isTag ? 0 : current.isEntity ? 1 : current.text.length();
				current.printPos = printPos;
				printPos += current.printLength + (current.spaceFollows ? 1 : 0);
                tokens.add(current);
                current = new XmlToken();
                inToken = false;
                sb.setLength(0);
            }
        }
        return tokens;
    }
}
