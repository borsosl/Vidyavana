package hu.vidyavana.db.api;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import hu.vidyavana.util.Globals;

public class Lucene
{
	public static final Version VERSION = Version.LATEST;
	public static Lucene SYSTEM = forUser(null);

	private Directory index;
	private IndexWriter writer;
	private File dir;

	
	public Lucene(String user)
	{
		if(user == null)
			dir = new File(Globals.cwd, "system/index");
		else
			dir = new File(Globals.cwd, "users/"+user+"/index");
	}
	
	
	public static Lucene forUser(String user)
	{
		if(user == null && SYSTEM != null)
			return SYSTEM;
		// TODO cache for some time
		return new Lucene(user);
	}
	
	
	public Lucene open()
	{
		try
		{
			index = FSDirectory.open(dir.toPath());
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
			HtmlAnalyzer analyzer = new HtmlAnalyzer();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
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
			SYSTEM.open();
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher sr = new IndexSearcher(reader);
			Term t = new Term("text", "olvastak");
			TopDocs res = sr.search(new TermQuery(t), 10);
			System.out.println(res.totalHits);
			for(ScoreDoc sd : res.scoreDocs)
			{
				Document doc = reader.document(sd.doc);
				System.out.println(doc.get("bookId")+"/"+doc.get("segment")+"/"+doc.get("ordinal"));
			}
			reader.close();
			SYSTEM.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
