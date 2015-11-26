package hu.vidyavana.db;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import hu.vidyavana.db.api.Lucene;

public class IndexBooks
{
	private Lucene lucene;
	private IndexWriter iw;
	private FieldType txtFieldType;
	private int bookId;
	private int segment;

	
	public IndexBooks(Lucene lucene)
	{
		this.lucene = lucene;
	}


	public void init()
	{
		iw = lucene.writer();
		txtFieldType = new FieldType();
		txtFieldType.setTokenized(true);
		txtFieldType.setStored(false);
		txtFieldType.setStoreTermVectors(false);
		txtFieldType.setOmitNorms(true);
		txtFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		txtFieldType.freeze();
	}


	public void initBook(int bookId, int segment)
	{
		this.bookId = bookId;
		this.segment = segment;
	}


	public void addPara(int ordinal, String txt)
	{
		Document doc = new Document();
		doc.add(new IntField("bookId", bookId, Store.YES));
		doc.add(new IntField("segment", segment, Store.YES));
		doc.add(new IntField("ordinal", ordinal, Store.YES));
		doc.add(new Field("text", txt, txtFieldType));
		try
		{
			// TODO transaction
			iw.addDocument(doc);
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}


	public void finish()
	{
		lucene.closeWriter();
	}
}
