package hu.vidyavana.convert.indd;

import hu.vidyavana.convert.api.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InddFileProcessor implements FileProcessor
{
	private static boolean skipConnect = false;

	private Pattern sameStartAndEnd = Pattern.compile("^<([bi])>(.*)</\\1>$");
	private Pattern hasCharStyle = Pattern.compile("<[bi]>");
	private Pattern emptyStyle = Pattern.compile("<([bi])>(\\s*)</\\1>");
	private Pattern spaceBeforeClosingTag = Pattern.compile("(\\s+)((</[bi]>)+)");
	private Pattern styleOffOn = Pattern.compile("</([bi])>(\\s*)<\\1>");

	private File destDir;
	private String destName;
	private int fileIndex;
	private WriterInfo writerInfo;
	private String ebookPath;
	private List<String> manual;
	
	private int filePageNum, prevFilePageNum;
	private int lineNumber;
	private Book book;
	private Chapter chapter;
	private Paragraph para;
	private Stack<String> formatStack;
	private File tocFile;
	private int textLevel;
	private int flowLevel;
	private StringBuilder[] text;
	private CharacterMapManager charMaps;
	private ParagraphStyle defineParaStyle;
	private Map<String, ParagraphStyle> styleSheet;
	private boolean paraStartPending;
	private boolean continuingPara;
	private Scanner scanner;
	private File destFile;
	private StringBuilder wordBuffer;
	private ReverseHyphenate revHyphen;
	private StyleMapping styleMap;
	private int torzsVersLineNum;
	private int emptyRowsBefore;


	@Override
	public void init(File srcDir, File destDir) throws Exception
	{
		this.destDir = destDir;
		destDir.mkdirs();
		writerInfo = new WriterInfo();
		writerInfo.fileNames = new ArrayList<>();
		// writerInfo.forEbook = !"false".equals(System.getProperty("for.ebook"));
		writerInfo.forEbook = true;
		ebookPath = System.getProperty("ebook.path");
		manual = new ArrayList<String>();
		charMaps = new CharacterMapManager();
		charMaps.init();
		scanner = new Scanner(System.in);
		wordBuffer = new StringBuilder();
		revHyphen = new ReverseHyphenate(destDir);
		revHyphen.init();
		styleMap = new StyleMapping(destDir, new StyleMapping.VenuGita());
		styleMap.init();
		
		if(!writerInfo.forEbook)
		{
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
		startChapter();
	}


	@Override
	public void process(File srcFile, String fileName) throws Exception
	{
		writerInfo.srcFile = srcFile;
		para = null;
		lineNumber = 0;
		styleSheet = new HashMap<>();
		textLevel = 0;
		text = new StringBuilder[10];
		filePageNum = fileName.indexOf('_');
		if(filePageNum > -1)
			filePageNum = Integer.valueOf(fileName.substring(0, filePageNum));
		if(filePageNum != prevFilePageNum)
		{
			if(filePageNum-prevFilePageNum > 5)
				startChapter();
			else
			{
				newPara("UjOldal");
				para.text.append("<!-- page break -->");
			}
		}
		readFile(srcFile);
		prevFilePageNum = filePageNum;
		
		// reminders of manual work
		// if(fileName.toLowerCase().indexOf("hund28xt") != -1)...
	}


	@Override
	public void finish() throws IOException
	{
		endChapter();
		charMaps.close();
		revHyphen.close();
		styleMap.close();

		if(ebookPath != null)
		{
			Files.copy(new File(ebookPath, "ed.xsl").toPath(),
				new File(destDir, "ed.xsl").toPath(),
				StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File(ebookPath, "ed.css").toPath(),
				new File(destDir, "ed.css").toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		}
		
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
		
		for(String m : manual)
		{
			System.out.print("!!! ");
			System.out.println(m);
		}
	}


	private void startChapter() throws IOException
	{
		endChapter();
		book = new Book();
		chapter = new Chapter();
		book.chapter.add(chapter);
		formatStack = new Stack<>();

		++fileIndex;
		destName = "00000" + fileIndex;
		destName = destName.substring(destName.length()-5);
		destFile = new File(destDir.getAbsolutePath() + "/" + destName + ".xml");
		writerInfo.xmlFile = destFile;
		writerInfo.fileNames.add(destFile.getName());
	}


	private void endChapter() throws IOException
	{
		if(book != null)
			book.writeToFile(writerInfo);
	}


	private void newPara(String style)
	{
		Paragraph prevPara = para;
		if(!endPara())
		{
			if(continuingPara)
			{
				continuingPara = false;
				paraStartPending = true;
				return;
			}
			prevPara = para.prev;
			chapter.para.remove(chapter.para.size()-1);
		}
		para = new Paragraph();
		para.style = ParagraphStyle.clone(styleSheet.get(style));
		para.style.basedOn = style;
		para.prev = prevPara;
		chapter.para.add(para);
		text[0] = para.text;
		paraStartPending = true;
		wordBuffer.setLength(0);
	}


	private boolean endPara()
	{
		if(para != null)
		{
			Paragraph prev = para.prev;
			if(!skipConnect && prev != null && para.text.length() > 0 &&
				(prev.cls == ParagraphClass.Fejezetcim || prev.cls == ParagraphClass.TorzsKezdet0Bek) && 
				para.text.charAt(para.text.length()-1)!='.')
			{
				System.out.println("Connect paragraphs?");
				if(para.text.length() < 30 && chapter.para.size() > 1)
					System.out.println(chapter.para.get(chapter.para.size()-2).text);
				System.out.println(para.text);
				String s = scanner.next();
				if(".".equals(s))
				{
					continuingPara = true;
					char c = para.text.charAt(para.text.length()-1);
					if(c != '-' && c != ' ')
						para.text.append(' ');
					return false;
				}
			}
			para.style.emptyRowsBefore = emptyRowsBefore;
			endWord();
			purgeFormatStack();
			cleanupPara();
			styleMap.convertStylename(para, scanner);
			if(para.text.length() == 0)
			{
				++emptyRowsBefore;
				return false;
			}
			if(para.cls == ParagraphClass.TorzsVers)
			{
				if(prev != null && prev.cls == ParagraphClass.TorzsVers)
				{
					++torzsVersLineNum;
					prev.text.append("<br/>");
					if(torzsVersLineNum % 2 == 0)
						prev.text.append("\u2002\u2002");
					prev.text.append(para.text);
					para.text.setLength(0);
					return false;
				}
				else
					torzsVersLineNum = 1;
			}
		}
		emptyRowsBefore = 0;
		return true;
	}


	private void endWord()
	{
		if(wordBuffer.length() > 0)
		{
			if(wordBuffer.length() > 3)
				revHyphen.check(wordBuffer, para.text, scanner);
			wordBuffer.setLength(0);
		}
	}


	private void readFile(File in) throws Exception
	{
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(in), "UTF8")))
		{
			String line;
			while((line = br.readLine()) != null)
			{
				++lineNumber;
				processLine(line);
			}
			if(!endPara())
				chapter.para.remove(chapter.para.size()-1);
		}
	}
	
	
	private void processLine(String line)
	{
		writerInfo.line = line;
		for(int pos = 0; pos < line.length(); ++pos)
		{
			char c = line.charAt(pos);
			boolean skip = false;
			if(c == '<')
			{
				if(flowLevel == 0)
				{
					++textLevel;
					skip = true;
				}
				++flowLevel;
			}
			else if(c == '>')
			{
				--flowLevel;
				if(flowLevel == 0)
				{
					String tag = text[textLevel].toString().trim();
					text[textLevel].setLength(0);
					--textLevel;
					parseTag(tag);
					skip = true;
				}
			}
			if(!skip)
			{
				writerInfo.pos = pos;
				addChar(c);
			}
		}
	}


	private String processLevel(String line)
	{
		++textLevel;
		processLine(line);
		String res = text[textLevel].toString().trim();
		text[textLevel].setLength(0);
		--textLevel;
		return res;
	}


	private void parseTag(String tag)
	{
		if(tag.startsWith("0x"))
		{
			int code = Integer.parseInt(tag.substring(2), 16);
			addChar(code);
		}
		else if(tag.startsWith("ParaStyle:"))
		{
			charMaps.selectFont(CharacterMapManager.DEFAULT_STYLENAME);
			String styleName = processLevel(tag.substring(10));
			charMaps.selectFont(CharacterMapManager.DEFAULT_TEXT);
			newPara(styleName);
		}
		else if(tag.startsWith("DefineParaStyle:"))
		{
			int eq = tag.indexOf('=');
			if(eq < 0)
				return;
			String styleName = tag.substring(16, eq);
			charMaps.selectFont(CharacterMapManager.DEFAULT_STYLENAME);
			styleName = processLevel(styleName);
			charMaps.selectFont(CharacterMapManager.DEFAULT_TEXT);
			String styles = tag.substring(eq+1);
			defineParaStyle = new ParagraphStyle();
			processLine(styles);
			styleSheet.put(styleName, defineParaStyle);
			defineParaStyle = null;
		}
		else
		{
			ParagraphStyle style = defineParaStyle != null ? defineParaStyle : para != null ? para.style : null;
			if(style == null)
				return;
			if(tag.startsWith("cTypeface:"))
			{
				if(style == defineParaStyle)
				{
					// modify style object
					if(tag.endsWith(":Italic"))
						style.italic = true;
					else if(tag.endsWith(":Bold"))
						style.bold = true;
					else if(tag.endsWith(":Bold Italic"))
					{
						style.bold = true;
						style.italic = true;
					}
					else if(!tag.endsWith(":") && !tag.endsWith(":Roman"))
					{
						pos();
						throw new RuntimeException("Unknown typeface: " + tag);
					}
					return;
				}
				// add inline style
				if(tag.endsWith(":") || tag.endsWith(":Roman"))
					purgeFormatStack();
				else if(tag.endsWith(":Italic"))
				{
					boolean psp = paraStartPending;
					addChars("<i>");
					paraStartPending = psp;
					formatStack.push("</i>");
				}
				else if(tag.endsWith(":Bold"))
				{
					addChars("<b>");
					formatStack.push("</b>");
				}
				else if(tag.endsWith(":Bold Italic"))
				{
					addChars("<b><i>");
					formatStack.push("</i></b>");
				}
				else
				{
					pos();
					throw new RuntimeException("Unknown typeface: " + tag);
				}
			}
			else if(tag.startsWith("cFont:"))
			{
				String font = tag.substring(6).trim();
				if(font.length() > 0)
				{
					style.font = font;
					if(style != defineParaStyle)
						charMaps.selectFont(styleFont(style));
				}
			}
			else if(tag.startsWith("cSize:"))
			{
				Integer val = val(tag);
				if(val != null)
					style.size = val;
			}
			else if(tag.startsWith("pTextAlignment:"))
			{
				if(tag.indexOf(":Justify") > -1)
					style.align = Align.Justify;
				else if(tag.endsWith(":Center"))
					style.align = Align.Center;
				else if(tag.endsWith(":Left"))
					style.align = Align.Left;
				else if(tag.endsWith(":Right"))
					style.align = Align.Right;
			}
			else if(tag.startsWith("pSpaceBefore:"))
			{
				Integer val = val(tag);
				if(val != null)
					style.before = val;
			}
			else if(tag.startsWith("pLeftIndent:"))
			{
				Integer val = val(tag);
				if(val != null)
					style.left = val;
			}
			else if(tag.startsWith("pRightIndent:"))
			{
				Integer val = val(tag);
				if(val != null)
					style.right = val;
			}
			else if(tag.startsWith("pFirstLineIndent:"))
			{
				Integer val = val(tag);
				if(val != null)
					style.first = val;
			}
			else if(tag.startsWith("BasedOn:"))
			{
				charMaps.selectFont(CharacterMapManager.DEFAULT_STYLENAME);
				String styleName = processLevel(tag.substring(8));
				charMaps.selectFont(CharacterMapManager.DEFAULT_TEXT);
				style.basedOn = styleName;
			}
		}
	}


	private Integer val(String tag)
	{
		int ix = tag.indexOf(':');
		String num = tag.substring(ix+1);
		if(num.isEmpty())
			return null;
		Double d = Double.valueOf(num);
		return (int)(d*100);
	}


	private void addChar(int c)
	{
		if(text[textLevel] == null)
			text[textLevel] = new StringBuilder(textLevel<2 ? 10000 : 1000);
		if(paraStartPending && textLevel == 0)
		{
			if(c==' ' || c=='\t' || c>=8192 && c<=8207)
				return;
			charMaps.selectFont(styleFont(para.style));
			paraStartPending = false;
		}
		if(c >= 128)
		{
			CharacterMap cmap = charMaps.map();
			Character ch = cmap.get(c);
			if(ch == null)
			{
				pos();
				if(para.text.length() < 50 && chapter.para.size() > 1)
					System.out.println(chapter.para.get(chapter.para.size()-2).text);
				System.out.println(para.text + "..." + c);
				String s = scanner.next();
				if("?".equals(s))
					c = '?';
				else if("X".equals(s))
					throw new RuntimeException("CMap input aborted.");
				else
				{
					ch = s.charAt(0);
					cmap.put(c, ch);
					c = ch;
				}				
			}
			else
				c = ch;
		}
		else if(c == 10)
		{
			StringBuilder sb = text[textLevel];
			char prev = sb.charAt(sb.length()-1);
			if(prev != ' ')
				c = ' ';
			else
				return;
		}
		if(textLevel == 0)
		{
			boolean wordChar = Character.isAlphabetic(c)
				|| c>=256 && c<400 || c>7600 && c<7800 || c=='-';
			if(wordChar)
				wordBuffer.append((char) c);
			else
				endWord();
		}
		text[textLevel].append((char) c);
	}


	private String styleFont(ParagraphStyle style)
	{
		while(style != null)
		{
			if(style.font != null)
				return style.font;
			if(style.basedOn != null)
				style = styleSheet.get(style.basedOn);
		}
		return CharacterMapManager.DEFAULT_TEXT;
	}


	private void addChars(String s)
	{
		for(int i=0, len=s.length(); i<len; ++i)
			addChar(s.charAt(i));
	}


	private void purgeFormatStack()
	{
		endWord();
		while(formatStack.size() > 0)
			para.text.append(formatStack.pop());
	}


	public void cleanupPara()
	{
		boolean change = false;
		String txt = para.text.toString();
		String txt2 = emptyTextStyles(txt);
		if(txt2 != null)
		{
			change = true;
			txt = txt2;
		}
		txt2 = styleOffOn(txt);
		if(txt2 != null)
		{
			change = true;
			txt = txt2;
		}
		txt2 = allTextSameStyle(txt);
		if(txt2 != null)
		{
			change = true;
			txt = txt2;
		}
		txt2 = spaceBeforeClosingTag(txt);
		if(txt2 != null)
		{
			change = true;
			txt = txt2;
		}
		if(change)
		{
			para.text.setLength(0);
			para.text.append(txt);
		}
	}


	public String allTextSameStyle(String txt)
	{
		String orig = txt;
		boolean trimmed = false;
		boolean bold = false;
		boolean italic = false;
		while(true)
		{
			String txt2 = txt.trim();
			if(txt != txt2)
			{
				txt = txt2;
				trimmed = true;
			}
			Matcher m = sameStartAndEnd.matcher(txt);
			if(m.find())
			{
				txt = m.replaceFirst(m.group(2));
				if("b".equals(m.group(1)))
					bold = true;
				else if("i".equals(m.group(1)))
					italic = true;
			}
			else
				break;
		}
		if(hasCharStyle.matcher(txt).find())
			return trimmed ? orig.trim() : null;
		if(orig == txt)
			return null;
		if(bold)
			para.style.bold = true;
		if(italic)
			para.style.italic = true;
		return txt;
	}


	public String styleOffOn(String txt)
	{
		Matcher m = styleOffOn.matcher(txt);
		String txt2 = m.replaceAll("$2");
		if(txt == txt2)
			return null;
		return txt2;
	}


	public String emptyTextStyles(String txt)
	{
		Matcher m = emptyStyle.matcher(txt);
		String txt2 = m.replaceAll("$2");
		if(txt == txt2)
			return null;
		return txt2;
	}


	public String spaceBeforeClosingTag(String txt)
	{
		Matcher m = spaceBeforeClosingTag.matcher(txt);
		String txt2 = m.replaceAll("$2$1");
		if(txt == txt2)
			return null;
		return txt2;
	}
		

	private void pos()
	{
		System.out.println("["+writerInfo.srcFile.getName()+":"+lineNumber+":"+writerInfo.pos+", "+charMaps.selectedFont+"]");
	}
}
