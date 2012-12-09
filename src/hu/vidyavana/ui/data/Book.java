package hu.vidyavana.ui.data;

import java.sql.*;

public class Book
{
	public int id;
	public int parentId;
	public int systemPriority;
	public int userPriority;
	public boolean exist;
	public String title;
	
	
	public Book()
	{
	}
	
	
	public Book(ResultSet rs)
	{
		try {
			id = rs.getInt("id");
		} catch (SQLException ex) {
		}
		try {
			parentId = rs.getInt("parent_id");
		} catch (SQLException ex) {
		}
		try {
			systemPriority = rs.getInt("system_priority");
		} catch (SQLException ex) {
		}
		try {
			userPriority = rs.getInt("user_priority");
		} catch (SQLException ex) {
		}
		try {
			exist = rs.getBoolean("exist");
		} catch (SQLException ex) {
		}
		try {
			title = rs.getString("title");
		} catch (SQLException ex) {
		}
	}
}
