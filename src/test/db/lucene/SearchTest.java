package test.db.lucene;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import hu.vidyavana.search.model.Search;
import hu.vidyavana.search.task.SearchTask;
import hu.vidyavana.util.Globals;

public class SearchTest
{
	@BeforeClass
	public static void setup()
	{
		Globals.cwd = new File(".");
		Globals.searchExecutors = Executors.newSingleThreadExecutor();
	}

	
	@Test
	public void testSimpleWords()
	{
		Search d = new Search();
		d.user = "lnd";
		d.queryStr = "pandu fiai";
		d.reqHits = 10;
		runQuery(d);
		Assert.assertTrue(d.hits != null);
	}

	
	void runQuery(Search d)
	{
		Future<?> res = Globals.searchExecutors.submit(new SearchTask(d));
		try
		{
			res.get();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
