package hu.vidyavana.db.data;

import hu.vidyavana.db.api.*;
import java.sql.*;
import java.util.*;


public class WordDao
{
	public static HashMap<String,Integer> getAllWords()
	{
		final HashMap<String, Integer> wordIds = new HashMap<>();
		Database.System.query("select * from word", new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				int idCol = rs.findColumn("id");
				int wordCol = rs.findColumn("word");

				while(rs.next())
				{
					wordIds.put(rs.getString(wordCol), rs.getInt(idCol));
				}
			}
		});
		return wordIds;
	}
	
	
	public static void addWords(List<String> words)
	{
		if(words.size() == 0)
			return;
		
		StringBuilder sb = new StringBuilder(100 + words.size() * 100);
		sb.append("insert into word (word) values ");
		for(String word : words)
			sb.append("('").append(word).append("'),");
		sb.setLength(sb.length()-1);
		Database.System.execute(sb.toString());
	}
}
