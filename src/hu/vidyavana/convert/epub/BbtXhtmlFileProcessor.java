package hu.vidyavana.convert.epub;

import hu.vidyavana.convert.api.*;
import hu.vidyavana.convert.api.WriterInfo.SpecialFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.vidyavana.convert.api.ParagraphClass.*;
import static hu.vidyavana.convert.epub.BbtXhtmlFileProcessor.Ebook.BBD;
import static hu.vidyavana.convert.epub.BbtXhtmlFileProcessor.Ebook.OWK;
import static hu.vidyavana.convert.epub.BbtXhtmlFileProcessor.Ebook.SSR;
import static hu.vidyavana.convert.epub.BbtXhtmlFileProcessor.StyleNameMapping.m;

public class BbtXhtmlFileProcessor implements FileProcessor
{
	enum Ebook { SSR, BBD, OWK }
	private Ebook ebook = BBD;

	static class StyleNameMapping {
		String css;
		ParagraphClass cls;

		static StyleNameMapping m(String css, ParagraphClass cls) {
			StyleNameMapping map = new StyleNameMapping();
			map.css = css;
			map.cls = cls;
			return map;
		}
	}

	StyleNameMapping[] inddStyleNameMappings = {
			m("translation", Forditas),
			m("speech", BalraKoveto),
			m("text-number", TorzsVersszam),
			m("poem-number", TorzsVersszam),
			m("verse-uvaca", TorzsUvaca),
			m("verse-anustubh", TorzsVers),
			m("asterism", Csillagok),
			m("reference", Hivatkozas),
			m("signature", Balra),
			m("poem", NemDoltVers),
			m("footnote-body", Labjegyzet)
	};

	StyleNameMapping[] edStyleNameMappings = {
			m("para", TorzsKoveto),
			m("purp", TorzsKezdet),
			m("verse-in-purp", TorzsVers),
			m("ch", Fejezetszam),
			m("ch-title", Fejezetcim),
			m("sanskrit", Vers)
	};

	StyleNameMapping[] styleNameMappings = ebook == SSR ? inddStyleNameMappings : edStyleNameMappings;

	private static final Pattern FILENAME_CHAPTER = Pattern.compile("(\\d\\d)(\\D\\D)");
	private static final Pattern BODY_LINE = Pattern.compile("^\\s*<body(.*?)>");
	private static final Pattern HTML_TAG_LINE = Pattern.compile("^\\s*(<(p|div|h\\d|ol|ul|li)(.*?)>)?(.*?)(</(p|div|h\\d|body|ol|ul|li)>)?\\s*$");
	private static final Pattern HTML_ATTR = Pattern.compile("([a-zA-Z-]+)\\s*=\\s*\"(.*?)\"");
	private static final Pattern WHITE_SPLITTER = Pattern.compile("\\s+");
	private static final Pattern CHAPTER_PAGE_BODY_ID = Pattern.compile("s\\d+");

	private File destDir;
	private String srcFileName;
	private WriterInfo writerInfo;
	private List<String> manual;
	private boolean bbdOwk = ebook == BBD || ebook == OWK;

	private int fileNameChapter, lineNumber;
	private String fileNameClass;
	private Book book;
	private Chapter chapter;
	private Paragraph para, prevPara;
	private Stack<String> formatStack;
	private File tocFile;

	private boolean inBody, inVerse;
	private XhtmlTagInfo body, tag;
	private boolean coverPageFile;
	private boolean white;
	private int h2count;
	private Stack<XhtmlTagInfo> tagStack = new Stack<>();
	private StringBuilder ncsb = new StringBuilder(10000);


