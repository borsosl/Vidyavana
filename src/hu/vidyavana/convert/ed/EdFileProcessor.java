package hu.vidyavana.convert.ed;

import static hu.vidyavana.convert.ed.EdPreviousEntity.*;
import hu.vidyavana.convert.api.*;
import java.io.*;
import java.util.Stack;

public class EdFileProcessor
{
	private String srcFileName;
	private int lineNumber;
	private Book book;
	private Chapter chapter;
	private Paragraph para;
	private EdTags currentTag;
	private int nextPos;
	private EdPreviousEntity prev;
	private Stack<String> formatStack;
	private boolean skippingUnhandledTag;


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
		
		xml.getParentFile().mkdirs();
		book.writeToFile(xml);
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
			skippingUnhandledTag = false;
			
			String tagStr = sequenceToString(line, 1, length, (short) '=').trim().toLowerCase();
			while(nextPos<length && line[nextPos]==' ') ++nextPos;
			
			currentTag = EdTags.find(tagStr);
			if(currentTag == null)
				throw new IllegalStateException(String.format("ERROR: Nem definialt tag '%s' a '%s' fajlban. Sor: %d.", tagStr, srcFileName, lineNumber));
			
			// if it's an unhandled tag
			if(currentTag.alias == EdTags.unhandled)
			{
				skippingUnhandledTag = true;
				return;
			}

			// info or content tags
			para = new Paragraph();
			String tagName = currentTag.name();
			switch(currentTag)
			{
				case book_title:
					tagName = "title";
					// no break!
				case lila:
					book.info.add(para);
					break;
				case chaptno:
					tagName = "chapter_number";
					chapter.info.add(para);
					break;
				case chapter_head:
					tagName = "head";
					chapter.info.add(para);
					break;
				case textno:
					tagName = "text_number";
					// no break!
				default:
					chapter.para.add(para);
			}
			if(currentTag.alias == EdTags.info)
				para.tagName = tagName;
			else
				para.cls = currentTag.cls;
			prev = Tag;
		}

		if(skippingUnhandledTag || nextPos >= length)
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
							String num = number.toString();
							if(processed == 'J')
							{
								if("2".equals(num))
								{
									
								}
							}
							// TODO
							processed = -1;
							number = null;
						}
					}
					
					if(c == '$')
					{
						
					}
					
					if(c == 'D') c = 'M';
					
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
							para.cls = ParagraphClass.Kozepen;
						else if(c2 == 'R')
							para.cls = ParagraphClass.Jobbra;
					}
					
					else if(c == '~')
					{
						// only count leading indent marks: they may only serve right alignment purposes
						if(prev==Tag)
							++para.indent;
						else
						{
							para.text.append('\u2002');
							prev = Space;
						}
					}
					
					else if(c == 'R')
					{
						para.text.append("<br/>");
						prev = Linebreak;
					}
					
					else if(c == '-')
					{
						prev = Hyphen;
					}
					
					else if(c == '+')
					{
						
					}
					
					else if(c == '|')
					{
						prev = Microspace;
					}
					
					else if(c == 'N')
					{
						para.text.append('\u00a0');
						prev = Space;
					}
					
					else if(c == '_')
					{
						para.text.append('\u2003');
						prev = Space;
					}
					
					else if(c == 'T')
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
						// disregard, only expect this in script
						if(!currentTag.name().startsWith("sans") && 
							!currentTag.name().startsWith("ben"))
								throw new IllegalStateException(String.format("ERROR: <\\d+> formazas a '%s' fajlban. Sor: %d.", c, srcFileName, lineNumber));
					}
					
					
					if(c == '>' || pos>=length)
						break;
				}
				// sanity check
				if(pos >= length)
					throw new IllegalStateException(String.format("ERROR: Hibas karakter formazas a '%s' fajlban. Sor: %d.", c, srcFileName, lineNumber));
			}
			
			else if(c == 127)
			{
				int msp = 1;
				while(++pos<length && line[pos]==127)
					++msp;
				--pos;
				if(msp > 2)
					para.text.append(' ');
				prev = Microspace;
			}

			// printable characters
			else
			{
				// process first line indent
				if(prev == Beginning && para.indent > 0)
				{
					// if style has 2 indents by default
					if(para.cls != null && para.cls.defaultIndent && para.indent == 2)
						para.indent = 0;
					// if many indent marks start a paragraph
					else if(para.indent > 4)
					{
						if(para.cls == ParagraphClass.TorzsKoveto)
							para.cls = ParagraphClass.Jobbra;
						else if(para.cls == ParagraphClass.MegjegyzesKoveto)
							para.cls = ParagraphClass.MegjegyzesJobbra;
						para.indent = 0;
					}
					// none of the above: store indent in extra style
					if(para.indent > 0)
					{
						if(para.style == null)
							para.style = new ParagraphStyle();
						para.style.first = para.indent;
					}
				}
				
				// convert ed encoding to unicode
				if(c >= 128)
				{
					c = EdCharacter.convert(c);
					if(c == 0)
						throw new IllegalStateException(String.format("ERROR: Nem definialt karakterkod '%d' a '%s' fajlban. Sor: %d.", c, srcFileName, lineNumber));
					para.text.append((char) c);
					prev = Char;
				}
				
				// plain ascii characters
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
