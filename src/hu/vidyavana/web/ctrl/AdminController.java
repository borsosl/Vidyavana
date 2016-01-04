package hu.vidyavana.web.ctrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import hu.vidyavana.db.dao.UserDao;
import hu.vidyavana.db.model.BookPackage;
import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Globals;
import hu.vidyavana.web.MainPage;
import hu.vidyavana.web.PanditServlet;
import hu.vidyavana.web.RequestInfo;

public class AdminController
{
	public void service(RequestInfo ri) throws Exception
	{
		if(ri.user.adminLevel == User.AdminLevel.None)
		{
			ri.resp.setStatus(404);
			return;
		}
		ri.ajax = true;
		if(ri.args.length == 1)
		{
			ri.admin = true;
			ri.req.setAttribute("_ri", ri);
			new MainPage().service(ri);
		}
		else if("list-users".equals(ri.args[1]))
			listUsers(ri);
		else if("modify-user".equals(ri.args[1]))
			modifyUser(ri);
		else if("delete-user".equals(ri.args[1]))
			deleteUser(ri);
		else if("init-access".equals(ri.args[1]))
			initAccess(ri);
		else if("save-access".equals(ri.args[1]))
			saveAccess(ri);
		else if("remove-sessions".equals(ri.args[1]))
			removeSessions(ri);
		else
			ri.resp.setStatus(404);
	}


	private void listUsers(RequestInfo ri)
	{
		try
		{
			List<User> users = UserDao.getAllUsers(true);
			HashMap<String, Object> res = PanditServlet.ajaxMap();
			res.put("users", users);
			res.put("books", BookPackage.serializeAll());
			ri.ajaxResult = res;
			ri.renderAjaxTemplate("/admin/list-users.html", res);
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	
	private void modifyUser(RequestInfo ri)
	{
		if(ri.user.adminLevel != User.AdminLevel.Full)
		{
			PanditServlet.failResult(ri);
			return;
		}
		User user = UserDao.findUserByEmail(ri.req.getParameter("email"));
		user.name = ri.req.getParameter("name");
		user.adminLevel = User.AdminLevel.valueOf(ri.req.getParameter("admin"));
		UserDao.updateUser(user);
		PanditServlet.okResult(ri);
	}

	
	private void deleteUser(RequestInfo ri)
	{
		if(ri.user.adminLevel != User.AdminLevel.Full)
		{
			PanditServlet.failResult(ri);
			return;
		}
		UserDao.deleteUser(ri.req.getParameter("email"));
		PanditServlet.okResult(ri);
	}

	
	private void initAccess(RequestInfo ri)
	{
		User user = UserDao.findUserByEmail(ri.req.getParameter("email"));
		ri.ajaxText = "\"" + user.accessStr + "\"";
	}

	
	private void saveAccess(RequestInfo ri)
	{
		User user = UserDao.findUserByEmail(ri.req.getParameter("email"));
		user.accessStr = ri.req.getParameter("access");
		UserDao.updateUser(user);
		PanditServlet.okResult(ri);
	}

	
	private void removeSessions(RequestInfo ri)
	{
		ArrayList<String> userSessions = Globals.sessionsByUser.get(ri.args[2]);
		for(int i=userSessions.size()-1; i>=0; --i)
			userSessions.remove(i);
		PanditServlet.okResult(ri);
	}
}
