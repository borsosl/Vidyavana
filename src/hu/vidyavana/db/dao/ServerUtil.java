package hu.vidyavana.db.dao;

import hu.vidyavana.db.AddBook;
import hu.vidyavana.util.Globals;
import hu.vidyavana.web.RequestInfo;
import java.io.IOException;

public class ServerUtil
{

	public void service(RequestInfo ri)
	{
		if("rebuild".equals(ri.args[1]))
		{
			String user = null;
			if(ri.args.length > 2)
				user = ri.args[2];
			try
			{
				AddBook.rebuildOnServer(user, ri.resp.getWriter());
			}
			catch(IOException ex)
			{
			}
		}
		else if("maint".equals(ri.args[1]))
		{
			boolean val = true;
			if(ri.args.length > 2)
				val = false;
			Globals.maintenance = val;
		}
	}
}
