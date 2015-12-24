package hu.vidyavana.db.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import hu.vidyavana.db.api.ResultSetCallback;
import hu.vidyavana.db.api.Sql;
import hu.vidyavana.db.api.StatementCallback;
import hu.vidyavana.db.model.User;

public class UserDao
{
	public static List<User> getAllUsers(final boolean forList)
	{
		final List<User> users = new ArrayList<User>();
		Sql.System.query("select * from user order by email", new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				while(rs.next())
				{
					User user = getUser(rs, forList);
					users.add(user);
				}
				super.useResultSet(rs);
			}
		});
		return users;
	}


	public static User findUserByEmail(final String email)
	{
		final User[] user = new User[1];
		Sql.System.wrapPreparedStatement("select * from user where email=?", new StatementCallback()
		{
			@Override
			public void usePreparedStatement(PreparedStatement stmt) throws SQLException
			{
				stmt.setString(1, email);
				ResultSet rs = stmt.executeQuery();
				if(!rs.next())
					return;
				user[0] = getUser(rs, false);
			}
		});
		return user[0];
	}
	
	
	public static void insertUser(final User user)
	{
		Sql.System.wrapPreparedStatement("insert into user " +
			"(admin, email, password, name, reg_token, access) " +
			"values (?,?,?,?,?,?)", new StatementCallback()
		{
			@Override
			public void usePreparedStatement(PreparedStatement stmt) throws SQLException
			{
				stmt.setString(1, user.adminLevel.name());
				stmt.setString(2, user.email);
				stmt.setString(3, user.password);
				stmt.setString(4, user.name);
				stmt.setString(5, user.regToken);
				stmt.setString(6, user.accessStr);
				stmt.executeUpdate();
			}
		});
	}
	
	
	public static void updateUser(final User user)
	{
		Sql.System.wrapPreparedStatement("update user " +
			"set admin=?, email=?, password=?, name=?, reg_token=?, access=? " +
			"where id=?", new StatementCallback()
		{
			@Override
			public void usePreparedStatement(PreparedStatement stmt) throws SQLException
			{
				stmt.setString(1, user.adminLevel.name());
				stmt.setString(2, user.email);
				stmt.setString(3, user.password);
				stmt.setString(4, user.name);
				stmt.setString(5, user.regToken);
				stmt.setString(6, user.accessStr);
				stmt.executeUpdate();
			}
		});
	}
	
	
	public static void deleteUser(final String email)
	{
		Sql.System.wrapPreparedStatement("delete from user where email=?", new StatementCallback()
		{
			@Override
			public void usePreparedStatement(PreparedStatement stmt) throws SQLException
			{
				stmt.setString(1, email);
				stmt.execute();
			}
		});
	}
	
	
	public static User getUser(ResultSet rs, boolean forList)
	{
		User user = new User();
		try {
			user.id = rs.getInt("id");
		} catch (SQLException ex) {
		}
		try {
			user.adminLevel = User.AdminLevel.valueOf(rs.getString("admin"));
		} catch (Exception ex) {
		}
		try {
			user.email = rs.getString("email");
		} catch (SQLException ex) {
		}
		try {
			user.name = rs.getString("name");
		} catch (SQLException ex) {
		}
		try {
			user.accessStr = rs.getString("access");
		} catch (SQLException ex) {
		}
		if(!forList)
		{
			try {
				user.password = rs.getString("password");
			} catch (SQLException ex) {
			}
			try {
				user.regToken = rs.getString("reg_token");
			} catch (SQLException ex) {
			}
		}
		return user;
	}
}
