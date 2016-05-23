package hu.vidyavana.convert.api;

import com.google.common.collect.Sets;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hu.vidyavana.convert.api.ParagraphClass.*;

public class ProofreadWords {
    public static boolean ACTIVE = false;

    private static Pattern TAG_SPLIT = Pattern.compile("<(/?)(\\w+)[^>]*>");
    private static Set<ParagraphClass> sanskritParaTypes = Sets.newHashSet(Uvaca, Vers, Proza, TorzsUvaca, TorzsVers, Kozepen);

    private Map<String, Set<String >> hun = new TreeMap<>(new DiacriticWordComparator());
    private Map<String, Set<String >> sans = new TreeMap<>(new DiacriticWordComparator());
    private boolean isSanskrit;
    private String srcFileName;
    private int srcFileLine;

    public void collect(Paragraph para, String txt) {
        srcFileName = para.srcFileName;
        srcFileLine = para.srcFileLine;
        boolean isEndPara = "end".equals(para.srcStyle);
        boolean isSanskritPara = sanskritParaTypes.contains(para.cls);
        isSanskrit = isSanskritPara;
        Matcher tags = TAG_SPLIT.matcher(txt);
        for(int ix = 0; ix < txt.length(); ix = tags.end()) {
            if(tags.find(ix)) {
                if(tags.start() > ix)
                    processParaSection(txt.substring(ix, tags.start()));
                if(!isSanskritPara && "i".equals(tags.group(2))) {
                    isSanskrit = tags.group(1).length() == 0;
                    if(isEndPara)
                        isSanskrit = !isSanskrit;
                }
            } else {
                processParaSection(txt.substring(ix));
                break;
            }
        }
    }

    private void processParaSection(String s) {
        Matcher words = Paragraph.WORD.matcher(s);
        for(int ix = 0; ix < s.length(); ix = words.end()) {
            if(words.find(ix))
                processWord(words.group());
            else
                break;
        }
    }

    private void processWord(String s) {
        String lower = DiacriticLowercase.word(s);
        boolean isSanskritWord = isSanskrit;
        if(DiacriticLowercase.hasSanskritCharacter(lower, true))
            isSanskritWord = true;
        addWord(isSanskritWord ? sans : hun, lower);
    }

    private void addWord(Map<String, Set<String>> map, String word) {
        Set<String> locations = map.get(word);
        if(locations == null) {
            locations = new HashSet<>();
            map.put(word, locations);
        }
        if(locations.size() < 4)
            locations.add(srcFileName + ':' + srcFileLine);
    }

    public void writeFiles(File destDir, WriterInfo writerInfo) {
        writeFiles(destDir, sans, "sans");
        writeFiles(destDir, hun, "hun");
    }

    private void writeFiles(File destDir, Map<String, Set<String>> map, String name) {
        File tocFile = new File(destDir.getAbsolutePath() + "/proof-"+name+".txt");
        try(Writer writer = new OutputStreamWriter(new FileOutputStream(tocFile), "UTF-8")) {
            for (Map.Entry<String, Set<String>> e : map.entrySet()) {
                writer.write(e.getKey());
                if(e.getValue().size() < 4) {
                    StringBuilder sb = new StringBuilder(100);
                    sb.append(" : ");
                    for(String loc : e.getValue())
                        sb.append(loc).append('|');
                    sb.setLength(sb.length()-1);
                    writer.write(sb.toString());
                }
                writer.write("\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
