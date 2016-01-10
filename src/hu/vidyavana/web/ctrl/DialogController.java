package hu.vidyavana.web.ctrl;

import hu.vidyavana.web.RequestInfo;

public class DialogController
{
	public void service(RequestInfo ri) throws Exception
	{
		ri.ajax = true;
		if("html".equals(ri.args[1]))
			htmlOnly(ri);
		else
			ri.resp.setStatus(404);
	}


	private void htmlOnly(RequestInfo ri)
	{
		try
		{
			String path = "/dialog/" + ri.args[2] + ".html";
			ri.renderAjaxTemplate(path, null);
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
