package hu.vidyavana.web.ctrl;

import hu.vidyavana.db.dao.ForgottenPasswordDao;
import hu.vidyavana.db.dao.UserDao;
import hu.vidyavana.db.model.ForgottenPassword;
import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Encrypt;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.util.MailTask;
import hu.vidyavana.web.PanditServlet;
import hu.vidyavana.web.RequestInfo;
import hu.vidyavana.web.Sessions;

import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class AuthController
{
	public static final Pattern EMAIL = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
	
	public void service(RequestInfo ri) throws Exception
	{
		ri.ajax = true;
		if("login".equals(ri.args[1]))
			login(ri);
		else if("authenticate".equals(ri.args[1]))
			authenticate(ri);
		else if("register".equals(ri.args[1]))
			register(ri);
		else if("verify".equals(ri.args[1]))
			verify(ri);
		else if("decline".equals(ri.args[1]))
			decline(ri);
		else if("logout".equals(ri.args[1]))
			logout(ri);
		else if("forgotten".equals(ri.args[1]))
			forgotten(ri);
		else if("accept-new-password".equals(ri.args[1]))
			acceptNewPassword(ri);
		else
			ri.resp.setStatus(404);
	}

	
	public void login(RequestInfo ri) throws Exception
	{
		ri.ajax = false;
		ri.renderJsp("/login.jsp");
	}


	public void authenticate(RequestInfo ri)
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
			if(!user.regToken.isEmpty())
				PanditServlet.messageResult(ri, "Az e-mail cím megerősítése nem történt meg. A regisztrációkor küldött levélben van a link.");
			else
				setUserInSession(ri, user);
		}
		else
			PanditServlet.failResult(ri);
	}


	public void register(RequestInfo ri)
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
			if(user.password.length() != 32)
			{
				PanditServlet.messageResult(ri, "Hibás jelszó");
				return;
			}
			try {
				UserDao.insertUser(user);
				setUserInSession(ri, user);
				ri.ses.setAttribute("justRegistered", true);
				if(Globals.serverEnv)
					Globals.mailExecutor.submit(new MailTask("register", user.email, user.regToken));
			} catch(Exception ex) {
				PanditServlet.messageResult(ri, "Az e-mail cím már regisztrálva van.");
			}
		}
	}


	public void verify(RequestInfo ri) throws Exception
	{
		User user = verifyOrDecline(ri);
		if(user == null)
			return;
		user.regToken = "";
		UserDao.updateUser(user);
		ri.resp.getWriter().write("Az e-mail cím megerősítése sikeres volt.");
	}


	public void decline(RequestInfo ri) throws Exception
	{
		User user = verifyOrDecline(ri);
		if(user == null)
			return;
		UserDao.deleteUser(user.email);
		ri.resp.getWriter().write("A regisztrációt töröltük.");
	}


	protected User verifyOrDecline(RequestInfo ri) throws IOException
	{
		String email = ri.req.getParameter("email").trim();
		String token = ri.req.getParameter("token").trim();
		if(!verifyEmail(email))
		{
			ri.resp.setStatus(403);
			return null;
		}
		User user = UserDao.findUserByEmail(email);
		if(user == null || !user.regToken.isEmpty() && !user.regToken.equals(token))
		{
			ri.resp.setStatus(403);
			return null;
		}
		ri.ajax = false;
		if(user.regToken.isEmpty())
		{
			ri.resp.getWriter().write("A link (már) nem érvényes.");
			return null;
		}
		return user;
	}


	protected void setUserInSession(RequestInfo ri, User user)
	{
		if(Sessions.addUserToSessionMap(ri.ses, user))
		{
			user.setAccess();
			ri.ses.setAttribute("user", user);
			Log.activity("Login: "+user.toString());
			PanditServlet.okResult(ri);
		}
		else
			PanditServlet.messageResult(ri, "Egyszerre csak "+Globals.concurrentSessions+
				" eszközön / böngészőben használhatod az alkalmazást. A Kilép menüponttal"+
				" kell kilépned a másikból, vagy várj max. fél órát.");
	}


	private boolean verifyEmail(String email)
	{
		return EMAIL.matcher(email).matches();
	}

	
	public void logout(RequestInfo ri)
	{
		User user = (User) ri.ses.getAttribute("user");
		if(user == null)
		{
			ri.ajaxText = "{\"error\": \"expired\"}";
			return;
		}
		Enumeration<String> attributeNames = ri.ses.getAttributeNames();
		while(attributeNames.hasMoreElements()) {
			String name = attributeNames.nextElement();
			ri.ses.removeAttribute(name);
		}
		Sessions.removeUserFromSessionMap(ri.ses, user);
		PanditServlet.okResult(ri);
	}

	private void forgotten(RequestInfo ri)
	{
		String email = ri.req.getParameter("email").trim();
		if(!verifyEmail(email))
		{
			PanditServlet.messageResult(ri, "Helytelen e-mail formátum.");
			return;
		}
		User user = UserDao.findUserByEmail(email);
		if(user != null)
		{
			StringBuilder password = new StringBuilder();
			for(int i=0; i<8; ++i)
				password.append(generatePasswordChar());
			ForgottenPassword fp = new ForgottenPassword();
			fp.email = email;
			fp.password = Encrypt.md5(password.toString());
			ForgottenPasswordDao.delete(email);
			ForgottenPasswordDao.insert(fp);
			if(Globals.serverEnv)
				Globals.mailExecutor.submit(new MailTask("forgotten", user.email, password.toString()));
			PanditServlet.okResult(ri);
		}
		else
			PanditServlet.messageResult(ri, "Ismeretlen e-mail cím.");
	}

	private void acceptNewPassword(RequestInfo ri) throws IOException {
		String email = ri.req.getParameter("email").trim();
		if(!verifyEmail(email))
		{
			ri.resp.setStatus(403);
			return;
		}
		User user = UserDao.findUserByEmail(email);
		ForgottenPassword fp = ForgottenPasswordDao.find(email);
		if(user == null || fp == null)
		{
			ri.resp.setStatus(403);
			return;
		}
		user.password = fp.password;
		UserDao.updateUser(user);
		ForgottenPasswordDao.delete(email);
		ri.ajax = false;
		ri.resp.getWriter().write("A jelszó változtatása sikeres volt.");
	}

	private char generatePasswordChar() {
		int c = Double.valueOf(Math.random() * 62.0).intValue();
		if(c<10) c+=48;
		else if(c<36) c+=55;
		else c+=61;
		return (char) c;
	}
}
