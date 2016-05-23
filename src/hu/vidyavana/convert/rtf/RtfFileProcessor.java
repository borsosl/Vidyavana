package hu.vidyavana.convert.rtf;

import static hu.vidyavana.convert.ed.EdCharacter.UtfMarkers.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import hu.vidyavana.convert.api.*;
import hu.vidyavana.convert.ed.EdCharacter;
import hu.vidyavana.convert.indd.CharacterMap;
import hu.vidyavana.convert.indd.CharacterMapManager;

public class RtfFileProcessor implements FileProcessor
{
	public boolean XML_MODE = false;
	
	File destDir;
	String srcFileName;
	WriterInfo writerInfo;
	
	Book book;
	Chapter chapter;
	Paragraph para;
	File tocFile;

	short[] line;
	int length;
	int ptr;
	
	boolean head;
	Stack<Block> levels = new Stack<Block>();
	Block currentLevel;

	Map<String, String> fontMap = new HashMap<>();
	Map<String, ParagraphStyle> styleMap = new HashMap<>();
	CharacterMapManager charMaps;
	int unicodeTrailSkip;


	
	static class Block
	{
		StringBuilder text = new StringBuilder();
		ParagraphStyle para = new ParagraphStyle();
		boolean destination;
		String destCtrlWord;
		boolean fonttbl;
		String fontCode;
		boolean stylesheet;
		String styleCode;
		int unicodeExtraByte;
		
		static Block clone(Block orig)
		{
			Block block = new Block();
			block.para = ParagraphStyle.clone(orig.para);
			block.fonttbl = orig.fonttbl;
			block.stylesheet = orig.stylesheet;
			block.unicodeExtraByte = orig.unicodeExtraByte;
			return block;
		}
	}


	@Override
	public void init(File srcDir, File destDir)
	{
		this.destDir = destDir;
		writerInfo = new WriterInfo();
		writerInfo.fileNames = new ArrayList<>();

		// xml TOC file
		try
		{
			tocFile = new File(destDir.getAbsolutePath() + "/toc.xml");
			Writer toc = writerInfo.toc = new OutputStreamWriter(new FileOutputStream(tocFile), "UTF-8");
			toc.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			toc.write("<toc>\r\n");
			toc.write("  <version>");
			toc.write(new SimpleDateFormat("yyMMdd").format(new Date()));
			toc.write("</version>\r\n");
			toc.write("  <entries>\r\n");
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Error initializing TOC file.", ex);
		}
	}


	@Override
	public void process(File srcFile, String fileName) throws Exception
	{
		srcFileName = fileName;
		File destFile = new File(destDir.getAbsolutePath() + "/" + fileName + (XML_MODE ? ".xml" : ".001"));
		process(srcFile, destFile);
	}


	@Override
	public void finish() throws IOException
	{
		charMaps.close();
		Writer toc = writerInfo.toc;
		if(toc != null)
			try
			{
				toc.write("  </entries>\r\n");
				toc.write("  <files>\r\n");
				for(String fname : writerInfo.fileNames)
				{
					toc.write("    <file>");
					toc.write(fname);
					toc.write("</file>\r\n");
				}
				toc.write("  </files>\r\n");
				toc.write("</toc>\r\n");
				toc.close();
			}
			catch(IOException ex)
			{
				throw new RuntimeException("Error writing TOC file.", ex);
			}
	}


	public void process(File rtf, File out) throws Exception
	{
		book = new Book();
		chapter = new Chapter();
		book.chapter.add(chapter);
		
		readRtfFile(rtf);
		
		out.getParentFile().mkdirs();
		if(XML_MODE)
		{
			writerInfo.xmlFile = out;
			writerInfo.fileNames.add(out.getName());
			book.writeToFile(writerInfo);
		}
		else
			writeEdFile(out);
	}


	private void readRtfFile(File rtf) throws Exception
	{
		initRtf();
		try(InputStream is = new BufferedInputStream(new FileInputStream(rtf)))
		{
			// use short instead of byte to work around signed byte handling
			line = new short[100000];
			ptr = 0;
			while(true)
			{
				int c = is.read();
				if(c=='\n' || (ptr > 90000 && c == '{') || c<0)
				{
					if(ptr > 0)
					{
						length = ptr;
						ptr = 0;
						processBuffer();
					}
					if(c < 0) break;
					ptr = 0;
				}
				if(c < ' ') continue;
				line[ptr++] = (short) c;
			}
		}
	}
	
	
	private void initRtf() throws Exception
	{
		head = true;
		charMaps = new CharacterMapManager();
		charMaps.init();
		charMaps.selectFont(CharacterMapManager.DEFAULT_STYLENAME);
		if(!XML_MODE)
			EdCharacter.initReverseConversion();
	}


