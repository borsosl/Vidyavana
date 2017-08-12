package hu.vidyavana.convert.epub;

import java.util.Set;

public class XhtmlTagInfo {
    public String name;
    public String id;
    public Set<String> classes;
    public XhtmlTagInfo prevChild;
    public int orderedListCounter = 0;

    public XhtmlTagInfo(String name) {
        this.name = name;
    }
}
