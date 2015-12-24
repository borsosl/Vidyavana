package hu.vidyavana.db.api;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import org.h2.Driver;

public enum Sql
{
	System;
	
	public static Path pathToSqlFiles;

	
	static
	{
		Driver.load();
	}
	
	
	private Connection conn;
	
	
	public Connection connection()
	{
		try
		{
			if(conn == null || conn.isClosed())
				conn = DriverManager.getConnection("jdbc:h2:"+path(),"","");

			return conn;
		}
		catch(SQLException ex)
		{
			throw new RuntimeException("Failed to create connection for DB "+name(), ex);
		}
	}

	
	public void close()
	{
		if(conn != null)
		{
			try
			{
				if(!conn.isClosed())
					conn.close();
			}
			catch(SQLException ex)
			{
			}
			conn = null;
		}
	}
	
	
	public static void closeAll()
	{
		for(Sql db : Sql.values())
			db.close();
	}

	
	public String path()
	{
		try
		{
			String dir = pathToSqlFiles.toRealPath().toUri().getPath();
			return dir+name();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}


	public void wrapStatement(StatementCallback stcb)
	{
		connection();
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			stcb.useStatement(stmt);
		}
		catch(SQLException ex)
		{
			throw new RuntimeException("Failed to execute SQL", ex);
		}
		finally
		{
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(SQLException ex)
			{
				throw new RuntimeException("Failed to close statement", ex);
			}
		}
	}


	public void wrapPreparedStatement(String sql, StatementCallback stcb)
	{
		connection();
		PreparedStatement stmt = null;
		try
		{
			stmt = conn.prepareStatement(sql);
			stcb.usePreparedStatement(stmt);
		}
		catch(SQLException ex)
		{
			throw new RuntimeException("Failed to execute SQL", ex);
		}
		finally
		{
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(SQLException ex)
			{
				throw new RuntimeException("Failed to close statement", ex);
			}
		}
	}


	public void execute(final String sql)
	{
		wrapStatement(new StatementCallback()
		{
			@Override
			public void useStatement(Statement stmt) throws SQLException
			{
				stmt.execute(sql);
			}
		});
	}


	public void update(final String sql)
	{
		wrapStatement(new StatementCallback()
		{
			@Override
			public void useStatement(Statement stmt) throws SQLException
			{
				stmt.executeUpdate(sql);
			}
		});
	}


	public void query(final String sql, final ResultSetCallback rscb)
	{
		wrapStatement(new StatementCallback()
		{
			@Override
			public void useStatement(Statement stmt) throws SQLException
			{
				ResultSet rs = null;
				try
				{
					rs = stmt.executeQuery(sql);
					rscb.useResultSet(rs);
				}
				finally
				{
					if(rs != null)
						rs.close();
				}
			}
		});
	}
	
	
	public void autoCommit(boolean on)
	{
		try
		{
			connection().setAutoCommit(on);
		}
		catch(SQLException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	
	public void commit()
	{
		try
		{
			connection().commit();
		}
		catch(SQLException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	
	public void rollback()
	{
		try
		{
			connection().rollback();
		}
		catch(SQLException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	
	public static String quote(String str)
	{
		if(str.indexOf('\'') == -1)
			return str;
		
		return str.replace("'", "''");
	}
}
