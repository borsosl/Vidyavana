package hu.vidyavana.convert.epub;

import hu.vidyavana.convert.indd.CharacterMap;
import java.io.*;
import java.util.Scanner;

public class RemoveZeroBytes
{
	public static final String infn = "d:\\Temp\\2\\brahma-samhita.utf16.txt";
	public static final String outfn = "d:\\Temp\\2\\brahma-samhita.8bit.txt";

	public static void main(String[] args)
	{
		try
		{
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(infn));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfn));
			CharacterMap utf8Map = new CharacterMap(new File("c:\\wk\\prg\\Java\\vidyavana\\meta\\cmap\\balaramRev\\Balaram.cmap.txt"));
			utf8Map.init();
			Scanner scanner = new Scanner(System.in);
			int c1, c2;
			while(true)
			{
				c1 = in.read();
				if(c1 == -1)
					break;
				c2 = in.read();
				if(c2 == -1)
					break;
				if(c2 == 0)
					out.write(c1);
				else
				{
					int b = c2*256 + c1;
					if(b == 8212)
						out.write(151);
					else if(b == 8211)
						out.write(150);
					else if(b == 8220)
						out.write(147);
					else if(b == 8221)
						out.write(148);
					else if(b == 8216)
						out.write(145);
					else if(b == 8217)
						out.write(146);
					else if(b == 8226)
						out.write(149);
					else if(b == 8230)
						out.write(133);
					else if(b == 8218)
						out.write(130);
					else if(b == 8209)
						out.write(32);
					else if(b == 8249)
						out.write(139);
					else if(b == 8250)
						out.write(155);
					else if(b == 65533)
						out.write(32);
					else if(b == 772 || b == 713)
						out.write(168);
					else if(b == 769)
						out.write('\'');
					else if(b == 803)
						out.write('.');
					else if(b == 729)
						out.write(186);
					else
					{
						Character b2 = utf8Map.get(b);
						if(b2 != null && b > 255)
							out.write(b2.charValue());
						else
						{
							System.out.println(b);
							String s = scanner.next();
							if("X".equals(s))
								break;
							out.write(Integer.valueOf(s));
						}
					}
				}
			}
			in.close();
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
