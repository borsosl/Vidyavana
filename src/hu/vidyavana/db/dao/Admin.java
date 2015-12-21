package hu.vidyavana.db.dao;

import java.util.List;
import hu.vidyavana.db.api.UserLucene;
import hu.vidyavana.db.model.User;
import hu.vidyavana.web.MainPage;
import hu.vidyavana.web.RequestInfo;

public class Admin
{
	public void service(RequestInfo ri) throws Exception
	{
//		if(ri.user.adminLevel == User.AdminLevel.None)
//			ri.resp.setStatus(404);
		ri.ajax = true;
		if(ri.args.length == 1)
		{
			ri.admin = true;
			ri.req.setAttribute("_ri", ri);
			new MainPage().service(ri);
		}
		else if("list-users".equals(ri.args[1]))
			listUsers(ri);
		else
			ri.resp.setStatus(404);
	}

	
	private void listUsers(RequestInfo ri)
	{
		try
		{
			List<User> users = UserLucene.inst.getAllUsers();
			ri.renderAjaxTemplate("/admin/list-users.tpl", users);
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