	private void writeEdFile(File out) throws Exception
	{
		try(BufferedOutputStream f = new BufferedOutputStream(new FileOutputStream(out)))
		{
			for(Paragraph p : chapter.para)
			{
				String s = p.text.toString();
				s = s.replace("<R>", "<R>\r\n");
				int cnt = 0;
				for(int i=0; i<s.length(); ++i)
				{
					int c = s.charAt(i);
					if(++cnt > 80 && c == ' ')
					{
						f.write(13);
						f.write(10);
						cnt = 0;
					}
					else
					{
						f.write((byte) c);
						if(c == 10)
							cnt = 0;
					}
				}
				f.write(13);
				f.write(10);
				f.write(13);
				f.write(10);
			}
		}
	}


	private void processBuffer()
	{
		// control word | control symbol | group | text
		while(true)
		{
			int c = nextChar();
			if(c == -1)
				return;
			if(c == '\\')
			{
				int c1 = line[ptr];
				if(c1 == '\\' || c1 == '{' || c1 == '}')
				{
					chr(c1, false, false);
					++ptr;
				}
				else if(c1 >= 'a' && c1 <= 'z')
					controlWord();
				else
					controlSymbol();
			}
			else if(c == '{')
				group();
			else if(c == '}')
				endGroup();
			else
				chr(c, false, false);
		}
	}


	private void group()
	{
		if(levels.size() == 0)
			levels.push(currentLevel = new Block());
		else
			levels.push(currentLevel = Block.clone(currentLevel));
	}


	private void endGroup()
	{
		pendingDestination();
		if(currentLevel.fonttbl && currentLevel.fontCode != null)
		{
			int semi = currentLevel.text.lastIndexOf(";");
			if(semi > -1)
				fontMap.put(currentLevel.fontCode, currentLevel.text.substring(0, semi));
		}
		if(currentLevel.stylesheet && currentLevel.styleCode != null)
		{
			int semi = currentLevel.text.lastIndexOf(";");
			if(semi > -1)
			{
				String name = currentLevel.text.substring(0, semi);
				name = name.replace(' ', '_');
				currentLevel.para.basedOn = name;
			}
			styleMap.put(currentLevel.styleCode, currentLevel.para);
		}
		Block prevLevel = levels.pop();
		if(levels.size() == 0)
			return;
		currentLevel = levels.peek();
		if(!head)
		{
			if(prevLevel.text.length() > 0)
				wrapWithFormatting(prevLevel, currentLevel);
			fontChanged();
		}
		// close char-style runs unless popped block has the same in the same para
	}


	private void wrapWithFormatting(Block child, Block parent)
	{
		StringBuilder sb = para.text;
		boolean italic = Boolean.TRUE.equals(child.para.italic) && child.para.italic != parent.para.italic;
		boolean bold = Boolean.TRUE.equals(child.para.bold) && child.para.bold != parent.para.bold;
		if(bold) str("<b>", "<B>", sb);
		if(italic) str("<i>", "<MI>", sb);
		sb.append(child.text);
		child.text.setLength(0);
		if(italic) str("</i>", "<M>", sb);
		if(bold) str("</b>", "<M>", sb);
	}


	private void controlWord()
	{
		pendingDestination();
		int c;
		StringBuilder sb = new StringBuilder();
		c = nextChar();
		while(c >= 'a' && c <= 'z')
		{
			sb.append((char) c);
			c = nextChar();
		}
		String word = sb.toString();
		sb.setLength(0);
		if(c == '-')
		{
			sb.append((char) c);
			c = nextChar();
		}
		while(c >= '0' && c <= '9')
		{
			sb.append((char) c);
			c = nextChar();
		}
		if(c != ' ' && c != -1)
			--ptr;
		if(currentLevel.destination)
			currentLevel.destCtrlWord = word;
		processControlWord(word, sb.toString(), null);
	}


	private void pendingDestination()
	{
		if(!currentLevel.destination || currentLevel.destCtrlWord == null)
			return;
		processControlWord(currentLevel.destCtrlWord, null, currentLevel.text);
		currentLevel.destination = false;
		currentLevel.text.setLength(0);
	}


