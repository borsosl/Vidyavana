package hu.vidyavana.convert.indd;

import java.io.*;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReverseHyphenate
{
	static Pattern pairs = Pattern.compile("^(.*?)\\t(.*)");
	static Pattern hyphen = Pattern.compile("[-÷]");
	static Pattern multiHyphens = Pattern.compile("[-÷]{2,}");

	File destDir, file;
	TreeMap<String, String> mapping;
	boolean dirty;

	public ReverseHyphenate(File destDir)
	{
		this.destDir = destDir;
		file = new File(destDir, "hyphen.txt");
	}

	public boolean init()
	{
		mapping = new TreeMap<>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8")))
		{
			while(true)
			{
				String ln = br.readLine();
				if(ln == null)
					break;
				ln = ln.trim();
				Matcher m = pairs.matcher(ln);
				if(m.find())
					mapping.put(m.group(1), m.group(2));
			}
		}
		catch(IOException e)
		{
			return false;
		}
		return true;
	}
	
	
	public void close() throws IOException
	{
		if(dirty)
		{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
			for(Entry<String, String> e : mapping.entrySet())
				bw.write(e.getKey()+"\t"+e.getValue()+"\r\n");
			bw.close();
		}
	}
	
	
	public String get(String key)
	{
		return mapping.get(key);
	}
	
	
	public void put(String key, String value)
	{
		mapping.put(key, value);
		dirty = true;
	}


	public void check(StringBuilder wordBuffer, StringBuilder paraBuffer, Scanner scanner)
	{
		boolean optHyph = false;
		int ix = wordBuffer.indexOf("÷");
		if(ix > 0)
			optHyph = true;
		else
			ix = wordBuffer.indexOf("-");
		if(ix > 0)
		{
			int len = wordBuffer.length();
			if(ix == len-1
				|| ix == len-2 && wordBuffer.charAt(wordBuffer.length()-1)=='e')
				return;
			String orig = wordBuffer.toString();
			String simpler = multiHyphens.matcher(orig).replaceAll("÷");
			String mod = get(simpler);
			if(mod == null)
			{
				if(optHyph)
					System.out.print("Opcionális ");
				System.out.println("Elválasztás: "+simpler);
				String s = scanner.next();
				if(".".equals(s))
				{
					mod = hyphen.matcher(simpler).replaceAll("");
					put(simpler, mod);
				}
				else if("X".equals(s))
					throw new RuntimeException("Hyphenate input aborted.");
				else if(s.length()>1)
				{
					mod = s;
					put(simpler, mod);
				}
				else
					put(simpler, simpler);
			}
			if(mod != null)
			{
				try
				{
					paraBuffer.setLength(paraBuffer.length()-orig.length());
				}
				catch(Exception ex)
				{
					System.out.println("Buffer error: "+paraBuffer.length());
				}
				paraBuffer.append(mod);
			}
		}
	}
}
