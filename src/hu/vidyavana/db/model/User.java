package hu.vidyavana.db.model;

import hu.vidyavana.util.Encrypt;

import java.io.Serializable;
import java.time.*;
import java.util.Date;

public class User implements Serializable
{
	public enum AdminLevel
	{
		// never rename old ones, serialized by name
		None,
		Full,
		BookRights
	}
	
	public int id;
	public AdminLevel adminLevel;
	public String email;
	public String password;
	public String name;
	public String regToken;
	public String accessStr;
	public long regDateLong;
	public long lastLoginLong;

	public BookAccess access;
	public ZonedDateTime regDate;
	public ZonedDateTime lastLogin;
	
	
	public void setDefaults()
	{
		adminLevel = AdminLevel.None;
		if(name == null)
			name = "";
		else
			name = name.trim();
		regToken = Encrypt.md5(""+(new Date().getTime()));
		accessStr = "1";
	}
	
	
	public void setAccess()
	{
		access = BookAccess.parseAccessStr(accessStr, null);
	}


	public void setDates()
	{
        ZoneId tz = ZoneId.of("CET");
        regDate = Instant.ofEpochMilli(regDateLong).atZone(tz);
		lastLogin = Instant.ofEpochMilli(lastLoginLong).atZone(tz);
	}

	
	@Override
	public String toString()
	{
		if(name == null || name.trim().isEmpty())
			return email;
		return name + " (" + email + ")";
	}
}
