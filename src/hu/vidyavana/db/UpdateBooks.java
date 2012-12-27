package hu.vidyavana.db;

import hu.vidyavana.db.data.*;
import hu.vidyavana.util.XmlUtil;
import java.io.File;
import java.util.Map;
import org.w3c.dom.*;

public class UpdateBooks
{
	public int added, updated;
	
	
	public void run()
	{
		String xml = getBooksXml();
		Document doc = XmlUtil.domFromString(xml);
		Element docElem = doc.getDocumentElement();
		String xmlVersion = docElem.getElementsByTagName("version").item(0).getTextContent();
		
		if(xmlVersion.compareTo(SettingsDao.getBooksVersion()) > 0)
			updateDb(docElem, xmlVersion);
	}


	private void updateDb(Element docElem, String xmlVersion)
	{
		BookDao bookDao = new BookDao();
		Map<Integer, Book> bookMap = bookDao.getAllBooks();

		NodeList books = docElem.getElementsByTagName("book");
		for(int i=0, len = books.getLength(); i<len; ++i)
		{
			Node book = books.item(i);
			NodeList children = book.getChildNodes();
			Book xmlBook = new Book();
			for(int j=0, len2 = children.getLength(); j<len2; ++j)
			{
				Node n = children.item(j);
				if("title".equals(n.getNodeName()))
					xmlBook.title = n.getTextContent().trim();
				else if("id".equals(n.getNodeName()))
					xmlBook.id = Integer.parseInt(n.getTextContent().trim());
				else if("parent_id".equals(n.getNodeName()))
					xmlBook.parentId = Integer.parseInt(n.getTextContent().trim());
				else if("priority".equals(n.getNodeName()))
					xmlBook.systemPriority = Integer.parseInt(n.getTextContent().trim());
				else if("version".equals(n.getNodeName()))
					xmlBook.repoVersion = n.getTextContent().trim();
			}
			
			Book dbBook = bookMap.get(xmlBook.id);
			if(dbBook == null)
			{
				bookDao.insertBook(xmlBook);
				++added;
			}
			else
			{
				if(dbBook.parentId == xmlBook.parentId
					&& dbBook.title.equals(xmlBook.title)
					&& dbBook.systemPriority == xmlBook.systemPriority
					&& dbBook.repoVersion.equals(xmlBook.repoVersion))
						continue;
				
				BookDao.updateBook(xmlBook);
				++updated;
			}
		}
		SettingsDao.setBooksVersion(xmlVersion);
	}


	private String getBooksXml()
	{
		return XmlUtil.readFromFile(new File("system.books.xml"));
	}
}
