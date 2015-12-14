package hu.vidyavana.db.api;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import hu.vidyavana.db.model.User;
import hu.vidyavana.search.api.Lucene;
import hu.vidyavana.util.Globals;

public class UserLucene extends Lucene
{
	public static final UserLucene inst = new UserLucene();

	public UserLucene()
	{
		super(null);
	}

	
	@Override
	protected void setupDirectory(String user)
	{
		dir = new File(Globals.cwd, "db/user");
	}

	
	@Override
	protected IndexWriterConfig createConfig()
	{
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		return config;
	}

	public synchronized boolean addUser(User user)
	{
		if(findUserByEmail(user.email) != null)
			return false;
		try
		{
			Document doc = user.toDoc(new Document());
			writer().addDocument(doc);
			writer.commit();
			return true;
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Adding user: "+user.email, ex);
		}
	}


	public synchronized User findUserByEmail(String email)
	{
		try
		{
			closeWriter();
			Query q = new TermQuery(new Term("email", email));
			TopDocs res = searcher().search(q, 1);
			if(res.totalHits != 1)
				return null;
			Document doc = searcher().doc(res.scoreDocs[0].doc);
			return new User().fromDoc(doc);
		}
		catch(IOException ex)
		{
			return null;
		}
	}


	public void deleteUser(String email)
	{
		try
		{
			Query q = new TermQuery(new Term("email", email));
			writer().deleteDocuments(q);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
