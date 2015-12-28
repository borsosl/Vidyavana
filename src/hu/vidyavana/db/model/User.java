package hu.vidyavana.db.model;

import java.io.Serializable;
import java.util.Date;
import hu.vidyavana.search.model.BookAccess;
import hu.vidyavana.util.Encrypt;

public class User implements Serializable
{
	public static enum AdminLevel
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
	public BookAccess access;
	
	
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

	
	@Override
	public String toString()
	{
		if(name == null || name.trim().isEmpty())
			return email;
		return name + " (" + email + ")";
	}
}
