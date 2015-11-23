package hu.vidyavana.db.api;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Lucene
{
	public static final Version VERSION = Version.LATEST;
	public static Lucene inst = new Lucene();

	private StandardAnalyzer analyzer;
	private Directory index;

	private IndexWriter writer;
	
	public Lucene open()
	{
		try
		{
			analyzer = new StandardAnalyzer();
			index = FSDirectory.open(new File(".").toPath());
			return this;
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	
	public void close()
	{
		if(index == null)
			return;
		try
		{
			index.close();
			index = null;
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	
	public IndexWriter writer()
	{
		try
		{
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			writer = new IndexWriter(index, config);
			return writer;
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	
	public void closeWriter()
	{
		try
		{
			writer.close();
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	
	public void test()
	{
		try
		{
			inst.open();
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher sr = new IndexSearcher(reader);
			Term t = new Term("text", "olvastak");
			TopDocs res = sr.search(new TermQuery(t), 10);
			System.out.println(res.totalHits);
			for(ScoreDoc sd : res.scoreDocs)
			{
				Document doc = reader.document(sd.doc);
				System.out.println(doc.get("bid"));
				System.out.println(doc.get("ord"));
			}
			reader.close();
			inst.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
