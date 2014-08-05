package hu.vidyavana.convert.epub;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ParsedEpubToBalarama
{
	public static final String infn = "d:\\temp\\2\\Sarartha Darsini - Visvanath Cakravarti Thakur.html.trans";
	public static final String outfn = "d:\\temp\\2\\Sarartha Darsini - Visvanath Cakravarti Thakur.html";
	public Map<String, Integer> cmap = new LinkedHashMap<>();

	
	private void process()
	{
		balaramaFontMap();
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(infn), "8859_1"));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfn));
			while(true)
			{
				String line = in.readLine();
				if(line == null)
					break;
				int pos = 0;
				while(true)
				{
					int ix = line.indexOf("@`#", pos);
					if(ix == -1)
						break;
					write(out, line, pos, ix);
					for(Entry<String, Integer> e : cmap.entrySet())
					{
						int len = e.getKey().length();
						if(line.length() < len)
							continue;
						if(line.substring(ix, ix+len).equals(e.getKey()))
						{
							out.write(e.getValue());
							pos = ix+len;
							break;
						}
					}
				}
				write(out, line, pos, line.length());
				out.write(13);
				out.write(10);
			}
			in.close();
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	private void write(BufferedOutputStream out, String line, int pos, int ix) throws IOException
	{
		for(int i=pos; i<ix; ++i)
			out.write(line.charAt(i));
	}

	private void balaramaFontMap()
	{
		cmap.put("@`#sh@", 231);
		cmap.put("@`#ng@", 236);
		cmap.put("@`#ny@", 239);
		cmap.put("@`#rii@", 232);
		cmap.put("@`#lrii@", 251);
		cmap.put("@`#a", 228);
		cmap.put("@`#i", 233);
		cmap.put("@`#u", 252);
		cmap.put("@`#d", 242);
		cmap.put("@`#h", 249);
		cmap.put("@`#l", 255);
		cmap.put("@`#m", 224);
		cmap.put("@`#n", 235);
		cmap.put("@`#r", 229);
		cmap.put("@`#s", 241);
		cmap.put("@`#t", 246);
		
		cmap.put("@`#SH@", 199);
		cmap.put("@`#NG@", 204);
		cmap.put("@`#NY@", 207);
		cmap.put("@`#RII@", 200);
		cmap.put("@`#LRII@", 223);	// not exists
		cmap.put("@`#A", 196);
		cmap.put("@`#I", 201);
		cmap.put("@`#U", 220);
		cmap.put("@`#D", 210);
		cmap.put("@`#H", 217);
		cmap.put("@`#L", 223);
		cmap.put("@`#M", 192);
		cmap.put("@`#N", 203);
		cmap.put("@`#R", 197);
		cmap.put("@`#S", 209);
		cmap.put("@`#T", 214);
	}


	public static void main(String[] args)
	{
		new ParsedEpubToBalarama().process();
	}

}
