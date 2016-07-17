package hu.vidyavana.util;

public class XmlToken {
    public String text;
    public boolean isTag;
    public boolean isEntity;
    public int sourcePos;
    public int printPos;
    public int printLength;
    public boolean spaceFollows;

    public boolean isText() {
        return !isTag && !isEntity;
    }

    public int fullLength() {
        return printLength + (spaceFollows ? 1 : 0);
    }
}
