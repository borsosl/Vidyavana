package test.db;

import hu.vidyavana.db.api.*;
import java.sql.*;

public class TestH2
{
	public static void main(String[] args)
	{
		listTable();
		Database.closeAll();
		System.out.println("done");
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
}
