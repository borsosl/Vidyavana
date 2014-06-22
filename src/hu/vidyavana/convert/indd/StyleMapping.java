package hu.vidyavana.convert.indd;

import hu.vidyavana.convert.api.Paragraph;
import hu.vidyavana.convert.api.ParagraphClass;
import java.io.*;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleMapping
{
	static Pattern pairs = Pattern.compile("^(.*?)\\t(.*)");

	File destDir, file;
	TreeMap<String, ParagraphClass> mapping;
	boolean dirty;

	public StyleMapping(File destDir)
	{
		this.destDir = destDir;
		file = new File(destDir, "styleMapping.txt");
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
					mapping.put(m.group(1), ParagraphClass.valueOf(m.group(2)));
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
			for(Entry<String, ParagraphClass> e : mapping.entrySet())
				bw.write(e.getKey()+"\t"+e.getValue().toString()+"\r\n");
			bw.close();
		}
	}
	
	
	public ParagraphClass get(String key)
	{
		return mapping.get(key);
	}
	
	
	public void put(String key, ParagraphClass value)
	{
		mapping.put(key, value);
		dirty = true;
	}


	public void convertStylename(String orig, Paragraph para, Scanner scanner)
	{
		ParagraphClass cls = get(orig);
		if(cls == null)
		{
			System.out.println("Stílus mapping: "+orig);
			do
			{
				String s = scanner.next();
				try
				{
					cls = ParagraphClass.valueOf(s);
				}
				catch(Exception ex)
				{
				}
			} while(cls == null);
			put(orig, cls);
		}
		para.cls = cls;
	}
}
