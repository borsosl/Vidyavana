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
		// use short instead of byte to work around signed byte handling
		short[] line = new short[1000];
		int ptr = 0;
		while(true)
		{
			int c = is.read();
			if(c==10 || c<0)
			{
				while(ptr>0 && line[ptr-1]==32) --ptr;
				if(ptr > 0)
					processLine(line, ptr);
				if(c < 0) break;
				ptr = 0;
				continue;
			}
			if(c==13 || ptr==0 && c==32) continue;
			line[ptr++] = (short) c;
		}
	}
	
	
	private void processLine(short[] line, int length)
	{
		// convert ed encoding to unicode
		StringBuffer sb = new StringBuffer();
		for(int pos=0; pos<length; ++pos)
		{
			int c = line[pos];
			if(line[pos]>=128)
			{
				c = EdCharacter.convert(c);
				if(c != 0)
					sb.append((char) c);
			}
			else
			{
				if(c == '@' && pos==0)
				{
					String tagStr = sequenceToString(line, 1, length, (short) '=').trim().toLowerCase();
					EdTags tag = EdTags.find(tagStr);
				}
				else
					sb.append((char) line[pos]);
			}
		}
	}


	private String sequenceToString(short[] line, int pos, int length, short c)
	{
		StringBuilder sb = new StringBuilder();
		while(pos < length && line[pos] != c)
			sb.append((char) line[pos++]);
		return sb.toString();
	}


	public static void main(String[] args) throws Exception
	{
		new EdFileProcessor().process(new File("c:\\wk2\\Sastra\\BBT\\Text\\BG\\HUBG01XT.H23"), null);
	}
}
