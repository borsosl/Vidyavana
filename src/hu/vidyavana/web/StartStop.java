package hu.vidyavana.web;

import hu.vidyavana.db.model.StorageRoot;
import hu.vidyavana.util.Globals;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
			runtimeDir.mkdirs();
			Globals.serverEnv = true;
		}
		else
			Globals.localEnv = true;
		Globals.cwd = runtimeDir;
		System.out.println("Working in " + runtimeDir.getAbsolutePath());
		StorageRoot sr = StorageRoot.SYSTEM;
		sr.setEncrypted(false);
		try
		{
			sr.openForRead();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	
	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		try
		{
			StorageRoot.SYSTEM.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
