package hu.vidyavana.db;

import hu.vidyavana.convert.api.DiacriticToLatinPairs;
import hu.vidyavana.db.api.ResultSetCallback;
import hu.vidyavana.db.data.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;


public class IndexBook
{
	private static Pattern REMOVE_TAGS = Pattern.compile("</?[a-z]{1,9}>");
	public static Pattern WORD = Pattern.compile("[A-Za-záéíóöőúüűÁÉÍÓÖŐÚÜŰāīūḍḥḷḹṁṅṇñṛṝṣśṭĀĪŪḌḤḶḸṀṄṆÑṚṜṢŚṬ]+");
	
	private int bookId;
	private HashMap<String, HashSet<Integer>> words;


	
	public IndexBook(int bookId)
	{
		this.bookId = bookId;
	}
	
	
	public void run()
	{
		init();
		iterateParagraphs();
		addToDatabase();
	}


	private void init()
	{
		words = new HashMap<>();
	}


	private void iterateParagraphs()
	{
		ParaDao.getBookParagraphs(bookId, new ResultSetCallback()
		{
			@Override
			public void useResultSet(ResultSet rs) throws SQLException
			{
				int ordinalCol = rs.findColumn("book_para_ordinal");
				int textCol = rs.findColumn("txt");
				while(rs.next())
				{
					int ordinal = rs.getInt(ordinalCol);
					String text = rs.getString(textCol);
					addParagraph(ordinal, text);
				}
			}
		});
	}


	protected void addParagraph(int ordinal, String text)
	{
		REMOVE_TAGS.matcher(text).replaceAll("");
		Matcher m = IndexBook.WORD.matcher(text);
		int ix = 0;
		StringBuilder sb = new StringBuilder(200);
		while(true)
		{
			if(m.find(ix))
			{
				String w = m.group().toLowerCase();
				sb.setLength(0);
				int len = w.length();
				for(int j = 0; j < len; ++j)
				{
					char orig = w.charAt(j);
					int latin = DiacriticToLatinPairs.convert(orig);
					sb.append(latin == 0 ? (char) orig : (char) latin);
				}
				String latin = sb.toString().toLowerCase();
				if(latin.length()>1 && !("az".equals(latin)))
				{
					HashSet<Integer> occur = words.get(latin);
					if(occur == null)
					{
						occur = new HashSet<>();
						occur.add(ordinal);
						words.put(latin, occur);
					}
					else
						occur.add(ordinal);
				}
				ix = m.end();
			}
			else
				break;
		}
	}


	private void addToDatabase()
	{
		OccurDao.updateOneBook(bookId, words);
	}
}
