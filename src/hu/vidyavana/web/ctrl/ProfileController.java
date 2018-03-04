package hu.vidyavana.web.ctrl;

import hu.vidyavana.db.model.Storage;
import hu.vidyavana.service.ProfileService;
import hu.vidyavana.web.RequestInfo;

public class ProfileController
{
	public void service(RequestInfo ri) throws Exception
	{
		ri.ajax = true;
		synchronized(Storage.SYSTEM)
		{
			Storage.SYSTEM.openForRead();
		}
		ProfileService service = new ProfileService(ri);
		String function = ri.args[1];
		if("page".equals(function))
			service.page();
		else if("save".equals(function))
			service.save();
		else if("verify-new-email".equals(ri.args[1]))
			service.verifyEmailChange(ri);
		else if("decline-new-email".equals(ri.args[1]))
			service.declineEmailChange(ri);
		else
			ri.resp.setStatus(404);
	}
}
