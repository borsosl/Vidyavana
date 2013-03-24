package hu.vidyavana.db.data;

import hu.vidyavana.db.api.*;
import hu.vidyavana.util.Encrypt;
import java.sql.*;
import java.util.List;

public class ParaDao
{
	public static void updateBookParagraphs(final int bookId, List<Para> paras)
	{
		if(paras.size() == 0)
			return;
		deleteBookParagraphs(bookId);
		try
		{
			Database.System.autoCommit(false);
			for(final Para p : paras)
			{
				Database.System.wrapPreparedStatement(
					"insert into para (book_id, book_para_ordinal, style, txt) values (?,?,?,?)",
					new StatementCallback()
				{
						@Override
						public void usePreparedStatement(PreparedStatement stmt) throws SQLException
						{
							stmt.setInt(1, bookId);
							stmt.setInt(2, p.bookParaOrdinal);
							stmt.setInt(3, p.style);
							stmt.setBytes(4, Encrypt.getInstance().encrypt(p.text));
							stmt.executeUpdate();
						}
				});
			}
			Database.System.commit();
		}
		catch(Exception ex)
		{
			Database.System.rollback();
			throw ex;
		}
		finally
		{
			Database.System.autoCommit(true);			
		}
		
	}

	
	public static void deleteBookParagraphs(int bookId)
	{
		Database.System.execute("delete from para where book_id=" + bookId);
	}

	
	public static void getBookParagraphs(int bookId, ResultSetCallback callback)
	{
		Database.System.query("select * from para" +
				" where book_id=" + bookId +
				" order by book_id, book_para_ordinal", callback);
	}
}
