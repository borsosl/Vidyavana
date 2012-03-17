package hu.vidyavana.convert.api;

import java.util.*;
import org.w3c.dom.*;

public class Chapter
{
	public Integer number;
	public String ref;
	public String title;
	public String head;
	public List<Paragraph> para = new ArrayList<Paragraph>();

	
	public void addToDocument(Document doc, Element parent)
	{
		Element chapt = doc.createElement("chapter");
		parent.appendChild(chapt);
		
		Element child;
		if(number != null)
		{
			child = doc.createElement("number");
			child.setTextContent(number.toString());
			chapt.appendChild(child);
		}
		if(ref != null)
		{
			child = doc.createElement("ref");
			child.setTextContent(ref);
			chapt.appendChild(child);
		}
		if(title != null)
		{
			child = doc.createElement("title");
			child.setTextContent(title);
			chapt.appendChild(child);
		}
		if(head != null)
		{
			child = doc.createElement("head");
			child.setTextContent(head);
			chapt.appendChild(child);
		}
		for(Paragraph par : para)
			par.addToDocument(doc, chapt);
	}
}
