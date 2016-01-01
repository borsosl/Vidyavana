package hu.vidyavana.web.ctrl;

import java.util.regex.Pattern;
import hu.vidyavana.db.dao.UserDao;
import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Log;
import hu.vidyavana.web.PanditServlet;
import hu.vidyavana.web.RequestInfo;

public class AuthController
{
	public static final Pattern EMAIL = Pattern.compile("\\S+@\\S+\\.\\S+");
	
	public void service(RequestInfo ri) throws Exception
	{
		ri.ajax = true;
		if("login".equals(ri.args[1]))
			login(ri);
		else if("authenticate".equals(ri.args[1]))
			authenticate(ri);
		else if("register".equals(ri.args[1]))
			register(ri);
		else if("logout".equals(ri.args[1]))
			logout(ri);
		else
			ri.resp.setStatus(404);
	}

	
	public void login(RequestInfo ri) throws Exception
	{
		ri.ajax = false;
		ri.renderJsp("/login.jsp");
	}


	public void authenticate(RequestInfo ri) throws Exception
	{
		String email = ri.req.getParameter("email").trim();
		if(!verifyEmail(email))
		{
			PanditServlet.failResult(ri);
			return;
		}
		User user = UserDao.findUserByEmail(email);
		if(user != null && user.password.equals(ri.req.getParameter("password")))
		{
			user.setAccess();
			ri.ses.setAttribute("user", user);
			Log.activity("Login: "+user.toString());
			PanditServlet.okResult(ri);
		}
		else
			PanditServlet.failResult(ri);
	}


	public void register(RequestInfo ri) throws Exception
	{
		// one at a time
		synchronized(AuthController.class)
		{
			User user = new User();
			user.email = ri.req.getParameter("email").trim();
			user.password = ri.req.getParameter("password");
			user.name = ri.req.getParameter("name");
			user.setDefaults();
	
			if(!verifyEmail(user.email))
			{
				PanditServlet.messageResult(ri, "Helytelen e-mail formátum");
				return;
			}
			if(!user.email.endsWith("@fiktiv.hu"))
			{
				PanditServlet.messageResult(ri, "Egyenlőre csak béta tesztelőknek");
				return;
			}
			if(user.password.length() != 32)
			{
				PanditServlet.messageResult(ri, "Hibás jelszó");
				return;
			}
			try {
				UserDao.insertUser(user);
				user.setAccess();
				ri.ses.setAttribute("user", user);
				PanditServlet.okResult(ri);
			} catch(Exception ex) {
				PanditServlet.messageResult(ri, "Az e-mail cím már regisztrálva van.");
			}
		}
	}


	private boolean verifyEmail(String email)
	{
		return EMAIL.matcher(email).matches();
	}

	
	public void logout(RequestInfo ri) throws Exception
	{
		ri.ses.removeAttribute("user");
		PanditServlet.okResult(ri);
	}
}
