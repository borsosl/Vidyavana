package hu.vidyavana.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class Globals
{
	public static File cwd;
	public static boolean serverEnv;
	public static boolean localEnv;
	public static boolean androidEnv;
	public static boolean maintenance;
	public static String downtime;
	public static ExecutorService searchExecutors;
	public static ExecutorService mailExecutor;
	public static int concurrentSessions;
	public static HashMap<String, ArrayList<String>> sessionsByUser;
}
