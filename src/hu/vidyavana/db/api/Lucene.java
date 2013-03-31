package hu.vidyavana.db.api;

import java.io.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;

public class Lucene
{
	public static Lucene inst = new Lucene();

	private StandardAnalyzer analyzer;
	private Directory index;

	private IndexWriter writer;
	
	public Lucene open()
	{
		try
		{
			analyzer = new StandardAnalyzer(Version.LUCENE_42);
			index = FSDirectory.open(new File("./index"));
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
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, analyzer);
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
