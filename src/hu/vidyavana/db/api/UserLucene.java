package hu.vidyavana.db.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import hu.vidyavana.db.model.User;
import hu.vidyavana.search.api.Lucene;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;

public class UserLucene extends Lucene
{
	public static final UserLucene inst = new UserLucene();

	public UserLucene()
	{
		super(null);
		try {
			closeWriter();
			reader();
		} catch(Exception ex) {
			try
			{
				writer().addDocument(new Document());
			}
			catch(IOException ex1)
			{
				Log.error("Cannot add first document", ex1);
			}
		}
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
			return new User().fromDoc(doc, false);
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


	public List<User> getAllUsers()
	{
		try
		{
			Query q = new WildcardQuery(new Term("email", "*"));
			IndexSearcher sr = searcher();
			TopDocs res = sr.search(q, Integer.MAX_VALUE);
			List<User> users = new ArrayList<>(res.totalHits);
			for(ScoreDoc sd : res.scoreDocs)
			{
				User u = new User();
				u.fromDoc(sr.doc(sd.doc), true);
				users.add(u);
			}
			return users;
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
