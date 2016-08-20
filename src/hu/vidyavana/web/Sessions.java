package hu.vidyavana.web;

import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.web.model.SessionRegistryEntry;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;

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
		ArrayList<SessionRegistryEntry> userSessions = Globals.sessionsByUser.get(user.email);
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
		userSessions.add(new SessionRegistryEntry(ses.getId()));
		Log.info("Session added to map: " + ses.getId());
		return true;
	}

	public static void updateUserAccessTime(HttpSession ses, User user)
	{
		processEntry(user.email, ses.getId(), (entry, it) -> entry.accessed = new Date());
	}


	public static void removeUserFromSessionMap(HttpSession ses, User user)
	{
		if(!processEntry(user.email, ses.getId(), (entry, it) -> it.remove()))
			return;
		if(Globals.sessionsByUser.get(user.email).isEmpty())
			Globals.sessionsByUser.remove(user.email);
		Log.info("Session removed from map: " + ses.getId());
	}

	private static boolean processEntry(String email, String sid, BiConsumer<SessionRegistryEntry, Iterator<SessionRegistryEntry>> callback) {
		ArrayList<SessionRegistryEntry> userSessions = Globals.sessionsByUser.get(email);
		if(userSessions == null)
		{
			Log.warning("User is missing from session map", null);
			return false;
		}
		Iterator<SessionRegistryEntry> it;
		for(it = userSessions.iterator(); it.hasNext(); ) {
			SessionRegistryEntry sre = it.next();
			if(sre.sid.equals(sid)) {
				callback.accept(sre, it);
				return true;
			}
		}
		Log.warning("Session is missing from user session list", null);
		return false;
	}

	public static void sweepSessionMap()
	{
		Set<String> emails = Globals.sessionsByUser.keySet();
		for (String email : emails) {
			ArrayList<SessionRegistryEntry> userSessions = Globals.sessionsByUser.get(email);
			if(userSessions != null) {
				Iterator<SessionRegistryEntry> it;
				for(it = userSessions.iterator(); it.hasNext(); ) {
					SessionRegistryEntry sre = it.next();
					if(new Date().getTime() - sre.accessed.getTime()  > 24*60*60*1000)
						it.remove();
				}
				if(userSessions.isEmpty())
					userSessions = null;
			}
			if(userSessions == null)
				Globals.sessionsByUser.remove(email);
		}
	}

}
