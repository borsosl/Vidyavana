package hu.vidyavana.convert.epub;

import hu.vidyavana.convert.api.FileProcessor;
import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.util.XmlUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlToEpubXhtmlProcessor implements FileProcessor
{
	public int FILE_LENGTH_GOAL = 50_000;
	public boolean newFileAtSectionOnly = false;
	public String[] breakOnlyFiles = {};

	public static Pattern TAG_NAME = Pattern.compile("^\\s*</?([^ >/]+)");
	public static Pattern NEW_FILE_CLASS = Pattern.compile("\"(Versszam|Szakaszcim|SzakaszcimBalra)\"");
	public static Pattern TEXT_NUMBER = Pattern.compile("<text_number>(.*)</text_number>");
	public static Pattern DIVISION_STYLES = Pattern.compile("<p class=\"(Fejezetszam|Fejezetcim|Szakaszcim|SzakaszcimBalra|Alcim|Versszam|Konyvcim)\">(.*)</p>");
	public static Pattern ASTERISK = Pattern.compile("<p class=\"Asterisk\">(.*)</p>");
	public static Pattern VERSE_BLOCK = Pattern.compile("class=\"(Uvaca|Vers|TorzsUvaca|TorzsVers|TorzsVersKozep|Hivatkozas)\"");
	public static Pattern BR = Pattern.compile("\\s*<br\\s*/>\\s*");
	public static Pattern INDENT = Pattern.compile("^\\s*");
	
	
	enum Level
	{
		Document, Chapter, Section, Text;
	}
	
	private File destDir;
	private StringBuilder manifest;
	private StringBuilder spine;
	private StringBuilder navMap;
	private StringBuilder longContent;
	private StringBuilder panditContent;
	private int paraOrdinal;
	private int blockStartParaOrdinal;
	private int navPointCount;
	private String chapterNum;
	private Stack<Level> levelStack;
	private int maxNavigationLevel;
	private int sectionCount;
	private int genHash;
	private String currentBaseFileName;
	private String currentFileName;
	private int currentFileIndex;
	private boolean thisFileBreakable;
	private boolean prevParaAsterisk;
	private int fileTextLength;
	private Level prevNavLevel;


	@Override
	public void init(File srcDir, File destDir)
	{
		this.destDir = destDir;
		manifest = new StringBuilder();
		spine = new StringBuilder();
		navMap = new StringBuilder();
		longContent = new StringBuilder();
		panditContent = new StringBuilder();
		paraOrdinal = 0;
		blockStartParaOrdinal = -10;
		navPointCount = 0;
		maxNavigationLevel = 0;
		genHash = 0;
		thisFileBreakable = breakOnlyFiles.length == 0;
	}


	@Override
	public void process(File srcFile, String fileName) throws Exception
	{
		srcFile = new File(destDir.getAbsolutePath() + "/" + fileName + ".xml");
		int dot = fileName.lastIndexOf('.');
		if(dot > -1)
			fileName = fileName.substring(0, dot);
		currentBaseFileName = fileName;
		currentFileIndex = -1;
		if(breakOnlyFiles.length > 0)
		{
			thisFileBreakable = false;
			for(String f : breakOnlyFiles)
				if(f.equals(currentBaseFileName))
				{
					thisFileBreakable = true;
					break;
				}
		}
		process(srcFile, startFile());
	}


	@Override
	public void finish() throws Exception
	{
		File content = new File(destDir.getAbsolutePath() + "/" + "content.txt");
		FileWriter f = new FileWriter(content);
		f.write(manifest.toString());
		f.write("------------\r\n");
		f.write(spine.toString());
		f.write("------------\r\n");
		f.write(navMap.toString());
		f.write("------------\r\n");
		f.write(maxNavigationLevel+"\r\n");
		f.write("------------\r\n");
		f.write(longContent.toString()+"\r\n");
		f.write("------------\r\n");
		f.write(panditContent.toString()+"\r\n");
		f.close();
	}


	private Writer startFile() throws Exception
	{
		++currentFileIndex;
		currentFileName = currentBaseFileName + (currentFileIndex>0 ? "_"+currentFileIndex : "");
		String id = currentFileName;
		if(id.charAt(0)>='0' && id.charAt(0)<='9')
			id = "_"+id;
		File destFile = new File(destDir.getAbsolutePath() + "/" + currentFileName + ".html");
		manifest.append("    <item id=\"")
			.append(id)
			.append("\" href=\"")
			.append(currentFileName)
			.append(".html\" media-type=\"application/xhtml+xml\"/>\r\n");
		spine.append("    <itemref idref=\"")
			.append(id)
			.append("\"/>\r\n");

		Writer out = new OutputStreamWriter(new FileOutputStream(destFile), "UTF-8");
		out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n");
		out.write("  <head>\r\n");
		out.write("    <title></title>\r\n");
		out.write("    <meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\" />\r\n");
		out.write("    <link type=\"text/css\" rel=\"stylesheet\" media=\"all\" href=\"stylesheet.css\" />\r\n");
		out.write("  </head>\r\n");
		out.write("  <body>\r\n");
		
		fileTextLength = 0;
		prevParaAsterisk = false;
		return out;
	}


	private void closeFile(Writer out) throws IOException
	{
		out.write("  </body>\r\n");
		out.write("</html>\r\n");
		out.close();
	}
	
	
	private Writer nextFile(Writer out, String line) throws Exception
	{
		if(!thisFileBreakable)
			return out;
		boolean force = !newFileAtSectionOnly && fileTextLength > FILE_LENGTH_GOAL * 3 / 2;
		if(force)
			line = null;
		
		// refs have a smaller threshold not to fit as the last line, with text flowing to next file
		if(force || fileTextLength > FILE_LENGTH_GOAL-(line==null ? 200 : 0))
		{
			Matcher m = null;
			if(line != null)
				m = NEW_FILE_CLASS.matcher(line);
			if(line == null || m.find())
			{
				closeFile(out);
				out = startFile();
			}
		}
		return out;
	}


	private void process(File srcFile, Writer out) throws Exception
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile), "UTF-8"));

		chapterNum = null;
		levelStack = new Stack<>();
		levelStack.push(Level.Document);
		sectionCount = 0;
		String tagName = null;
		String[] htmlTags = {"p", "b", "i", "br", "div", "img", "table", "tr", "td", "a"};
		boolean tagLine;
		boolean writeCurrentTag = false;
		boolean verseBlock=false;
		ArrayList<String> verseBuffer = new ArrayList<>();
		String textHash = "";
		int paraSinceTextHash = 10;
		while(true)
		{
			String line = in.readLine();
			if(line == null)
				break;
			tagLine = false;
			Matcher m = TAG_NAME.matcher(line);
			if(m.find())
			{
				tagLine = true;
				tagName = m.group(1);
				writeCurrentTag = false;
				for(String tag : htmlTags)
					if(tag.equals(tagName))
					{
						if("p".equals(tag))
						{
							++paraOrdinal;
							++paraSinceTextHash;
							out = addNavigation(line, textHash, paraSinceTextHash, out);
							verseBlock = VERSE_BLOCK.matcher(line).find();
							if(verseBlock && verseBuffer.isEmpty() && line.indexOf("class=\"Hivatkozas") > -1)
								verseBlock = false;
							out = nextFile(out, line);
						}
						writeCurrentTag = true;
						break;
					}
			}
			
			if(writeCurrentTag)
			{
				String indent = null;
				if(tagLine)
				{
					if(!verseBlock && verseBuffer.size()>0)
					{
						m = INDENT.matcher(verseBuffer.get(0));
						m.find();
						indent = m.group(); 
						out.write(indent);
						out.write("<div class=\"VsWrap1\"><div class=\"VsWrap2\">\r\n");

						for(String v : verseBuffer)
						{
							if(indent != null)
								out.write("  ");
							out.write(v);
							out.write("\r\n");
							fileTextLength += v.length();
						}
						verseBuffer.clear();
						out.write(indent);
						out.write("</div></div>\r\n");
					}
				}
				if(verseBlock)
					verseBuffer.add(line);
				else
				{
					out.write(line);
					out.write("\r\n");
					fileTextLength += line.length();
				}
			}
			if("text_number".equals(tagName))
			{
				m = TEXT_NUMBER.matcher(line);
				if(m.find())
				{
					out = nextFile(out, null);
					// create hash for texts
					textHash = "t"+m.group(1);
					out.write("    <div class=\"Ref\"><a id=\"");
					out.write(textHash);
					out.write("\"></a></div>\r\n");
					paraSinceTextHash = 0;
				}
			}
		}
		
		while(levelStack.size() > 1)
			popNavigationLevel(null);
		
		closeFile(out);
		in.close();
	}


	private Writer addNavigation(String line, String textHash, int paraSinceTextHash, Writer out) throws Exception
	{
		if(prevParaAsterisk) {
			out = nextFile(out, null);
			prevParaAsterisk = false;
		}
		Matcher m = DIVISION_STYLES.matcher(line);
		if(m.find()) {
			ParagraphClass cls = ParagraphClass.valueOf(m.group(1));
			String title = XmlUtil.noMarkup(inline(m.group(2).trim()));
			if(cls == ParagraphClass.Konyvcim)
				blockStartParaOrdinal = paraOrdinal;
			else if(cls == ParagraphClass.Fejezetszam) {
				chapterNum = title;
				if(paraOrdinal - blockStartParaOrdinal > 3)
					blockStartParaOrdinal = paraOrdinal;
				return out;
			}
			else if(cls == ParagraphClass.Fejezetcim)
			{
				addToNavMap(Level.Chapter, chapterNum, title, currentFileName+".html");
				chapterNum = null;
				return out;
			}
			else if(cls == ParagraphClass.Szakaszcim || cls == ParagraphClass.SzakaszcimBalra
					|| cls == ParagraphClass.Alcim) {
				if(chapterNum != null)
					addToNavMap(Level.Chapter, null, chapterNum, currentFileName+".html");
				out = nextFile(out, null);
				String sectionStr = "s"+(++sectionCount);
				out.write("    <div class=\"Ref\"><a id=\"");
				out.write(sectionStr);
				out.write("\"></a></div>\r\n");
				String sectionTitle = inline(m.group(2));
				addToNavMap(Level.Section, null, sectionTitle, currentFileName+".html#"+sectionStr);
				return out;
			}
			else if(cls == ParagraphClass.Versszam)
			{
				if(chapterNum != null)
					addToNavMap(Level.Chapter, null, chapterNum, currentFileName+".html");
				String text = inline(m.group(1));
				// if the @textno is too far back to belong to this @text
				if(paraSinceTextHash > 2)
				{
					out = nextFile(out, null);
					++genHash;
					textHash = "gen" + genHash;
					out.write("    <div class=\"Ref\"><a id=\"");
					out.write(textHash);
					out.write("\"></a></div>\r\n");
				}
				addToNavMap(Level.Text, null, text, currentFileName+".html#"+textHash);
				return out;
			}
		}
		m = ASTERISK.matcher(line);
		if(m.find())
			prevParaAsterisk = true;
		return out;
	}


	private void addToNavMap(Level newLevel, String chapter, String title, String uri)
	{
		while(levelStack.peek().ordinal() >= newLevel.ordinal())
			popNavigationLevel(newLevel);
		
		int indentLevel = levelStack.size()-1;
		char[] spaceArr = indentSpaces(indentLevel);
		levelStack.push(newLevel);
		
		// remove explicit <b> tags
		String printTitle = chapter == null ? title : chapter + " – " + title;
		String tocTitle = chapter == null ? title : chapter + "ǀ" + title;	// 01c0

		boolean heading = newLevel != Level.Text;
		boolean gap = false;
		if(heading)
		{
			if(indentLevel+1 > maxNavigationLevel)
				maxNavigationLevel = indentLevel + 1;
			
			++navPointCount;
			navMap.append(spaceArr)
				.append("<navPoint id=\"p")
				.append(navPointCount)
				.append("\" playOrder=\"")
				.append(navPointCount)
				.append("\">\r\n")
				.append(spaceArr)
				.append("  <navLabel><text>")
				.append(printTitle)
				.append("</text></navLabel>\r\n")
				.append(spaceArr)
				.append("  <content src=\"")
				.append(uri)
				.append("\"/>\r\n");
			panditContent.append("    <entry>\r\n")
					.append("      <level>")
					.append(indentLevel+1)
					.append("</level>\r\n")
					.append("      <title>")
					.append(tocTitle)
					.append("</title>\r\n")
					.append("      <para_ordinal>")
					.append(blockStartParaOrdinal == -10 ? paraOrdinal : blockStartParaOrdinal)
					.append("</para_ordinal>\r\n")
					.append("    </entry>\r\n");
			blockStartParaOrdinal = -10;
		}
		else
		{
			int ix = printTitle.lastIndexOf('.');
			if(ix > -1)
				printTitle = printTitle.substring(0, ix+1);
			gap = true;
		}

		int in = levelStack.size()-2;
		longContent.append(spaceArr);
		if(heading || prevNavLevel != Level.Text)
			longContent.append("<div class=\"")
				.append("ct")
				.append(in)
				.append("\">\r\n")
				.append(spaceArr);
		longContent.append("  <a href=\"")
			.append(uri)
			.append("\">")
			.append(printTitle)
			.append("</a>");
		if(gap)
			longContent.append("\u2002\u2002");
		longContent.append("\r\n");

		prevNavLevel = newLevel;
	}


	private void popNavigationLevel(Level newLevel)
	{
		Level popLevel = levelStack.pop();
		char[] spaceArr = indentSpaces(levelStack.size()-1);
		if(popLevel != Level.Text)
			navMap.append(spaceArr).append("</navPoint>\r\n");
		if(newLevel != Level.Text || prevNavLevel != Level.Text)
			longContent.append(spaceArr).append("</div>\r\n");
	}


	private char[] indentSpaces(int level)
	{
		char[] spaceArr = new char[4+level*2];
		Arrays.fill(spaceArr, ' ');
		return spaceArr;
	}


	private String inline(String text)
	{
		return BR.matcher(text).replaceAll(" ");
	}

}
