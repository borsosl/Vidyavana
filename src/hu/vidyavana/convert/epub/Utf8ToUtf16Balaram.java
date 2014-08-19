package hu.vidyavana.convert.epub;

import hu.vidyavana.convert.indd.CharacterMap;
import hu.vidyavana.convert.indd.CharacterMapManager;
import java.io.*;

public class Utf8ToUtf16Balaram
{
	public static final String infn = "c:\\backup\\srsbooks\\Govinda-lilamrta unicode.txt";
	public static final String outfn = "c:\\backup\\srsbooks\\Govinda-lilamrta unicode.utf16.txt";
	CharacterMapManager mgr;
	CharacterMap cmap;

	
	private void process()
	{
		try
		{
			mgr = new CharacterMapManager();
			mgr.init("c:\\wk\\prg\\Java\\vidyavana\\meta\\cmap\\balaramRev");
			mgr.selectFont("Balaram");
			cmap = mgr.map();
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(infn), "utf-8"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfn), "utf-16"));
			while(true)
			{
				int c = in.read();
				if(c == -1)
					break;
				if(c > 127)
				{
					Character c1 = cmap.get(c);
					if(c1 == null)
					{
						System.out.println("Missing mapping for unicode "+c);
						break;
					}
					out.write(c1);
					continue;
				}
				out.write(c);
			}
			in.close();
			out.flush();
			out.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	
	public static void main(String[] args)
	{
		new Utf8ToUtf16Balaram().process();
	}

}
