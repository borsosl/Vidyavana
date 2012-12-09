package hu.vidyavana.db;

import hu.vidyavana.db.api.*;
import hu.vidyavana.ui.data.Book;
import hu.vidyavana.util.XmlUtil;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.w3c.dom.*;

public class UpdateBooks
{
	String dbVersion;
	int maxUserPriority;
	public int added, updated;
	
	
	public void run()
	{
		String xml = getBooksXml();
		Document doc = XmlUtil.domFromString(xml);
		Element docElem = doc.getDocumentElement();
		String xmlVersion = docElem.getElementsByTagName("version").item(0).getTextContent();
		
		Database.System.query("select books_version from settings", new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				rs.next();
				dbVersion = rs.getString(1);
			}
		});

		if(xmlVersion.compareTo(dbVersion) > 0)
			updateDb(docElem, xmlVersion);
	}


	private void updateDb(Element docElem, String xmlVersion)
	{
		final Map<Integer, Book> bookMap = new HashMap<Integer, Book>();
		maxUserPriority = 0;
		Database.System.query("select * from book", new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				while(rs.next())
				{
					Book book = new Book(rs);
					bookMap.put(book.id, book);
					if(book.userPriority > maxUserPriority)
						maxUserPriority = book.userPriority;
				}
				super.useResultSet(rs);
			}
		});

		NodeList books = docElem.getElementsByTagName("book");
		int len = books.getLength();
		for(int i=0; i<len; ++i)
		{
			Node book = books.item(i);
			NodeList children = book.getChildNodes();
			final Book xmlBook = new Book();
			int len2 = children.getLength();
			for(int j=0; j<len2; ++j)
			{
				Node n = children.item(j);
				if("id".equals(n.getNodeName()))
					xmlBook.id = Integer.parseInt(n.getTextContent());
				else if("parent_id".equals(n.getNodeName()))
					xmlBook.parentId = Integer.parseInt(n.getTextContent());
				else if("priority".equals(n.getNodeName()))
					xmlBook.systemPriority = Integer.parseInt(n.getTextContent());
				else if("title".equals(n.getNodeName()))
					xmlBook.title = n.getTextContent();
			}
			
			Book dbBook = bookMap.get(xmlBook.id);
			if(dbBook == null)
			{
				Database.System.wrapPreparedStatement("insert into book " +
						"(id, parent_id, system_priority, user_priority, title) " +
						"values (?,?,?,?,?)", new StatementCallback()
				{
					@Override
					public void usePreparedStatement(PreparedStatement stmt) throws SQLException
					{
						stmt.setInt(1, xmlBook.id);
						stmt.setInt(2, xmlBook.parentId);
						stmt.setInt(3, xmlBook.systemPriority);
						stmt.setInt(4, ++maxUserPriority);
						stmt.setString(5, xmlBook.title);
						stmt.executeUpdate();
					}
				});
				++added;
			}
			else
			{
				if(dbBook.parentId == xmlBook.parentId && dbBook.systemPriority == xmlBook.systemPriority
					&& dbBook.title.equals(xmlBook.title))
						continue;
				
				Database.System.wrapPreparedStatement("update book " +
					"set parent_id=?, system_priority=?, title=? " +
					"where id=?", new StatementCallback()
				{
					@Override
					public void usePreparedStatement(PreparedStatement stmt) throws SQLException
					{
						stmt.setInt(1, xmlBook.parentId);
						stmt.setInt(2, xmlBook.systemPriority);
						stmt.setString(3, xmlBook.title);
						stmt.setInt(4, xmlBook.id);
						stmt.executeUpdate();
					}
				});
				++updated;
			}
		}
		Database.System.execute("update settings set books_version='"+xmlVersion+"'");
	}


	private String getBooksXml()
	{
		return readFromFile();
	}


	private String readFromFile()
	{
		try
		{
			Reader in = new InputStreamReader(new FileInputStream("system.books.xml"), "UTF-8");
			char[] arr = new char[100000];
			int len = in.read(arr);
			return new String(arr, 0, len);
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Unable to read books file.", ex);
		}
	}

}
