package hu.vidyavana.convert.api;

import java.util.*;
import org.w3c.dom.*;

public class Paragraph
{
	public String cls;
	public Integer verse;
	public StringBuffer sb = new StringBuffer();
	public List<String> footnote = new ArrayList<String>();
	
	
	public void addToDocument(Document doc, Element parent)
	{
		Element para = doc.createElement("p");
		parent.appendChild(para);
		
		if(cls != null)
			para.setAttribute("class", cls);
		para.setTextContent(sb.toString());
	}

}
