package hu.vidyavana.convert.api;

import java.util.*;
import org.w3c.dom.*;

public class Paragraph
{
	public String tagName;
	public ParagraphClass cls;
	public ParagraphStyle style;
	public StringBuffer text = new StringBuffer();
	public List<String> footnote = new ArrayList<String>();
	
	
	public void addToDocument(Document doc, Element parent)
	{
		if(tagName==null) tagName = "p";
		Element para = doc.createElement(tagName);
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

}
