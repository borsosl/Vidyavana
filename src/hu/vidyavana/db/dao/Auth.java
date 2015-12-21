package hu.vidyavana.db.dao;

import java.util.regex.Pattern;
import hu.vidyavana.db.api.UserLucene;
import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Log;
import hu.vidyavana.web.RequestInfo;

public class Auth
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
			ri.ajaxText = "{\"fail\": true}";
			return;
		}
		User user = UserLucene.inst.findUserByEmail(email);
		if(user != null && user.password.equals(ri.req.getParameter("password")))
		{
			ri.ses.setAttribute("user", user);
			Log.activity("Login: "+user.toString());
			ri.ajaxText = "{\"ok\": true}";
		}
		else
			ri.ajaxText = "{\"fail\": true}";
	}


	public void register(RequestInfo ri) throws Exception
	{
		User user = new User();
		user.email = ri.req.getParameter("email").trim();
		user.password = ri.req.getParameter("password");
		user.name = ri.req.getParameter("name");
		user.setDefaults();

		if(!verifyEmail(user.email))
		{
			ri.ajaxText = "{\"message\": \"Helytelen e-mail formátum\"}";
			return;
		}
		if(!user.email.endsWith("@fiktiv.hu"))
		{
			ri.ajaxText = "{\"message\": \"Egyenlőre csak béta tesztelőknek.\"}";
			return;
		}
		if(user.password.length() != 32)
		{
			ri.ajaxText = "{\"message\": \"Hibás jelszó\"}";
			return;
		}
		if(UserLucene.inst.addUser(user))
		{
			ri.ses.setAttribute("user", user);
			ri.ajaxText = "{\"ok\": true}";
		}
		else
			ri.ajaxText = "{\"message\": \"Az e-mail cím már regisztrálva van.\"}";
	}


	private boolean verifyEmail(String email)
	{
		return EMAIL.matcher(email).matches();
	}

	
	public void logout(RequestInfo ri) throws Exception
	{
		ri.ses.removeAttribute("user");
		ri.ajaxText = "{\"ok\": true}";
	}
}
