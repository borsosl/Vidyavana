package hu.vidyavana.util;

import java.util.Stack;
import java.util.logging.Logger;

public class Timing
{
	private static Stack<Long> start = new Stack<Long>();


	public static void start()
	{
		start.push(System.nanoTime());
	}


	public static long stop()
	{
		return System.nanoTime() - start.pop();
	}


	public static void stop(String note)
	{
		stop(note, null);
	}


	public static void stop(String note, Logger log)
	{
		long t = stop();
		String diff = Double.toString(t / 1000000000.0);
		diff = diff.substring(0, diff.indexOf('.')+4);
		String out = note + " time " + diff + " sec.";
		if(log != null)
			log.finest(out + System.lineSeparator());
		else
			System.out.println(out);
	}
}
