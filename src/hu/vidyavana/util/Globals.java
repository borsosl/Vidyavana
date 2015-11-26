package hu.vidyavana.util;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import hu.vidyavana.search.model.Search;

public class Globals
{
	public static File cwd;
	public static boolean serverEnv;
	public static boolean localEnv;
	public static boolean androidEnv;
	public static boolean maintenance;
	public static Map<Integer, Search> search = new HashMap<>();
	public static LinkedList<Search> activeSearch = new LinkedList<>();
	public static LinkedList<Search> inactiveSearch = new LinkedList<>();
	public static ExecutorService searchExecutors;
}
