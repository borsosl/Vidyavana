package hu.vidyavana.web;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import hu.vidyavana.db.model.Storage;
import hu.vidyavana.util.Globals;

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
			// TODO check for android path
			runtimeDir = new File(path, "runtime");
			Globals.serverEnv = true;
		}
		else
		{
			runtimeDir = runtimeDir.getAbsoluteFile();
			Globals.localEnv = true;
		}
		Globals.cwd = runtimeDir.getAbsoluteFile().toPath().normalize().toFile();
		System.out.println("Working in " + runtimeDir.getAbsolutePath());
		Storage store = Storage.SYSTEM;
		store.setEncrypted(false);
		try
		{
			store.openForRead();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		Globals.searchExecutors = Executors.newCachedThreadPool();
	}

	
	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		try
		{
			// TODO close cached user storages
			Storage.SYSTEM.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
