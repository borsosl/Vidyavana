package hu.vidyavana.web.ctrl;

import hu.vidyavana.db.model.Storage;
import hu.vidyavana.service.BookmarkService;
import hu.vidyavana.web.RequestInfo;

public class BookmarkController
{
	public void service(RequestInfo ri) throws Exception
	{
		ri.ajax = true;
		synchronized(Storage.SYSTEM)
		{
			Storage.SYSTEM.openForRead();
		}
		BookmarkService service = new BookmarkService(ri);
		String function = ri.args[1];
		if("page".equals(function))
			service.page(null);
		else if("go".equals(function))
			service.gotoBookmark();
		else if("filter".equals(function))
			service.filter();
		else if("save".equals(function))
			service.save();
		else if("delete".equals(function))
			service.delete();
		else
			ri.resp.setStatus(404);
	}
}
