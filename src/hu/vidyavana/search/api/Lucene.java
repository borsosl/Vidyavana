package hu.vidyavana.search.api;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import hu.vidyavana.util.Globals;

public class Lucene
{
	public static final Version VERSION = Version.LATEST;
	public static Lucene SYSTEM = forUser(null);

	protected File dir;
	protected Directory index;
	protected IndexWriter writer;
	protected DirectoryReader reader;
	protected IndexSearcher searcher;

	
	public Lucene(String user)
	{
		setupDirectory(user);
		open();
	}


	protected void setupDirectory(String user)
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
			if(index == null)
				index = FSDirectory.open(dir.toPath());
			return this;
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Opening index dir", ex);
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
			throw new RuntimeException("Closing index dir", ex);
		}
	}
	
	
	public IndexWriter writer()
	{
		try
		{
			if(reader != null)
			{
				reader.close();
				reader = null;
				searcher = null;
			}
			IndexWriterConfig config = createConfig();
			writer = new IndexWriter(index, config);
			return writer;
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Opening index writer", ex);
		}
	}


	protected IndexWriterConfig createConfig()
	{
		HtmlAnalyzer analyzer = new HtmlAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		return config;
	}
	
	
	public void closeWriter()
	{
		try
		{
			if(writer == null)
				return;
			writer.close();
			writer = null;
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Closing index writer", ex);
		}
	}
	
	
	public DirectoryReader reader()
	{
		try
		{
			if(writer != null)
				return null;

			if(reader == null)
				reader = DirectoryReader.open(index);

			return reader;
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Opening index reader", ex);
		}
	}
	
	
	public IndexSearcher searcher()
	{
		if(reader() == null)
			return null;

		if(searcher == null)
			searcher = new IndexSearcher(reader);

		return searcher;
	}
}
