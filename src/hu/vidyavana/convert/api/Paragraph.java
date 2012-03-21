package hu.vidyavana.convert.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.List;

public class Paragraph
{
	public String cls;
	public ParagraphStyle style;
	public Integer verse;
	public StringBuffer text = new StringBuffer();
	public List<String> footnote = new ArrayList<String>();
	
	
	public void addToDocument(Document doc, Element parent)
	{
		Element para = doc.createElement("p");
		parent.appendChild(para);
		
		if(cls != null)
			para.setAttribute("class", cls);
		if(style != null)
			para.setAttribute("style", style.toString());
		para.setTextContent(text.toString());
	}

}
