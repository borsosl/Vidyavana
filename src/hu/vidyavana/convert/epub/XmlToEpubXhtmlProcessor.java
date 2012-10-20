package hu.vidyavana.convert.epub;

import hu.vidyavana.convert.api.FileProcessor;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class XmlToEpubXhtmlProcessor implements FileProcessor
{
	public static Pattern TAG_NAME = Pattern.compile("^\\s*</?([^ >]+)");
	public static Pattern TEXT_NUMBER = Pattern.compile("<text_number>(.*)</text_number>");
	public static Pattern CHAPTER = Pattern.compile("<p class=\"Fejezetszam\">(.*)</p>");
	public static Pattern CHAPTER_TITLE = Pattern.compile("<p class=\"Fejezetcim\">(.*)</p>");
	public static Pattern SECTION = Pattern.compile("<p class=\"Alcim\">(.*)</p>");
	public static Pattern TEXT = Pattern.compile("<p class=\"Versszam\">(.*)</p>");
	public static Pattern BR = Pattern.compile("\\s*<br\\s*/>\\s*");
	public static Pattern B = Pattern.compile("</?b>");
	
	
	enum Level
	{
		Document, Chapter, Section, Text;
	}
	
	private File destDir;
	private StringBuilder manifest;
	private StringBuilder spine;
	private StringBuilder navMap;
	private int navPointCount;
	private String chapter;
	private Stack<Level> levelStack;
	private int maxNavigationLevel;
	private int sectionCount;


	@Override
	public void init(File srcDir, File destDir)
	{
		this.destDir = destDir;
		manifest = new StringBuilder();
		spine = new StringBuilder();
		navMap = new StringBuilder();
		navPointCount = 0;
		maxNavigationLevel = 0;
	}


	@Override
	public void process(File srcFile, String fileName) throws Exception
	{
		srcFile = new File(destDir.getAbsolutePath() + "/" + fileName + ".xml");
		int dot = fileName.lastIndexOf('.');
		if(dot > -1)
			fileName = fileName.substring(0, dot);
		String id = fileName;
		if(id.charAt(0)>='0' && id.charAt(0)<='9')
			id = "_"+id;
		File destFile = new File(destDir.getAbsolutePath() + "/" + fileName + ".html");
		manifest.append("    <item id=\"")
			.append(id)
			.append("\" href=\"")
			.append(fileName)
			.append(".html\" media-type=\"application/xhtml+xml\"/>\r\n");
		spine.append("    <itemref idref=\"")
			.append(id)
			.append("\"/>\r\n");
		process(srcFile, destFile, fileName);
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
		f.close();
	}


	private void process(File srcFile, File destFile, String fileName) throws Exception
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile), "UTF-8"));
		Writer out = new OutputStreamWriter(new FileOutputStream(destFile), "UTF-8");
		
		out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n");
		out.write("  <head>\r\n");
		out.write("    <title></title>\r\n");
		out.write("    <meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\" />\r\n");
		out.write("    <link type=\"text/css\" rel=\"stylesheet\" media=\"all\" href=\"stylesheet.css\" />\r\n");
		out.write("  </head>\r\n");
		out.write("  <body>\r\n");
		
		chapter = null;
		levelStack = new Stack<>();
		levelStack.push(Level.Document);
		sectionCount = 0;
		String tagName = null;
		String[] htmlTags = {"p", "b", "i"};
		String textHash = "";
		while(true)
		{
			String line = in.readLine();
			if(line == null)
				break;
			Matcher m = TAG_NAME.matcher(line);
			if(m.find())
				tagName = m.group(1);
			
			for(String tag: htmlTags)
				if(tag.equals(tagName))
				{
					if("p".equals(tag))
						addNavigation(line, fileName, textHash, out);

					out.write(line);
					out.write("\r\n");
					break;
				}
			if("text_number".equals(tagName))
			{
				m = TEXT_NUMBER.matcher(line);
				if(m.find())
				{
					// create hash for texts
					textHash = "t"+m.group(1);
					out.write("    <div class=\"Ref\"><a id=\"");
					out.write(textHash);
					out.write("\"></a></div>\r\n");
				}
			}
		}
		
		while(levelStack.size() > 1)
			popNavigationLevel();
		
		out.write("  </body>\r\n");
		out.write("</html>\r\n");
		
		in.close();
		out.close();
	}


	private void addNavigation(String line, String fileName, String textHash, Writer out) throws Exception
	{
		Matcher m = CHAPTER.matcher(line);
		if(m.find())
		{
			chapter = m.group(1);
			return;
		}
		m = CHAPTER_TITLE.matcher(line);
		if(m.find())
		{
			String title = inline(m.group(1));
			addToNavMap(Level.Chapter, chapter==null ? title : chapter+" – "+title, fileName+".html");
			return;
		}
		m = SECTION.matcher(line);
		if(m.find())
		{
			String sectionStr = "s"+(++sectionCount);
			out.write("    <div class=\"Ref\"><a id=\"");
			out.write(sectionStr);
			out.write("\"></a></div>\r\n");
			String sectionTitle = inline(m.group(1));
			addToNavMap(Level.Section, sectionTitle, fileName+".html#"+sectionStr);
			return;
		}
		m = TEXT.matcher(line);
		if(m.find())
		{
			String text = inline(m.group(1));
			addToNavMap(Level.Text, text, fileName+".html#"+textHash);
			return;
		}
	}


	private void addToNavMap(Level newLevel, String title, String uri)
	{
		while(levelStack.peek().ordinal() >= newLevel.ordinal())
			popNavigationLevel();
		
		int indentLevel = levelStack.size()-1;
		char[] spaceArr = indentSpaces(indentLevel);
		levelStack.push(newLevel);
		if(indentLevel+1 > maxNavigationLevel)
			maxNavigationLevel = indentLevel + 1;
		
		// remove explicit <b> tags 
		title = B.matcher(title).replaceAll("");
		
		++navPointCount;
		navMap.append(spaceArr)
			.append("<navPoint id=\"p")
			.append(navPointCount)
			.append("\" playOrder=\"")
			.append(navPointCount)
			.append("\">\r\n")
			.append(spaceArr)
			.append("  <navLabel><text>")
			.append(title)
			.append("</text></navLabel>\r\n")
			.append(spaceArr)
			.append("  <content src=\"")
			.append(uri)
			.append("\"/>\r\n");
	}


	private void popNavigationLevel()
	{
		levelStack.pop();
		char[] spaceArr = indentSpaces(levelStack.size()-1);
		navMap.append(spaceArr).append("</navPoint>\r\n");
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
