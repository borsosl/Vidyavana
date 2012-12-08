package hu.vidyavana.util;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.*;

public class Log
{
	private static Logger logger;
	private static FileHandler handler;
	
	static
	{
		try
		{
			URL url = Log.class.getResource("/hu/resource/logging.properties");
			LogManager.getLogManager().readConfiguration(url.openStream());
			
			logger = Logger.getLogger("common");
			logger.setLevel(Level.ALL);
			
			handler = new FileHandler("vidyavana.log");
			handler.setFormatter(new SimplerFormatter());
			logger.addHandler(handler);
		}
		catch(SecurityException | IOException ex)
		{
			System.out.println("Failed to initialize logging.");
		}
	}
	
	
	public static void level(Level level)
	{
		logger.setLevel(level);
	}
	
	
	public static void error(String text, Throwable t)
	{
		if(text != null)
			logger.severe(text);
		if(t != null)
			logger.severe(stackTrace(t));
	}
	
	
	public static void warning(String text, Throwable t)
	{
		if(text != null)
			logger.warning(text);
		if(t != null)
			logger.warning(stackTrace(t));
	}
	
	
	public static void info(String text)
	{
		logger.info(text);
	}
	
	
	private static String stackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter(2000);
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
}


class SimplerFormatter extends Formatter
{
	private static final MessageFormat messageFormat = new MessageFormat("[{0,date,MM-dd HH:mm:ss}]: {1}");


	@Override
	public String format(LogRecord record)
	{
		Object[] arguments = new Object[2];
		arguments[0] = new Date(record.getMillis());
		arguments[1] = record.getMessage();
		return messageFormat.format(arguments);
	}

}
