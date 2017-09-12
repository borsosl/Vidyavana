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
	// conversion phase settings
	private static boolean skipHyphens = true;
	private static boolean skipFootnotes = true;
	private static boolean skipConnect = true;
	private static boolean markStrippedFormatting = true;
	private static boolean removeOptionalHyphens = false;
	
	// book-specific settings
	private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{}));
	// NPH hun: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{54,342,348,374,392,432}));
	// NPH eng: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{32,318,322,346,364}));
	// KS eng: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{22,122,592,594,660,664}));
	// SBC eng: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{26,28,646}));
	// SBC hun: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{28,804}));
	// SG eng: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{162, 182, 228, 266}));
	// SOI eng: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{14, 34, 44, 72, 78}));
	// NVM eng 1: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{26}));
	// NVM eng 6: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{806}));
	// NVM hun 1: private Set<Integer> forceNewFile = new HashSet(Arrays.asList(new Integer[]{28,628}));
	private Set<Integer> noNewFile = new HashSet(Arrays.asList(new Integer[]{}));
	// NPH hun: private Set<Integer> noNewFile = new HashSet(Arrays.asList(new Integer[]{522}));
	// KS eng: private Set<Integer> noNewFile = new HashSet(Arrays.asList(new Integer[]{18,64,121,171,443,481,508}));
	// SBC eng: private Set<Integer> noNewFile = new HashSet(Arrays.asList(new Integer[]{639}));
	// SBC hun: private Set<Integer> noNewFile = new HashSet(Arrays.asList(new Integer[]{22,687}));
	// SG eng: private Set<Integer> noNewFile = new HashSet(Arrays.asList(new Integer[]{65}));
	// NVM eng 1: private Set<Integer> noNewFile = new HashSet(Arrays.asList(new Integer[]{18}));
	// NVM hun 1: private Set<Integer> noNewFile = new HashSet(Arrays.asList(new Integer[]{24}));
	private int chapterDigits = 2;
	private String endNoteFileName = "99";
	// NPH: private String endNoteFileName = "22";
	// KS: private String endNoteFileName = "19";
	private Map<String, String> supPrefix = new HashMap<String, String>()
	{{
		/* KS eng
		put("00012_00065,00_00151,99.txt", "01");
		put("00018_00066,48_00151,99.txt", "02");
		put("00022_00065,00_00151,99.txt", "03");
		put("00090_00065,00_00151,99.txt", "1");
		put("00124_00065,00_00151,99.txt", "2");
		put("00152_00065,00_00151,99.txt", "3");
		put("00184_00064,81_00151,99.txt", "4");
		put("00218_00065,00_00151,99.txt", "5");
		put("00258_00065,00_00151,99.txt", "6");
		put("00294_00065,00_00151,99.txt", "7");
		put("00330_00065,00_00151,99.txt", "8");
		put("00372_00065,00_00151,99.txt", "9");
		put("00416_00065,00_00151,99.txt", "A");
		put("00454_00065,00_00151,99.txt", "B");
		put("00500_00065,00_00151,99.txt", "C");
		put("00548_00065,00_00151,99.txt", "E");
		put("00590_00065,00_00151,99.txt", "M");

		/* NPH eng
		put("00014_00065,00_00151,99.txt", "01");
		put("00000_00065,00_00016,00.txt", "02");
		put("00036_00065,00_00151,99.txt", "11");
		put("00060_00065,00_00151,99.txt", "12");
		put("00080_00065,00_00151,99.txt", "13");
		put("00108_00065,00_00151,99.txt", "14");
		put("00130_00065,00_00151,99.txt", "15");
		put("00164_00065,00_00151,99.txt", "21");
		put("00190_00065,00_00151,99.txt", "22");
		put("00214_00065,00_00151,99.txt", "23");
		put("00244_00065,00_00151,99.txt", "31");
		put("00264_00065,00_00151,99.txt", "32");
		put("00286_00065,00_00151,99.txt", "33");
		put("00302_00065,00_00151,99.txt", "34");
		put("00318_00065,00_00151,99.txt", "41");
		put("00322_00065,00_00151,99.txt", "42");
		put("00342_00065,00_00151,99.txt", "44");
		put("00346_00065,00_00151,99.txt", "45");

		/* NPH
		put("00014_00065,00_00151,99.txt", "01");
		put("00020_00064,01_00151,99.txt", "02");
		put("00058_00065,00_00151,99.txt", "11");
		put("00084_00065,00_00151,99.txt", "12");
		put("00106_00065,00_00151,99.txt", "13");
		put("00134_00064,81_00151,99.txt", "14");
		put("00156_00065,00_00151,99.txt", "15");
		put("00190_00065,00_00151,99.txt", "21");
		put("00216_00065,00_00151,99.txt", "22");
		put("00240_00065,00_00151,99.txt", "23");
		put("00270_00065,00_00151,99.txt", "31");
		put("00290_00065,00_00151,99.txt", "32");
		put("00312_00065,00_00151,99.txt", "33");
		put("00328_00065,00_00151,99.txt", "34");
		put("00344_00065,00_00151,99.txt", "41");
		put("00348_00065,00_00151,99.txt", "42");
		put("00370_00065,00_00151,99.txt", "44");
		put("00374_00065,00_00151,99.txt", "45");
		*/
	}};
	
	public static String BR = "<br/>";

	private Pattern sameStartAndEnd = Pattern.compile("^<([bi])>(.*)</\\1>$");
	private Pattern hasCharStyle = Pattern.compile("<[bi]>");
	private Pattern emptyStyle = Pattern.compile("<([bi])>(\\s*)</\\1>");
	private Pattern spaceBeforeClosingTag = Pattern.compile("(\\s+)((</[bi]>)+)");
	private Pattern styleOffOn = Pattern.compile("</([bi])>(\\s*)<\\1>");
	private Pattern startsWithNum = Pattern.compile("^\\d+\\.");
	private Pattern multiWhite = Pattern.compile("[ \\t]{2,}");

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
	private boolean verseBreak;
	private boolean caps;
	
	// notes
	private boolean supScr;
	private StringBuilder supScrBuffer = new StringBuilder();
	private boolean inFootnote;
	private int fnCounter = 0;
	private int currentFootnoteStart;
	private StringBuilder footnoteBuffer = new StringBuilder();
	private String forwardRefPrefix;
	private String backRefPrefix;
	private Map<String, String> endnoteFileNumMap = new HashMap<>();
	private Map<Integer, String> endnoteLineToBackRefPrefixMap = new HashMap<>();
	private String prevBackRefPrefix;
	private int sameAnswerCounter;


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
		readEndNoteBackRefFile();
		
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
		flowLevel = 0;
		text = new StringBuilder[10];
		filePageNum = fileName.indexOf('_');
		if(filePageNum > -1)
			filePageNum = Integer.valueOf(fileName.substring(0, filePageNum));
		if(filePageNum != prevFilePageNum)
		{
			if((filePageNum-prevFilePageNum > 5 || forceNewFile.contains(filePageNum))
				&& !noNewFile.contains(filePageNum))
					startChapter();
			else
			{
				newPara("UjOldal");
				para.text.append("<!-- page break -->");
			}
		}
		String pref = supPrefix.get(fileName);
		if(pref != null)
		{
			forwardRefPrefix = "f"+pref;
			backRefPrefix = "b"+forwardRefPrefix;
		}
		else
			forwardRefPrefix = null;
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
		writeEndNoteBackRefFile();

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
		destName = destName.substring(destName.length()-chapterDigits);
		destFile = new File(destDir.getAbsolutePath() + "/" + destName + ".xml");
		writerInfo.xmlFile = destFile;
		writerInfo.fileNames.add(destFile.getName());
	}


	private void endChapter() throws IOException
	{
		if(footnoteBuffer.length() > 0)
		{
			chapter.footnotes = footnoteBuffer.toString();
			footnoteBuffer.setLength(0);
			fnCounter = 0;
		}
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
		caps = false;
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
			endWord(13);
			purgeFormatStack();
			cleanupPara();
			styleMap.convertStylename(para, scanner);
			if(para.text.length() == 0)
			{
				++emptyRowsBefore;
				if(prev != null && prev.cls == ParagraphClass.TorzsVers)
					verseBreak = true;
				return false;
			}
			if(para.cls == ParagraphClass.TorzsVers)
			{
				if(prev != null && prev.cls == ParagraphClass.TorzsVers && !verseBreak)
				{
					++torzsVersLineNum;
					prev.text.append(BR);
					if(torzsVersLineNum % 2 == 0)
						prev.text.append("\u2002\u2002");
					prev.text.append(para.text);
					para.text.setLength(0);
					return false;
				}
				else
					torzsVersLineNum = 1;
			}
			if(para.cls == ParagraphClass.KozepenVers)
			{
				if(prev != null && prev.cls == ParagraphClass.KozepenVers && !verseBreak)
				{
					prev.text.append(BR);
					prev.text.append(para.text);
					para.text.setLength(0);
					return false;
				}
			}
			verseBreak = false;
			if(prev != null && prev.cls == ParagraphClass.TorzsVers)
			{
				int ix = prev.text.indexOf(BR+"\u2002\u2002");
				if(ix > -1 && prev.text.indexOf(BR, ix+BR.length()) == -1)
				{
					// two-line sloka: remove 2nd line indent
					prev.text.replace(ix+BR.length(), ix+BR.length()+2, "");
				}
			}
			if(!skipFootnotes && endNoteFileName.equals(destName))
			{
				if(startsWithNum.matcher(para.text).find())
				{
					if(para.text.length()>25 || 
						(para.text.indexOf("üggelék")==-1 && para.text.indexOf("ÜGGELÉK")==-1))
					{
						endSuperscript(para.text);
					}
				}
			}
		}
		emptyRowsBefore = 0;
		return true;
	}


	private boolean endWord(int c)
	{
		int len = wordBuffer.length();
		if(len > 0)
		{
			if(!skipHyphens && len > 3)
			{
				if(c==' ' && wordBuffer.charAt(len-1)=='-' && wordBuffer.charAt(len-2)=='÷')
					return false;
				revHyphen.check(wordBuffer, para.text, scanner);
			}
			wordBuffer.setLength(0);
		}
		return true;
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
		boolean escape = false;
		for(int pos = 0; pos < line.length(); ++pos)
		{
			char c = line.charAt(pos);
			boolean skip = false;
			if(c == '\\' && !escape)
			{
				escape = true;
				continue;
			}
			if(c == '<' && !escape)
			{
				if(flowLevel == 0)
				{
					++textLevel;
					skip = true;
				}
				++flowLevel;
			}
			else if(c == '>' && !escape)
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
			escape = false;
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
			if(inFootnote)
				return;
			charMaps.selectFont(CharacterMapManager.DEFAULT_STYLENAME);
			String styleName = processLevel(tag.substring(10));
			charMaps.selectFont(CharacterMapManager.DEFAULT_TEXT);
			newPara(styleName);
		}
		else if(tag.startsWith("DefineParaStyle:"))
		{
			if(inFootnote)
				return;
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
					if(tag.endsWith(":Italic") || tag.endsWith(": Italic"))
						style.italic = true;
					else if(tag.endsWith(":Bold") || tag.endsWith(": Bold"))
						style.bold = true;
					else if(tag.endsWith(":Bold Italic") || tag.endsWith(":BoldItalic"))
					{
						style.bold = true;
						style.italic = true;
					}
					else if(!isNormalFont(tag))
					{
						pos();
						throw new RuntimeException("Unknown typeface: " + tag);
					}
					return;
				}
				// add inline style
				if(isNormalFont(tag))
					purgeFormatStack();
				else if(tag.endsWith(":Italic") || tag.endsWith(": Italic"))
				{
					boolean psp = paraStartPending;
					addChars("<i>");
					paraStartPending = psp;
					formatStack.push("</i>");
				}
				else if(tag.endsWith(":Bold") || tag.endsWith(": Bold"))
				{
					addChars("<b>");
					formatStack.push("</b>");
				}
				else if(tag.endsWith(":Bold Italic") || tag.endsWith(":BoldItalic"))
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
			else if(tag.startsWith("CharStyle:"))
			{
				charMaps.selectFont(CharacterMapManager.DEFAULT_STYLENAME);
				String styleName = processLevel(tag.substring(10));
				if(defineParaStyle != null || para == null || para.style == null || para.style.font == null)
					charMaps.selectFont(CharacterMapManager.DEFAULT_TEXT);
				else
					charMaps.selectFont(para.style.font);
				if("dolt".equals(styleName))
				{
					boolean psp = paraStartPending;
					addChars("<i>");
					paraStartPending = psp;
					formatStack.push("</i>");
				}
				else if(styleName.isEmpty())
					purgeFormatStack();
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
			else if(tag.startsWith("cCase:"))
			{
				if(textLevel==0 && (tag.endsWith(":All Caps") || tag.endsWith(":Small Caps")))
					caps = true;
				else if(tag.equals("cCase:"))
					caps = false;
			}
			else if(tag.startsWith("cPosition:"))
			{
				if(tag.endsWith(":Superscript"))
				{
					supScr = true;
					supScrBuffer.setLength(0);
				}
				else if(supScr && tag.endsWith("cPosition:"))
					endSuperscript(null);
			}
			else if(tag.startsWith("FootnoteStart:"))
			{
				inFootnote = true;
				currentFootnoteStart = text[0].length();
			}
			else if(tag.startsWith("FootnoteEnd:"))
			{
				endSuperscript(null);
				inFootnote = false;
			}
			else if(inFootnote)
				return;
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


	private boolean isNormalFont(String tag)
	{
		return tag.endsWith(":") || tag.endsWith(":Normal")
			|| tag.endsWith(":Roman") || tag.endsWith(":Regular");
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
			if(isWhite(c))
				return;
			if(para != null)
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
				System.out.println(para.text + "..." + c + " / " + para.style.font + " / " + cmap.cmapFile.getName());
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
			if(sb.length() == 0)
				c = '÷';
			else
			{
				char prev = sb.charAt(sb.length()-1);
				if(prev != ' ')
					c = ' ';
				else
					return;
			}
		}
		if(textLevel == 0)
		{
			boolean wordChar = Character.isAlphabetic(c)
				|| c>=256 && c<400 || c>7600 && c<7800 || c=='-' || c=='÷';
			if(wordChar || !endWord(c)) {
				if(!removeOptionalHyphens || c != '÷')
					wordBuffer.append((char) c);
			}
			if(supScr && !inFootnote)
			{
				if(c >= '*' && c <= '9')
				{
					supScrBuffer.append((char) c);
					return;
				}
				else if(supScrBuffer.length() > 0)
					endSuperscript(null);
				else
				{
					System.out.print("False superscript at ");
					pos();
					supScr = false;
				}
			}
			if(caps)
				c = Character.toUpperCase(c);
		}
		if(textLevel == 0 && removeOptionalHyphens && c == '÷')
			return;
		text[textLevel].append((char) c);
	}


	private void addChars(String s)
	{
		for(int i=0, len=s.length(); i<len; ++i)
			addChar(s.charAt(i));
	}


	private String styleFont(ParagraphStyle style)
	{
		while(style != null)
		{
			if(style.font != null)
				return style.font;
			if(style.basedOn != null)
				style = styleSheet.get(style.basedOn);
			else
				break;
		}
		return CharacterMapManager.DEFAULT_TEXT;
	}


	private void endSuperscript(StringBuilder paraBuf)
	{
		supScr = false;
		if(skipFootnotes)
			return;
		String sup;
		int dotIx = -1;
		if(inFootnote)
		{
			sup = "" + (++fnCounter);
			footnoteBuffer.append("    <p class=\"Labjegyzet\"><a id=\"f")
				.append(sup)
				.append("\" /><a href=\"#bf")
				.append(sup)
				.append("\">[")
				.append(sup)
				.append("]</a>")
				.append(para.text.substring(currentFootnoteStart))
				.append("</p>\r\n");
			para.text.setLength(currentFootnoteStart);
		}
		else if(paraBuf != null)
		{
			dotIx = paraBuf.indexOf(".");
			sup = paraBuf.substring(0, dotIx);
			text[0] = new StringBuilder();
		}
		else
			sup = supScrBuffer.toString();

		if(sup.isEmpty())
			return;

		// precede [ with space if needed
		if(text[0].length()>0 && !isWhite(text[0].charAt(text[0].length()-1)))
			text[0].append(' ');
		text[0].append("<a id=\"");
		if(inFootnote)
			text[0].append("bf").append(sup);
		else if(forwardRefPrefix != null)
		{
			text[0].append(backRefPrefix).append(sup);
			endnoteFileNumMap.put(backRefPrefix+sup, destName);
		}
		else
		{
			getSupPrefix();
			text[0].append(backRefPrefix.substring(1)).append(sup);
		}
		text[0].append("\" /><a href=\"");
		if(inFootnote)
			text[0].append("#f").append(sup);
		else if(forwardRefPrefix != null)
			text[0].append(endNoteFileName).append(".html#").append(forwardRefPrefix).append(sup);
		else
		{
			String fileNum = endnoteFileNumMap.get(backRefPrefix+sup);
			text[0].append(fileNum)
				.append(".html#")
				.append(backRefPrefix)
				.append(sup);
		}
		text[0].append("\">[").append(sup).append("]</a>");
		if(paraBuf != null)
		{
			text[0].append(paraBuf.substring(dotIx+1));
			para.text = text[0];
		}
	}


	private void getSupPrefix()
	{
		backRefPrefix = endnoteLineToBackRefPrefixMap.get(filePageNum*10000+lineNumber);
		if(backRefPrefix == null)
		{
			if(sameAnswerCounter > 0)
			{
				--sameAnswerCounter;
				backRefPrefix = prevBackRefPrefix;
			}
			else
			{
				System.out.println("Notes section after: "
					+ chapter.para.get(chapter.para.size()-2).text);
				//String s = scanner.next();
				String s = "99";
				if("X".equals(s))
					throw new RuntimeException("Notes input aborted.");
				else if("-".equals(s))
					backRefPrefix = prevBackRefPrefix;
				else if(s.startsWith("-"))
				{
					sameAnswerCounter = Integer.valueOf(s.substring(1));
					backRefPrefix = prevBackRefPrefix;
				}
				else
					backRefPrefix = prevBackRefPrefix = "bf" + s;
			}
			endnoteLineToBackRefPrefixMap.put(filePageNum*10000+lineNumber, backRefPrefix);
		}
	}
	
	
	private void readEndNoteBackRefFile()
	{
		File f = new File(destDir, "endNoteBackRef.ser");
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f)))
		{
			endnoteLineToBackRefPrefixMap = (Map<Integer, String>) ois.readObject();
		}
		catch(Exception ex)
		{
			endnoteLineToBackRefPrefixMap = new HashMap<>();
		}
	}
	
	
	private void writeEndNoteBackRefFile()
	{
		File f = new File(destDir, "endNoteBackRef.ser");
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f)))
		{
			oos.writeObject(endnoteLineToBackRefPrefixMap);
		}
		catch(Exception ex)
		{
		}
	}


	private boolean isWhite(int c)
	{
		return c==' ' || c>=8192 && c<=8207 || c=='\t' || c==160;
	}


	private void purgeFormatStack()
	{
		endWord(0);
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
		txt2 = multiWhite(txt);
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
				if(markStrippedFormatting)
					txt = "÷:" + m.group(1) + '÷' + m.replaceFirst(m.group(2));
				else
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


	public String multiWhite(String txt)
	{
		Matcher m = multiWhite.matcher(txt);
		String txt2 = m.replaceAll(" ");
		if(txt == txt2)
			return null;
		return txt2;
	}
		

	private void pos()
	{
		System.out.println("["+writerInfo.srcFile.getName()+":"+lineNumber+":"+writerInfo.pos+", "+charMaps.selectedFont+"]");
	}
}