	private void processControlWord(String word, String par, StringBuilder destText)
	{
		// simple control with numeric param, or destination with any param.
		ParagraphStyle s = currentLevel.para;
		switch(word)
		{
			case "i":
				currentLevel.para.italic = !"0".equals(par);
				break;
			case "b":
				currentLevel.para.bold = !"0".equals(par);
				break;
			case "u":
				chr(Integer.valueOf(par));
				unicodeTrailSkip = currentLevel.unicodeExtraByte;
				break;
			case "par":
				endPara();
				break;
			case "f":
				if(currentLevel.fonttbl)
					currentLevel.fontCode = par;
				else
				{
					currentLevel.para.font = fontMap.get(par);
					if(!head)
						fontChanged();
				}
				break;
			case "s": case "cs":
				if(currentLevel.stylesheet)
					currentLevel.styleCode = par;
				else
				{
					currentLevel.para.apply(styleMap.get(par));
					if(!head)
						fontChanged();
				}
				break;
			case "emdash":
				chr('—');
				break;
			case "endash":
				chr('–');
				break;
			case "lquote":
				chr('‘');
				break;
			case "rquote":
				chr('’');
				break;
			case "ldblquote":
				chr('“');
				break;
			case "rdblquote":
				chr('”');
				break;
			case "tab":
				str("<!--tab-->", "<T>");
				break;
			case "line":
				str("<br />", "<R>");
				break;
			case "pard":
				head = false;
				para = new Paragraph();
				// intentional fallthru
			case "sectd":
				s.align = Align.Left;
				s.before = 0;
				s.first = 0;
				break;
			case "plain":
				s = currentLevel.para;
				s.bold = false;
				s.italic = false;
				break;
			case "qj":
				s.align = Align.Justify;
				break;
			case "ql":
				s.align = Align.Left;
				break;
			case "qc":
				s.align = Align.Center;
				break;
			case "qr":
				s.align = Align.Right;
				break;
			case "uc":
				currentLevel.unicodeExtraByte = Integer.valueOf(par);
				break;
			case "footnote":
				// TODO
				break;
			case "fonttbl":
				currentLevel.fonttbl = true;
				break;
			case "stylesheet":
				currentLevel.stylesheet = true;
				break;
		}
	}


	private void controlSymbol()
	{
		pendingDestination();
		int c = nextChar();
		if(c == '\'')
		{
			String hex = ""+((char) nextChar())+((char) nextChar());
			c = Integer.parseInt(hex, 16);
			chr(c, true, false);
		}
		else if(c == '*')
		{
			currentLevel.destination = true;
			currentLevel.destCtrlWord = null;
		}
		else if(c == '~')
		{
			if(XML_MODE)
				chr(160, false, false);
			else
				str("<N>");
		}
		else if(c == '-' && !XML_MODE)
			str("<->");
		else if(c == '_')
			chr('–');
	}


	private void endPara()
	{
		try
		{
			para.cls = ParagraphClass.valueOf(currentLevel.para.basedOn);
		}
		catch(Exception ex)
		{
			para.cls = ParagraphClass.TorzsKoveto;
		}
		wrapWithFormatting(currentLevel, new Block());

		String italicRestart = XML_MODE ? "</i><i>" : "<M><MI>";
		while(true)
		{
			int ix = para.text.indexOf(italicRestart);
			if(ix == -1) break;
			para.text.delete(ix, ix+italicRestart.length());
		}
		String boldRestart = XML_MODE ? "</b><b>" : "<M><B>";
		while(true)
		{
			int ix = para.text.indexOf(boldRestart);
			if(ix == -1) break;
			para.text.delete(ix, ix+boldRestart.length());
		}
		if(!XML_MODE)
			while(true)
			{
				int ix = para.text.indexOf("<M><M>");
				if(ix == -1) break;
				para.text.delete(ix, ix+6);
			}
			
		chapter.para.add(para);
		para = new Paragraph();
	}


	private void fontChanged()
	{
		ParagraphStyle style = currentLevel.para;
		String font;
		if(style != null && style.font != null)
			font = style.font;
		else
			font = CharacterMapManager.DEFAULT_TEXT;
		charMaps.selectFont(font);
	}


	private void chr(int c)
	{
		chr(c, true, true);
	}


	private void chr(int c, boolean convert, boolean unicode)
	{
		if(unicodeTrailSkip-- > 0)
			return;
		if(convert)
		{
			// from font encoding to Unicode
			if(!unicode && c >= 128)
			{
				CharacterMap cmap = charMaps.map();
				Character ch = cmap.get(c);
				if(ch == null && c < 256)
				{
					str("{`#!"+c+":"+currentLevel.para.font+"}");
					return;
				}
				c = ch;
			}
			
			// from Unicode to Ed
			if(!XML_MODE)
				try
				{
					if(c == 8201)
						c = 127;
					else if(c == -3840)
						c = 0x93;
					else if(c == -3816)
						c = 0x97;
					else
					{
						c = EdCharacter.convertToEd(c);
						if(c == Nbsp.code)
						{
							str("<N>");
							return;
						}
					}
				}
				catch(Exception ex)
				{
					str("{`#"+c+"}");
					return;
				}
		}
		currentLevel.text.append((char) c);
	}


	private void str(String s)
	{
		currentLevel.text.append(s);
	}


	private void str(String xml, String ed)
	{
		if(XML_MODE)
			str(xml);
		else
			str(ed);
	}


	private void str(String xml, String ed, StringBuilder sb)
	{
		if(XML_MODE)
			sb.append(xml);
		else
			sb.append(ed);
	}


	private int nextChar()
	{
		if(ptr == length)
			return -1;
		return line[ptr++];
	}
	
	
	public static void main(String[] args) throws Exception
	{
		String[] names = {};
		for(String name : names)
		{
			RtfFileProcessor rtf = new RtfFileProcessor();
			rtf.init(new File(""), new File(""));
			rtf.process(new File(""+name), name.substring(0, name.indexOf('.')));
			rtf.finish();
		}
	}
}
