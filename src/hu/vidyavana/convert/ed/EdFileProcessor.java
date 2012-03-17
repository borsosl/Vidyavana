package hu.vidyavana.convert.ed;

import java.io.*;

public class EdFileProcessor
{
	public void process(File ed, File xml) throws Exception
	{
		readEdFile(ed);
	}


	private void readEdFile(File ed) throws Exception
	{
		InputStream is = new BufferedInputStream(new FileInputStream(ed));
		byte[] sor = new byte[1000];
		int ptr = 0;
		while(true)
		{
			int c = is.read();
			if(c==10 || c<0)
			{
				while(ptr>0 && sor[ptr-1]==32) --ptr;
				if(ptr > 0)
					processLine(sor, ptr);
				if(c < 0) break;
				ptr = 0;
				continue;
			}
			if(c==13 || ptr==0 && c==32) continue;
			sor[ptr++] = (byte) c;
		}
	}
	
	
	private void processLine(byte[] sor, int length)
	{
	}


	public static void main(String[] args) throws Exception
	{
		new EdFileProcessor().process(new File("c:\\wk2\\Sastra\\BBT\\Text\\BG\\HUBG01XT.H23"), null);
	}
}
