package hu.vidyavana.web;

import java.io.File;
import java.util.concurrent.Executors;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import hu.vidyavana.db.dao.TocTree;
import hu.vidyavana.db.model.Storage;
import hu.vidyavana.util.Encrypt;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;

public class StartStop implements ServletContextListener
{
	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		ServletContext ctx = sce.getServletContext();
		String path = ctx.getRealPath("/");
		File runtimeDir = new File(path, "../../runtime");
		if(!runtimeDir.exists())
		{
			runtimeDir = new File(path, "WEB-INF/runtime");
			Globals.serverEnv = true;
		}
		else
			Globals.localEnv = true;
		Globals.cwd = runtimeDir.getAbsoluteFile();

		Log.info("Pandit context initializing");
		Log.info("Working in " + runtimeDir.getAbsolutePath());
		long t0 = System.currentTimeMillis();
		try
		{
			Storage.SYSTEM.setEncrypted(false);
			TocTree.inst.readFromFile();
		}
		catch(Exception ex)
		{
			Log.error("Opening SYSTEM store or reading TOC", ex);
		}
		Encrypt.getInstance().init();
		Globals.searchExecutors = Executors.newCachedThreadPool();
		Log.info("Pandit context initialized in "+(System.currentTimeMillis() - t0));
	}

	
	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		try
		{
			// TODO close cached user storages
			Storage.SYSTEM.close();
		}
		catch(Exception ex)
		{
			Log.error("Closing SYSTEM store", ex);
		}
		Log.info("Pandit context destroyed");
		Log.close();
	}
}
