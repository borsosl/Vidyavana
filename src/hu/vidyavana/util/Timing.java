package hu.vidyavana.util;

import java.util.Stack;

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
		long t = stop();
		String diff = Double.toString(t / 1000000000.0);
		diff = diff.substring(0, diff.indexOf('.')+4);
		System.out.println(note + ": " + diff + " sec.");
	}
}
