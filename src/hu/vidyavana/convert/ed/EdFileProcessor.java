package hu.vidyavana.convert.ed;

import static hu.vidyavana.convert.ed.EdPreviousEntity.*;
import hu.vidyavana.convert.api.Book;
import hu.vidyavana.convert.api.Chapter;
import hu.vidyavana.convert.api.Paragraph;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Stack;

public class EdFileProcessor
{
	private String srcFileName;
	private int lineNumber;
	private Book book;
	private Chapter chapter;
	private Paragraph para;
	private int nextPos;
	private EdPreviousEntity prev;
	private Stack<String> formatStack;


	public void process(File ed, File xml) throws Exception
	{
		srcFileName = ed.getName();
		lineNumber = 1;
		book = new Book();
		chapter = new Chapter();
		book.chapter.add(chapter);
		prev = Beginning;
		formatStack = new Stack<>();
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
				++lineNumber;
				continue;
			}
			if(c=='\r' || ptr==0 && (c==' ' || c=='\t')) continue;
			line[ptr++] = (short) c;
		}
	}
	
	
	private void processLine(short[] line, int length)
	{
		// handle tags at the beginning of lines
		nextPos = 0;
		if(line[0] == '@')
		{
			String tagStr = sequenceToString(line, 1, length, (short) '=').trim().toLowerCase();
			while(nextPos<length && line[nextPos]==' ') ++nextPos;
			
			EdTags tag = EdTags.find(tagStr);
			if(tag == null)
				throw new IllegalStateException(String.format("ERROR: Nem definialt tag '%s' a '%s' fajlban. Sor: %d.", tagStr, srcFileName, lineNumber));
			
			// TODO if it's an unhandled tag

			// TODO if it's a marker tag

			// it's a paragraph tag
			para = new Paragraph();
			chapter.para.add(para);
			prev = Tag;
		}
		if(nextPos >= length)
			return;

		// microspace to start a line: prevent implied space
		if(line[nextPos] == 127)
			prev = Microspace;
		
		// implied space if the previous line ended with a letter or punctuation
		if(prev == Char)
		{
			para.text.append(' ');
			prev = Space;
		}

		// iterate the characters of the line after the optional tag
		for(int pos=nextPos; pos<length; ++pos)
		{
			int c = line[pos];
			
			// handle inline formatting
			if(c == '<')
			{
				int processed = -1; 
				StringBuilder number = null;	// %fpjk után
				while(true)
				{
					c = Character.toUpperCase(line[++pos]);
					
					// process numbers after specific letters
					if(number != null)
					{
						if(c>='0' && c<='9' || c=='-' && number.length()==0 || c=='.')
						{
							number.append((char) c);
							continue;
						}
						else
						{
							// TODO
							processed = -1;
							number = null;
						}
					}
					
					if(c == '$')
					{
						
					}
					
					// M/MI
					if(c == 'M')
					{
						int c2 = Character.toUpperCase(line[pos+1]);
						if(c2 == 'I')
						{
							++pos;
							para.text.append("<i>");
							formatStack.push("</i>");
						}
						else
							while(formatStack.size() > 0)
								para.text.append(formatStack.pop());
					}
					
					// B/BI/BR
					else if(c == 'B')
					{
						int c2 = Character.toUpperCase(line[pos+1]);
						if(c2 == 'I')
						{
							++pos;
							para.text.append("<b><i>");
							formatStack.push("</i></b>");
						}
						else if(c2 == 'R')
						{
							// TODO
							++pos;
						}
						else
						{
							para.text.append("<b>");
							formatStack.push("</b>");
						}
					}
					
					// QC, QJ, QR
					else if(c == 'Q')
					{
						int c2 = Character.toUpperCase(line[++pos]);
						if(c2 == 'C')
							para.cls = "";	// TODO center line
						else if(c2 == 'R')
							para.cls = "";	// TODO right align
					}
					
					// számDT
					else if(c == '~')
					{
						
					}
					
					else if(c == 'R')
					{
						
					}
					
					else if(c == '-')
					{
						
					}
					
					else if(c == '+')
					{
						
					}
					
					else if(c == '|')
					{
						
					}
					
					else if(c == 'N')
					{
						
					}
					
					else if(c == '_')
					{
						
					}
					
					else if(c == 'T')
					{
						
					}
					
					else if(c == 'D')
					{
						
					}
					
					// numbered ones
					else if(number == null && "%FPJK".indexOf(c) != -1)
					{
						processed = c;
						number = new StringBuilder();
						continue;
					}
					
					else if(c>='0' && c<='9')
					{
					}
					
					
					if(c == '>' || pos>=length)
						break;
				}
				// sanity check
				if(pos >= length)
					throw new IllegalStateException(String.format("ERROR: Hibas karakter formazas a '%s' fajlban. Sor: %d.", c, srcFileName, lineNumber));
			}
			
			// convert ed encoding to unicode
			else if(c>=128)
			{
				c = EdCharacter.convert(c);
				if(c == 0)
					throw new IllegalStateException(String.format("ERROR: Nem definialt karakterkod '%d' a '%s' fajlban. Sor: %d.", c, srcFileName, lineNumber));
				para.text.append((char) c);
				prev = Char;
			}

			// store ascii characters
			else
			{
				if(c == '"')
					c = '”';
				para.text.append((char) c);
				
				if(c == ' ')
					prev = Space;
				else if(c == '-')
					prev = Hyphen;
				else
					prev = Char;
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
