package hu.vidyavana.convert.ed;

import static hu.vidyavana.convert.ed.EdPreviousEntity.*;
import hu.vidyavana.convert.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class EdFileProcessor implements FileProcessor
{
	private File destDir;
	private String srcFileName;
	private String ebookPath;
	private boolean forEbook;
	private List<String> manual;
	
	private int lineNumber;
	private Book book;
	private Chapter chapter;
	private Paragraph para;
	private EdTags currentTag;
	private EdTags currentAlias;
	private int nextPos;
	private EdPreviousEntity prev;
	private Stack<String> formatStack;
	private boolean skippingUnhandledTag;
	private int lastTextTag;
	private int fontCode;
	private int microspace;
	private int emspace;
	private boolean superscript;
	private StringBuilder deferredMarkup;


	@Override
	public void init(File srcDir, File destDir)
	{
		this.destDir = destDir;
		ebookPath = System.getProperty("ebook.path");
		forEbook = System.getProperty("for.ebook") != null;
		manual = new ArrayList<String>();
	}


	@Override
	public void process(File srcFile, String fileName) throws Exception
	{
		srcFileName = fileName;
		File destFile = new File(destDir.getAbsolutePath() + "/" + fileName + ".xml");
		process(srcFile, destFile);
		
		// reminders of manual work
		if(fileName.toLowerCase().indexOf("hund28xt") != -1)
			manual.add("hund28xt.h50: footnote to be merged!");
	}


	@Override
	public void finish()
	{
		for(String m : manual)
		{
			System.out.print("!!! ");
			System.out.println(m);
		}
	}


	public void process(File ed, File xml) throws Exception
	{
		lineNumber = 1;
		book = new Book();
		chapter = new Chapter();
		book.chapter.add(chapter);
		prev = Beginning;
		formatStack = new Stack<>();
		lastTextTag = -100;
		deferredMarkup = new StringBuilder();
		
		readEdFile(ed);
		
		File outDir = xml.getParentFile();
		if(outDir.mkdirs() && ebookPath != null)
		{
			Files.copy(new File(ebookPath, "ed.xsl").toPath(),
				new File(outDir, "ed.xsl").toPath(),
				StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File(ebookPath, "ed.css").toPath(),
				new File(outDir, "ed.css").toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		}
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
		purgeFormatStack();
	}
	
	
	private void processLine(short[] line, int length)
	{
		// handle tags at the beginning of lines
		nextPos = 0;
		if(line[0] == '@')
		{
			if(superscript)
			{
				para.text.append("</sup>");
				superscript = false;
			}
			purgeFormatStack();
			skippingUnhandledTag = false;
			
			String tagStr = sequenceToString(line, 1, length, (short) '=').trim().toLowerCase();
			while(nextPos<length && line[nextPos]==' ') ++nextPos;
			
			currentTag = EdTags.find(tagStr);
			if(currentTag == null)
				throw new IllegalStateException(String.format("ERROR: Nem definialt tag '%s' a '%s' fajlban. Sor: %d.", tagStr, srcFileName, lineNumber));
			currentAlias = currentTag.alias;
			if(currentAlias == null)
				currentAlias = currentTag;
			
			// if it's an unhandled tag
			if(currentAlias == EdTags.unhandled)
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
					// text precedes textno with a max. of 1 tag in between
					if(chapter.para.size()-lastTextTag < 2)
					{
						// insert text_number before text
						chapter.para.add(lastTextTag-1, para);
						break;
					}
					// no break: add text_number as last
				default:
					chapter.para.add(para);
			}

			// info is represented as xml tag
			if(currentAlias == EdTags.info)
				para.tagName = tagName;

			// book text is p tag with a class attribute
			else
				para.cls = currentAlias.cls;
			
			// after footnote or any other quote para, following purp para is changed to purport for line spacing
			if(currentAlias == EdTags.purp_para)
			{
				Paragraph prevPara = chapter.para.get(chapter.para.size()-2);
				if(prevPara.cls != null && prevPara.cls.name().startsWith("Megjegyzes"))
				{
					currentTag = currentAlias = EdTags.purport;
					para.cls = currentAlias.cls;
				}
			}

			// register index level
			else if(currentAlias == EdTags.index_level_0)
			{
				if(tagName.startsWith("index_level"))
					para.indexLevel = Integer.parseInt(tagName.substring(12));
				else if(tagName.startsWith("xi"))
					para.indexLevel = Integer.parseInt(tagName.substring(2, 3));
			}

			// remember position of text tag
			else if(currentAlias == EdTags.text)
				lastTextTag = chapter.para.size();

			// mark footnote paragraphs
			else if(currentTag == EdTags.footnote || currentTag == EdTags.small_foot)
				para.text.append("Lábjegyzet: ");

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
		fontCode = 0;
		microspace = 0;
		emspace = 0;
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
							if(processed == 'J')
							{
								double num = Double.parseDouble(number.toString());
								if(num > 200d)
								{
									para.text.append("<sup>");
									superscript = true;
								}
								else if(superscript)
								{
									para.text.append("</sup>");
									superscript = false;
								}
							}
							else if(processed == 'F')
							{
								fontCode = Integer.parseInt(number.toString());
								if(fontCode == 255)
									fontCode = 0;
							}
							
							processed = -1;
							number = null;
						}
					}
					
					if(c == '$')
					{
						StringBuilder collect = new StringBuilder();
						for(++pos; (c=line[pos]) != '>'; ++pos)
							collect.append((char) c);
						if(collect.charAt(0)=='!')
						{
							if(collect.length()==1)		// <$!>
								prev = Linebreak;
							else
							{
								int c1 = collect.charAt(1);
								if(c1 == ' ')
									prev = OptionalSpace;
								else if(c1 == 'N')
								{
									if(prev == OptionalSpace)
										prev = Char;
								}
								else if(c1 == '|')
								{
									para.text.append(' ');
									prev = Space;
								}
								else if(c1 == 127)
								{
									microspace += collect.length()-1;
								}
							}
						}
					}
					
					if(c == 'D') c = 'M';
					
					// M/MI
					if(c == 'M')
					{
						int c2 = Character.toUpperCase(line[pos+1]);
						if(c2 == 'I')
						{
							++pos;
							if(formatStack.size()==0 || !("</i>".equals(formatStack.peek())))
							{
								if(emspace > 0 || microspace > 0)
									deferredMarkup.append("<i>");
								else
									para.text.append("<i>");
								formatStack.push("</i>");
							}
						}
						else
							purgeFormatStack();
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
							++pos;
						}
						else
						{
							if(formatStack.size()==0 || !("</b>".equals(formatStack.peek())))
							{
								if(emspace > 0 || microspace > 0)
									deferredMarkup.append("<b>");
								else
									para.text.append("<b>");
								formatStack.push("</b>");
							}
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
						// count and handle later: they may only serve right alignment purposes
						++emspace;
					}
					
					else if(c == 'R')
					{
						if(prev == OptionalSpace)
							prev = Char;
						else if(currentAlias != EdTags.chapter_title)
						{
							para.text.append("<br/>");
							prev = Linebreak;
						}
					}
					
					else if(c == '-')
					{
						prev = Hyphen;
					}
					
					else if(c == '+')
					{
						para.text.append("&tab_num;");
						prev = Space;
					}
					
					else if(c == '|')
					{
						if(prev != Space)
						{
							para.text.append(' ');
							prev = Space;
						}
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
						para.text.append("&tab;");
						prev = Space;
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
				++microspace;

			// printable characters
			else
			{
				postProcessCountedSymbols(line);
				
				// convert ed encoding to unicode
				if(c >= 128)
				{
					if(fontCode == 299)
					{
						if(c == 0x8a)
							c = '+';
					}
					
					if(c >= 128)
					{
						int savedCode = c;
						c = EdCharacter.convert(c);
						if(c == 0)
							throw new IllegalStateException(String.format("ERROR: Nem definialt karakterkod '%d' a '%s' fajlban. Sor: %d.", savedCode, srcFileName, lineNumber));
						if(c == '—' && prev == Space && currentAlias == EdTags.word_by_word)
						{
							// replace space with m dash in wbw
							para.text.setCharAt(para.text.length()-1, (char) c);
							prev = Dash;
							continue;
						}
					}
					
					para.text.append((char) c);
					if(c == '–' || c=='—')
						prev = Dash;
					else
						prev = Char;
				}
				
				// plain ascii characters
				else
				{
					if(c == ' ')
					{
						if(currentAlias == EdTags.word_by_word && prev == Dash)
							continue;
						prev = Space;
					}
					else if(c == '-')
						prev = Hyphen;
					else if(c == '"')
					{
						c = '”';
						prev = Char;
					}
					else
						prev = Char;

					para.text.append((char) c);
				}
			}
		}
		postProcessCountedSymbols(line);
	}


	private String sequenceToString(short[] line, int pos, int length, short c)
	{
		StringBuilder sb = new StringBuilder();
		while(pos < length && line[pos] != c)
			sb.append((char) line[pos++]);
		nextPos = pos+1;
		return sb.toString();
	}


	private void purgeFormatStack()
	{
		while(formatStack.size() > 0)
			para.text.append(formatStack.pop());
	}


	private void postProcessCountedSymbols(short[] line)
	{
		if(emspace > 0)
		{
			// process first line indent
			if(prev == Tag)
			{
				// if style has 2 indents by default: disregard marks
				if(para.cls != null && para.cls.defaultIndent)
				{
					emspace -= 2;
					if(emspace < 0)
						emspace = 0;
				}
				
				// if many indent marks start a paragraph
				else if(emspace > 4)
				{
					if(para.cls == ParagraphClass.TorzsKoveto)
						para.cls = ParagraphClass.Jobbra;
					else if(para.cls == ParagraphClass.MegjegyzesKoveto)
						para.cls = ParagraphClass.MegjegyzesJobbra;
					emspace = 0;
				}
			}
			
			if(forEbook && prev == Linebreak)
				emspace = 0;

			if(emspace > 3)
			{
				if(currentAlias == EdTags.word_by_word)
					emspace = 2;
				else if(currentTag == EdTags.footnote)
					emspace = 0;
				else if(currentTag != EdTags.bengali && currentTag != EdTags.center_line)
				{
					System.out.println("Tab line: " + sequenceToString(line, 0, 200, (short) '\r'));
					para.text.append("&tab;");
					emspace = 0;
				}
			}

			while(emspace > 0)
			{
				para.text.append('\u2002');
				--emspace;
			}
			prev = Space;

			if(deferredMarkup.length() > 0)
			{
				para.text.append(deferredMarkup);
				deferredMarkup.setLength(0);
			}
		}

		else if(microspace > 0)
		{
			if(microspace >= 4 || microspace >= 2 && currentAlias != EdTags.word_by_word)
			{
				para.text.append(' ');
			}
			microspace = 0;
			prev = Microspace;

			if(deferredMarkup.length() > 0)
			{
				para.text.append(deferredMarkup);
				deferredMarkup.setLength(0);
			}
		}
	}
}
