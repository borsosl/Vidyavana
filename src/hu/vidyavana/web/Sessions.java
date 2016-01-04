package hu.vidyavana.web;

import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;

public class Sessions implements HttpSessionListener
{
	@Override
	public void sessionCreated(HttpSessionEvent event)
	{
	}


	@Override
	public void sessionDestroyed(HttpSessionEvent event)
	{
		HttpSession ses = event.getSession();
		Log.info("Removing session "+ses.getId());
		if(Globals.sessionsByUser == null)
		{
			Log.warning("Session map is null, session may be stuck if it's read later", null);
			return;
		}
		User user = (User) ses.getAttribute("user");
		if(user == null)
			// already logged out or never logged in
			return;
		removeUserFromSessionMap(ses, user);
	}


	public static boolean addUserToSessionMap(HttpSession ses, User user)
	{
		ArrayList<String> userSessions = Globals.sessionsByUser.get(user.email);
		if(userSessions == null)
		{
			userSessions = new ArrayList<>();
			Globals.sessionsByUser.put(user.email, userSessions);
		}
		if(userSessions.size() >= Globals.concurrentSessions)
		{
			Log.info("Sessions that prevented a login: " + userSessions);
			return false;
		}
		userSessions.add(ses.getId());
		Log.info("Session added to map: " + ses.getId());
		return true;
	}


	public static void removeUserFromSessionMap(HttpSession ses, User user)
	{
		ArrayList<String> userSessions = Globals.sessionsByUser.get(user.email);
		if(userSessions == null)
		{
			Log.warning("User is missing from session map", null);
			return;
		}
		userSessions.remove(ses.getId());
		Log.info("Session removed from map: " + ses.getId());
	}

}