	@Override
	public void init(File srcDir, File destDir)
	{
		this.destDir = destDir;
		writerInfo = new WriterInfo();
		writerInfo.fileNames = new ArrayList<>();
		manual = new ArrayList<>();

		if(!writerInfo.forEbook)
		{
			// xml TOC file
			try
			{
				if(ebook == SSR) {
					writerInfo.initNumberedAbbrevTocLevels(2);
				}

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
	}


	@Override
	public void process(File srcFile, String fileName) throws Exception
	{
		srcFileName = fileName;
		writerInfo.specialFile = SpecialFile.fnameMap.get(fileName);
		File destFile = new File(destDir.getAbsolutePath() + "/" + fileName + ".xml");
		process(srcFile, destFile);
	}


	@Override
	public void finish()
	{
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


	public void process(File xhtml, File xml) throws Exception
	{
		Matcher m = FILENAME_CHAPTER.matcher(xhtml.getName());
		if(m.find()) {
			fileNameChapter = Integer.parseInt(m.group(1));
			fileNameClass = m.group(2);
		} else {
			fileNameChapter = 0;
			fileNameClass = "";
		}
		lineNumber = 1;
		if(!coverPageFile) {
			book = new Book();
			chapter = new Chapter();
			book.chapter.add(chapter);
		}
		para = prevPara = null;
		formatStack = new Stack<>();
		inBody = inVerse = coverPageFile = false;
		h2count = 0;
		
		readXhtmlFile(xhtml);

		if(!coverPageFile) {
			File outDir = xml.getParentFile();
			outDir.mkdirs();
			writerInfo.xmlFile = xml;
			writerInfo.fileNames.add(xml.getName());
			book.writeToFile(writerInfo);
		}
	}


	private void readXhtmlFile(File xhtml) throws Exception
	{
		try(BufferedReader reader = Files.newBufferedReader(xhtml.toPath(), Charset.forName("UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				++lineNumber;
				processLine(line);
			}
			purgeFormatStack();
		} catch (IOException ex) {
			System.out.println("Error reading " + xhtml.getAbsolutePath() + " at line " + lineNumber);
		}
	}
	
	
	private void processLine(String line)
	{
		if(line.trim().isEmpty())
			return;
		if(!inBody) {
			Matcher m = BODY_LINE.matcher(line);
			if(m.find()) {
				Map<String, String> attrMap = parseAttributes(m.group(1));
				body = new XhtmlTagInfo("body");
				body.id = attrMap.get("id");
				body.classes = classNamesSet(attrMap.get("class"));
				inBody = true;
				tagStack.push(body);
			}
			return;
		}
		Matcher m = HTML_TAG_LINE.matcher(line);
		if(!m.find())
			return;
		String tagName = m.group(2);
		if(tagName != null) tagName = tagName.toLowerCase();
		String tagAttr = m.group(3);
		String content = m.group(4);
		String endTag = m.group(6);
		if(endTag != null) endTag = endTag.toLowerCase();
		if(nonEmpty(tagName)) {
			tag = new XhtmlTagInfo(tagName);
			prevPara = para;
			para = new Paragraph();
			if(nonEmpty(tagAttr)) {
				boolean panditPara;
				if(tagAttr.endsWith(" pa")) {
					panditPara = true;
					tagAttr = tagAttr.substring(0, tagAttr.length()-3);
				} else
					panditPara = false;
				Map<String, String> attrMap = parseAttributes(tagAttr);
				tag.id = attrMap.get("id");
				tag.classes = classNamesSet(attrMap.get("class"));
				if(panditPara && tag.classes != null)
					para.cls = ParagraphClass.valueOf(tag.classes.iterator().next());
				tocPara(attrMap);
			}
			tagStack.push(tag);
			if(!"ol".equals(tag.name) && !"ul".equals(tag.name))
				chapter.para.add(para);
			white = true;
			mappedClassNames();
			inVerse = para.cls == Uvaca || para.cls == Vers || para.cls == TorzsUvaca || para.cls == TorzsVers;
		}
		if(nonEmpty(content)) {
			if(!white)
				para.text.append(' ');
			para.text.append(normalizeContent(content));
		}
		if(nonEmpty(endTag)) {
			if("body".equals(endTag))
				inBody = false;
			if(inBody)
				try {
					styleTag();
				} catch (RuntimeException e) {
					if("exclude".equals(e.getMessage()))
						chapter.para.remove(chapter.para.size()-1);
					else
						throw e;
				}
			XhtmlTagInfo closedTag = tagStack.pop();
			String inTag = tagStack.empty() ? null : closedTag.name;
			if(inTag != null && !endTag.equals(inTag) || inTag == null && !endTag.equals("body"))
				throw new IllegalStateException("End tag mismatch: opening " + inTag + ", closing " + endTag + ", line " + lineNumber);
			if(!tagStack.empty())
				tagStack.peek().prevChild = closedTag;
		}
	}

	private void tocPara(Map<String, String> attrMap) {
		String tocLevel = attrMap.get("toc-level");
		if(nonEmpty(tocLevel))
			para.tocLevel = Integer.parseInt(tocLevel);
		String tocText = attrMap.get("toc-text");
		if(nonEmpty(tocText))
			para.tocText = tocText;
	}

	private String normalizeContent(String content) {
		ncsb.setLength(0);
		int ptr = 0;
		int entityStart = -1;
		int tagStart = -1;
		while(ptr < content.length()) {
			int c = content.charAt(ptr++);
			if(c == '&') {
				if(entityStart != -1)
					throw new IllegalStateException("Entity in entity at line " + lineNumber);
				entityStart = ptr;
			} else if(entityStart != -1 && c == ';') {
				String entity = content.substring(entityStart, ptr-1).toLowerCase();
				entityStart = -1;
				if(entity.startsWith("#x"))
					ncsb.append((char) Integer.parseInt(entity.substring(2), 16));
				else if(entity.startsWith("#"))
					ncsb.append((char) Integer.parseInt(entity.substring(1), 10));
				else if("amp".equals(entity))
					ncsb.append('&');
				else
					throw new IllegalArgumentException("Unsupported entity " + entity + " at line " + lineNumber);
				white = false;
			} else if(c == '<') {
				if(tagStart != -1)
					throw new IllegalStateException("Tag in tag at line " + lineNumber);
				tagStart = ptr;
			} else if(tagStart != -1 && c == '>') {
				String tag = content.substring(tagStart, ptr-1).toLowerCase();
				tagStart = -1;
				white = false;
				if("em".equals(tag) || "i".equals(tag)) {
					if(!inVerse)
						ncsb.append("<i>");
				} else if("/em".equals(tag) || "/i".equals(tag)) {
					if(!inVerse)
						ncsb.append("</i>");
				} else if("strong".equals(tag) || "b".equals(tag))
					ncsb.append("<b>");
				else if("/strong".equals(tag) || "/b".equals(tag))
					ncsb.append("</b>");
				else if(tag.startsWith("a ") ||
						"/a".equals(tag))
					;
				else if(tag.startsWith("span")) {
					if(bbdOwk && content.substring(ptr-21, ptr+13).equals("<span class=\"hidden\">&emsp;</span>")) {
						ncsb.append("\u2002\u2002");
						ptr += 13;
					} else {
						ncsb.append("<").append(tag).append(">");
					}
				}
				else if(tag.startsWith("/"))
					ncsb.append("<").append(tag).append(">");
				else if("br /".equals(tag)) {
					ncsb.append("<br />");
					white = true;
				}
				else
					throw new IllegalArgumentException("Unsupported tag " + tag + " at line " + lineNumber);
			} else if(entityStart == -1 && tagStart == -1) {
				if(!white || c != ' ')
					ncsb.append((char) c);
				white = c == ' ';
			}
		}
		return ncsb.toString();
	}

	private void styleTag() {
		excludeMetaTags();
		partCover();
		chapterHeading();
		lists();
		defaltParagraphStyle();
		followUpTags();
		lineFeedByXhtmlStyleName();
		endingWhitespace();
		skipEmptyParagraph();
		verseIndents();
		footnotePara();
	}

	private void mappedClassNames() {
		if(tag.classes == null || tag.classes.isEmpty())
			return;
		Optional<StyleNameMapping> mapping = Stream.of(styleNameMappings)
				.filter(snm -> tag.classes.contains(snm.css))
				.findFirst();
		if(mapping.isPresent())
			para.cls = mapping.get().cls;
		if(ebook == SSR && para.cls == NemDoltVers && fileNameChapter == 29)
			para.cls = Balra;
	}

	private void excludeMetaTags() {
		if(bbdOwk) {
			if(tag.classes != null) {
				if(tag.classes.contains("ch-number"))
					throw new RuntimeException("exclude");
				if(tag.classes.contains("ch-head"))
					para.xmlTagName = "head";
			}
		}
	}

	private void partCover() {
		if(ebook != SSR)
			return;
		XhtmlTagInfo bodyTag = tagStack.get(0);
		if(!CHAPTER_PAGE_BODY_ID.matcher(bodyTag.id).matches())
			return;
		coverPageFile = true;
		if("h1".equals(tag.name))
			throw new RuntimeException("exclude");
		if("h2".equals(tag.name))
			para.cls = Fejezetszam;
		else if("h3".equals(tag.name))
			para.cls = Fejezetcim;
	}

	private void chapterHeading() {
		if(bbdOwk)
			return;
		XhtmlTagInfo bodyTag = tagStack.get(0);
		if(CHAPTER_PAGE_BODY_ID.matcher(bodyTag.id).matches())
			return;
		if("h1".equals(tag.name)) {
			para.cls = Fejezetcim;
			if(ebook == SSR && "xt".equals(body.id))
				para.tocLevel = 2;
		}
		else if("h2".equals(tag.name) && para.cls == null) {
			++h2count;
			if(ebook == SSR) {
				if(h2count == 1) {
					para.cls = Balra;
					if(!para.text.toString().trim().startsWith("<i>")) {
						para.text.insert(0, "<i>");
						para.text.append("</i>");
					}
				} else {
					para.cls = Alcim;
				}
			}
		} else if("h3".equals(tag.name)) {
			if(ebook == SSR) {
				para.cls = Kozepen;
				if(!para.text.toString().trim().startsWith("<b>")) {
					para.text.insert(0, "<b>");
					para.text.append("</b>");
				}
			}
		}
	}

	private void lists() {
		if(bbdOwk)
			return;
		if("ol".equals(tag.name) || "ul".equals(tag.name))
			throw new RuntimeException("exclude");
		if("li".equals(tag.name)) {
			para.cls = in1;
			XhtmlTagInfo typeTag = tagStack.get(tagStack.size() - 2);
			if("ol".equals(typeTag.name))
				para.text.insert(0, (++typeTag.orderedListCounter) + ". ");
		}
	}

	private void defaltParagraphStyle() {
		if("p".equals(tag.name) && para.cls == null)
			para.cls = TorzsKoveto;
	}

	private void followUpTags() {
		if(bbdOwk)
			return;
		int paraNum = chapter.para.size();
		if(paraNum < 2)
			return;
		XhtmlTagInfo prevTag = tagStack.get(tagStack.size() - 2).prevChild;
		if(prevTag != null && !tag.name.equals(prevTag.name)) {
			// tag change
			if("p".equals(tag.name)) {
				if(para.cls == null || para.cls == TorzsKoveto)
					para.cls = modernBodyIndent() ? TorzsKezdet : Balra;
				else if(para.cls == BalraKoveto)
					para.cls = Balra;
			}
		} else {
			// same tag
			if(prevPara != null && para.cls != prevPara.cls) {
				// different style so far
				boolean prevBodyText = prevPara.cls == null
						|| prevPara.cls.name().startsWith("TorzsK")
						|| prevPara.cls.name().startsWith("Balra");
				if(!prevBodyText) {
					if(para.cls == TorzsKoveto)
						para.cls = modernBodyIndent() ? TorzsKezdet : Balra;
					else if(para.cls == BalraKoveto)
						para.cls = Balra;
				}
			}
		}
	}

	private void lineFeedByXhtmlStyleName() {
		if(bbdOwk)
			return;
		if(tag.classes == null || tag.classes.isEmpty())
			return;
		int linefeed = 0;
		if(tag.classes.contains("space-single-line"))
			linefeed = 1;
		else if(tag.classes.contains("space"))
			linefeed = 2;
		if(linefeed == 0)
			return;
		if(para.cls != null && para.cls.name().contains("Koveto")) {
			para.cls = ParagraphClass.valueOf(para.cls.name().substring(0, para.cls.name().length()-6) + "Kezdet");
			--linefeed;
			if(linefeed == 0)
				return;
		}
		if(para.cls != null && para.cls.name().contains("Kezdet"))
			--linefeed;
		if(linefeed > 0) {
			Paragraph p = new Paragraph();
			p.cls = Ures1;
			chapter.para.add(chapter.para.size()-1, p);
		}
	}

	private void endingWhitespace() {
		int len, ptr = para.text.length();
		do {
			len = ptr;
			while(ptr > 0 && para.text.charAt(ptr-1) == ' ')
				--ptr;
			while(ptr >= 6 && para.text.substring(ptr-6).equals("<br />"))
				ptr -= 6;
		} while(ptr < len);
		para.text.setLength(ptr);
	}

	private void skipEmptyParagraph() {
		if(para.text.length() == 0 && !para.cls.name().startsWith("Ures"))
			throw new RuntimeException("exclude");
	}

	private void verseIndents() {
		if(bbdOwk)
			return;
		if(para.cls == NemDoltVers || !para.cls.name().contains("Vers"))
			return;
		String text = para.text.toString();
		String[] split = text.split("<br />");
		if(split.length >= 3) {
			for(int i = 1; i < split.length; i += 2)
				split[i] = "\u2002\u2002" + split[i];
			para.text.setLength(0);
			for(int i = 0; i < split.length-1; ++i)
				para.text.append(split[i]).append("<br />");
			para.text.append(split[split.length-1]);
		} else if(split.length == 2) {
			// ha lesz KozepenVers
		}
	}

	private void footnotePara() {
		if(para.cls != Labjegyzet)
			return;
		para.text.insert(0, "Lábjegyzet: ");
	}

	private boolean modernBodyIndent() {
		return ebook != SSR;
	}

	private void purgeFormatStack()
	{
		while(formatStack.size() > 0)
			para.text.append(formatStack.pop());
	}

	public static Map<String, String> parseAttributes(String s) {
		Map<String, String> attrs = new HashMap<>();
		Matcher m = HTML_ATTR.matcher(s);
		int start = 0;
		while (true) {
			if(!m.find(start)) break;
			start = m.end();
			attrs.put(m.group(1), m.group(2));
		}
		return attrs;
	}


	public static Set<String> classNamesSet(String classesString) {
		if(!nonEmpty(classesString))
			return null;
		return WHITE_SPLITTER.splitAsStream(classesString)
				.filter(s -> s.trim().length() > 0)
				.collect(Collectors.toSet());
	}

	private static boolean nonEmpty(String tagName) {
		return tagName != null && tagName.length() > 0;
	}
}
