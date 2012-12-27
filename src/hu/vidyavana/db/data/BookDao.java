package hu.vidyavana.db.data;

import hu.vidyavana.db.api.*;
import java.sql.*;
import java.util.*;

public class BookDao
{
	public int maxUserPriority;

	
	public Map<Integer, Book> getAllBooks()
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
					Book book = getBook(rs);
					bookMap.put(book.id, book);
					if(book.userPriority > maxUserPriority)
						maxUserPriority = book.userPriority;
				}
				super.useResultSet(rs);
			}
		});
		return bookMap;
	}
	
	
	public void insertBook(final Book book)
	{
		Database.System.wrapPreparedStatement("insert into book " +
			"(id, parent_id, title, system_priority, user_priority, repo_version) " +
			"values (?,?,?,?,?,?)", new StatementCallback()
		{
			@Override
			public void usePreparedStatement(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, book.id);
				stmt.setInt(2, book.parentId);
				stmt.setString(3, book.title);
				stmt.setInt(4, book.systemPriority);
				stmt.setInt(5, ++maxUserPriority);
				stmt.setString(6, book.repoVersion);
				stmt.executeUpdate();
			}
		});
	}
	
	
	public static void updateBook(final Book book)
	{
		Database.System.wrapPreparedStatement("update book " +
			"set parent_id=?, title=?, system_priority=?, repo_version=? " +
			"where id=?", new StatementCallback()
		{
			@Override
			public void usePreparedStatement(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, book.parentId);
				stmt.setString(2, book.title);
				stmt.setInt(3, book.systemPriority);
				stmt.setString(4, book.repoVersion);
				stmt.setInt(5, book.id);
				stmt.executeUpdate();
			}
		});
	}
	
	
	public static Book getBook(ResultSet rs)
	{
		Book book = new Book();
		try {
			book.id = rs.getInt("id");
		} catch (SQLException ex) {
		}
		try {
			book.parentId = rs.getInt("parent_id");
		} catch (SQLException ex) {
		}
		try {
			book.title = rs.getString("title");
		} catch (SQLException ex) {
		}
		try {
			book.systemPriority = rs.getInt("system_priority");
		} catch (SQLException ex) {
		}
		try {
			book.userPriority = rs.getInt("user_priority");
		} catch (SQLException ex) {
		}
		try {
			book.repoVersion = rs.getString("repo_version");
		} catch (SQLException ex) {
		}
		try {
			book.dbVersion = rs.getString("db_version");
		} catch (SQLException ex) {
		}
		return book;
	}
}
