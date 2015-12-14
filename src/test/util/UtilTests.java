package test.util;

import static org.junit.Assert.*;
import org.junit.Test;
import hu.vidyavana.util.Encrypt;

public class UtilTests
{
	@Test
	public void md5()
	{
		assertEquals("6c90aa3760658846a86a263a4e92630e", Encrypt.md5("teszt"));
	}
}
