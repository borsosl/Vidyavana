package hu.vidyavana.db.model;

public class Bookmark {
    public int id;
    public transient int userId;
    public String name;
    public boolean follow;
    public transient int bookSegmentId;
    public transient int ordinal;
    public String shortRef;
    public transient long lastUsed;
}
