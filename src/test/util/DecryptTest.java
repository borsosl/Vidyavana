package test.util;

import hu.vidyavana.db.api.*;
import hu.vidyavana.util.Encrypt;
import java.sql.*;
import java.util.*;

public class DecryptTest
{
	public static void run()
	{
		final Encrypt enc = Encrypt.getInstance();
		enc.init();
		final List<String> list = new ArrayList<String>(); 
		long t1 = System.currentTimeMillis();
		Database.System.query("select top 1000 txt from para", new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				while(rs.next())
				{
					list.add(enc.decrypt(rs.getBytes(1)));
				}
			}
		});
		System.out.println(list.get(100));
		System.out.println(System.currentTimeMillis() - t1);
	}
}
