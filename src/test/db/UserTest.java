package test.db;

import static org.junit.Assert.*;
import org.junit.Test;
import hu.vidyavana.db.api.UserLucene;
import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Encrypt;

public class UserTest
{

	@Test
	public void addUser()
	{
		UserLucene db = UserLucene.inst;
		User u = new User();
		u.email = "any@any.hu";
		u.password = "secret";
		User u2 = null;
		try
		{
			u.setDefaults();
			assertNotEquals(0, u.id);
			assertEquals(User.AdminLevel.None, u.adminLevel);
			assertNotEquals("secret", u.password);
			assertEquals("", u.name);
			assertNotNull(u.accessStr);
			assertNotNull(u.regToken);
			assertNotNull(u.accessStr);
			u2 = db.findUserByEmail(u.email);
			assertNull(u2);
			db.addUser(u);
			u2 = db.findUserByEmail(u.email);
			assertNotNull(u2);
			assertEquals(User.AdminLevel.None, u2.adminLevel);
			assertNotEquals(0, u2.id);
			assertEquals(Encrypt.md5("secret"), u2.password);
			assertEquals("", u2.name);
			assertNotNull(u2.accessStr);
			assertNotNull(u2.regToken);
			assertNotNull(u2.accessStr);
		}
		finally
		{
			db.deleteUser(u.email);
			u2 = db.findUserByEmail(u.email);
			assertNull(u2);
		}
	}

}
