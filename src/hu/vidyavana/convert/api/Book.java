package hu.vidyavana.convert.api;

import java.io.OutputStream;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

public class Book
{
	public String title;
	public List<Chapter> chapter = new ArrayList<Chapter>();


	public void createDocument(OutputStream out) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.newDocument();

		Element book = document.createElement("book");
		document.appendChild(book);
		
		Element child;
		if(title != null)
		{
			child = document.createElement("title");
			child.setTextContent(title);
			book.appendChild(child);
		}
		for(Chapter ch : chapter)
			ch.addToDocument(document, book);

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(out);
		transformer.transform(source, result);
	}
}
