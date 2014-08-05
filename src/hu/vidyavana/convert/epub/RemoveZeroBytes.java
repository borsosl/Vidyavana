package hu.vidyavana.convert.epub;

import java.io.*;

public class RemoveZeroBytes
{
	public static final String infn = "d:\\temp\\2\\Sarartha Darsini - Visvanath Cakravarti Thakur.utf16.txt";
	public static final String outfn = "d:\\temp\\2\\Sarartha Darsini - Visvanath Cakravarti Thakur.txt";

	public static void main(String[] args)
	{
		try
		{
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(infn));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfn));
			int c;
			while((c=in.read())!=-1)
				if(c != 0)
					out.write(c);
			in.close();
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
