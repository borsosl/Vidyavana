package test.util;

import hu.vidyavana.util.Log;
import java.util.logging.Level;

public class LoggerTest
{
	public static void main(String[] args)
	{
		Log.level(Level.INFO);
		Log.error("error", new RuntimeException("test ex"));
		Log.warning("warn", null);
		Log.info("info");
	}
}
