package hu.vidyavana.db.data;

import hu.vidyavana.db.api.Database;
import java.util.List;

public class ParaDao
{
	public static void updateBookParagraphs(int bookId, List<Para> paras)
	{
		if(paras.size() == 0)
			return;
		deleteBookParagraphs(bookId);
		StringBuilder sb = new StringBuilder(100 + paras.size() * 100);
		sb.append("insert into para (book_id, book_para_ordinal, style, txt) values ");
		for(Para p : paras)
		{
			sb.append('(').append(bookId).append(',').append(p.bookParaOrdinal).append(',')
				.append(p.style).append(",'").append(Database.quote(p.text)).append("'),");
		}
		sb.setLength(sb.length()-1);
		Database.System.execute(sb.toString());
	}

	
	public static void deleteBookParagraphs(int bookId)
	{
		Database.System.execute("delete from para where book_id=" + bookId);
	}

}
