package test.db;

import hu.vidyavana.db.api.*;
import java.sql.*;

@SuppressWarnings("unused")
public class TestH2
{
	public static void main(String[] args)
	{
		try
		{
			listContentsText();
		}
		finally
		{
			Database.closeAll();
		}
	}

	private static void createTable()
	{
		Database.System.execute("create table t1 (" +
				"f1 integer," +
				"f2 text" +
				")");
	}

	private static void fillTable()
	{
		Database db = Database.System;
		db.update("insert into t1 values (1,'s1'),(2,'s2')");
	}

	
	private static void listTable()
	{
		Database.System.query("select * from t1", new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				while(rs.next())
				{
					System.out.println(rs.getInt(1));
					System.out.println(rs.getString(2));
				}
			}
		});
	}

	
	private static void listContentsText()
	{
		Database.System.query("select top 20 c.division, p.txt from contents c left join para p on c.book_id=p.book_id and c.book_para_ordinal=p.book_para_ordinal", new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				while(rs.next())
				{
//					Blob clob = rs.getBlob(2);
//					byte[] bytes = clob.getBytes(1, (int) clob.length());
//					String text;
//					try
//					{
//						text = new String(bytes, "UTF-8");
//					}
//					catch(UnsupportedEncodingException ex)
//					{
//						text = "+!%/";
//					}
					System.out.println(rs.getString(1)+" "+rs.getString(2));
				}
			}
		});
	}
}
