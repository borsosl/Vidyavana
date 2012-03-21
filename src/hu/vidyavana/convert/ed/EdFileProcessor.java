package hu.vidyavana.convert.ed;

import hu.vidyavana.convert.api.Book;
import hu.vidyavana.convert.api.Chapter;
import hu.vidyavana.convert.api.Paragraph;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class EdFileProcessor
{
	private Book book;
	private Chapter chapter;
	private Paragraph para;
	private int nextPos;


	public void process(File ed, File xml) throws Exception
	{
		book = new Book();
		chapter = new Chapter();
		book.chapter.add(chapter);
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
			if(c=='\n' || c<0)
			{
				while(ptr>0 && line[ptr-1]==' ') --ptr;
				if(ptr > 0)
					processLine(line, ptr);
				if(c < 0) break;
				ptr = 0;
				continue;
			}
			if(c=='\r' || ptr==0 && (c==' ' || c=='\t')) continue;
			line[ptr++] = (short) c;
		}
	}
	
	
	private void processLine(short[] line, int length)
	{
		// empty line
		if(length == 0)
			return;

		// new tag
		nextPos = 0;
		if(line[0] == '@')
		{
			String tagStr = sequenceToString(line, 1, length, (short) '=').trim().toLowerCase();
			while(nextPos<length && line[nextPos]==' ') ++nextPos;
			
			EdTags tag = EdTags.find(tagStr);
			
			// TODO if it's an unhandled tag

			// TODO if it's a marker tag

			// it's a paragraph tag
			para = new Paragraph();
			chapter.para.add(para);
		}
		if(nextPos >= length)
			return;

		// convert ed encoding to unicode
		for(int pos=nextPos; pos<length; ++pos)
		{
			int c = line[pos];
			if(line[pos]>=128)
			{
				c = EdCharacter.convert(c);
				if(c != 0)
					para.text.append((char) c);
			}
			else
			{
				para.text.append((char) line[pos]);
			}
		}
	}


	private String sequenceToString(short[] line, int pos, int length, short c)
	{
		StringBuilder sb = new StringBuilder();
		while(pos < length && line[pos] != c)
			sb.append((char) line[pos++]);
		nextPos = pos+1;
		return sb.toString();
	}


	public static void main(String[] args) throws Exception
	{
		new EdFileProcessor().process(new File("c:\\wk2\\Sastra\\BBT\\Text\\BG\\HUBG01XT.H23"), null);
	}
}
