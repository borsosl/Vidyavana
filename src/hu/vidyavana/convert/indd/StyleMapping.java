package hu.vidyavana.convert.indd;

import hu.vidyavana.convert.api.Align;
import hu.vidyavana.convert.api.Paragraph;
import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.convert.api.ParagraphStyle;

import java.io.*;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hu.vidyavana.convert.api.ParagraphClass.*;

public class StyleMapping
{
	static Pattern pairs = Pattern.compile("^(.*?)\\t(.*)");

	File destDir, file;
	SpecificBook spec;
	TreeMap<String, ParagraphClass> mapping;
	boolean dirty;


	public StyleMapping(File destDir, SpecificBook spec)
	{
		this.destDir = destDir;
		this.spec = spec;
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
				{
					String orig = m.group(1);
					if(".".equals(orig))
						orig = "";
					mapping.put(orig, ParagraphClass.valueOf(m.group(2)));
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
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
			for(Entry<String, ParagraphClass> e : mapping.entrySet())
			{
				String orig = e.getKey();
				if("".equals(orig))
					orig = ".";
				bw.write(orig+"\t"+e.getValue().toString()+"\r\n");
			}
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


	public void convertStylename(Paragraph para, Scanner scanner)
	{
		String orig = para.style.basedOn;
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
		generalStyleBasedMapping(para);
		if(spec != null)
			spec.run(para);
	}

	
	private void generalStyleBasedMapping(Paragraph para)
	{
		ParagraphStyle ps = para.style;
		if(para.cls != TorzsKoveto)
		{
			// modification of specifically mapped styles
			if((para.cls == in0 || para.cls == Index) && ps.align == Align.Center)
				para.cls = Szakaszcim;
			if(para.cls == Fejezetszam || para.cls == Balra)
			{
				String upper = para.text.toString().toUpperCase();
				if(upper.startsWith("CHAPTER") && upper.length()<30)
				{
					para.text.setLength(0);
					para.text.append(upper);
				}
			}
			return;
		}
		if(ps.size != null)
		{
			if(ps.size > 3000)
			{
				if(para.text.length() > 3)
					para.cls = Konyvcim;
				else
					// iniciale
					para.cls = TorzsKezdet0Bek;
			}
			else if(ps.size > 2200)
			{
				if(para.text.length() < 5)
					para.cls = FejezetszamNagy;
			}
			if(para.cls != TorzsKoveto)
				return;
		}
		if(ps.bold != null && ps.bold)
		{
			if(para.cls != TorzsKoveto)
				return;
		}
		if(ps.italic != null && ps.italic)
		{
			if(para.text.length() > 200)
			{
				if(ps.emptyRowsBefore > 0)
					para.cls = TorzsKezdetDolt;
				else
					para.cls = TorzsKovetoDolt;
			}
			else
			{
				para.cls = TorzsVers;
				if(ps.align != null && ps.align == Align.Center)
				{
					if(ps.emptyRowsBefore > 0)
						para.cls = KozepenDolt;
					else
						para.cls = KozepenKovetoDolt;
				}
			}
			if(para.cls != TorzsKoveto)
				return;
		}
		if(ps.align != null)
		{
			if(ps.align == Align.Center)
			{
				if(para.text.length() < 4)
					para.cls = TorzsVersszam;
				else if(ps.emptyRowsBefore > 0)
					para.cls = Kozepen;
				else
					para.cls = KozepenKoveto;
			}
			else if(ps.align == Align.Right)
				para.cls = Hivatkozas;
			if(para.cls != TorzsKoveto)
				return;
		}
		if(ps.emptyRowsBefore > 0 || para.prev != null && 
			para.prev.cls != TorzsKezdet && para.prev.cls != TorzsKoveto &&
			para.prev.cls != TorzsKezdetDolt && para.prev.cls != TorzsKovetoDolt)
		{
			para.cls = TorzsKezdet;
		}
	}
	
	
	public interface SpecificBook
	{
		void run(Paragraph para);
	}
	
	
	public static class VenuGita implements SpecificBook
	{
		@Override
		public void run(Paragraph para)
		{
		}
	}
}
