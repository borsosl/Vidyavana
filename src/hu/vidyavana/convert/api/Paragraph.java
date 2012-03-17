package hu.vidyavana.convert.api;

import org.w3c.dom.*;

public class Paragraph
{
	public String cls;
	public StringBuffer sb = new StringBuffer();
	
	
	public void addToDocument(Document doc, Element parent)
	{
		Element para = doc.createElement("p");
		parent.appendChild(para);
		
		if(cls != null)
			para.setAttribute("class", cls);
		para.setTextContent(sb.toString());
	}

}
