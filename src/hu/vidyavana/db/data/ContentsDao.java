package hu.vidyavana.db.data;

import hu.vidyavana.db.api.Database;
import java.util.List;

public class ContentsDao
{
	public static void updateBookContents(int bookId, List<Contents> contents)
	{
		if(contents.size() == 0)
			return;
		deleteBookContents(bookId);
		StringBuilder sb = new StringBuilder(100 + contents.size() * 100);
		sb.append("insert into contents (book_id, level, division, title, book_toc_ordinal, book_para_ordinal) values ");
		for(Contents c : contents)
		{
			sb.append('(').append(bookId).append(',').append(c.level).append(",'")
				.append(Database.quote(c.division)).append("','")
				.append(Database.quote(c.title)).append("',")
				.append(c.bookTocOrdinal).append(',').append(c.bookParaOrdinal).append("),");
		}
		sb.setLength(sb.length()-1);
		Database.System.execute(sb.toString());
	}

	
	public static void deleteBookContents(int bookId)
	{
		Database.System.execute("delete from contents where book_id=" + bookId);
	}
}
