package hu.vidyavana.convert.indd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterMap
{
	static Pattern codes = Pattern.compile("^(\\d+)\\t(\\d+)");

	File cmapFile;
	ArrayList<String> fonts;
	TreeMap<Integer, Character> mapping;
	boolean dirty;

	public CharacterMap(File cmapFile)
	{
		this.cmapFile = cmapFile;
	}

	public boolean init()
	{
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(cmapFile), "UTF8")))
		{
			fonts = new ArrayList<>();
			mapping = new TreeMap<>();
			while(true)
			{
				String ln = br.readLine();
				if(ln == null)
					break;
				ln = ln.trim();
				if(ln.startsWith("*"))
				{
					String fontName = ln.substring(1).trim();
					fonts.add(fontName);
					continue;
				}
				Matcher m = codes.matcher(ln);
				if(m.find())
				{
					Integer key = Integer.valueOf(m.group(1));
					char value = (char) Integer.valueOf(m.group(2)).intValue();
					mapping.put(key, value);
				}
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
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cmapFile), "UTF8"));
			for(String font : fonts)
				bw.write("* "+font+"\r\n");
			for(Entry<Integer, Character> e : mapping.entrySet())
			{
				char k = (char) e.getKey().intValue();
				bw.write(""+e.getKey()+"\t"+((int) e.getValue().charValue())+"\t"+k+"\t"+e.getValue()+"\r\n");
			}
			bw.close();
		}
	}
	
	
	public Character get(int code)
	{
		return mapping.get(code);
	}
	
	
	public void put(int code, char uni)
	{
		mapping.put(code, uni);
		dirty = true;
	}
}
