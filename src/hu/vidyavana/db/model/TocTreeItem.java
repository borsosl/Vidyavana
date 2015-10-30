package hu.vidyavana.db.model;

import java.io.Serializable;
import java.util.List;

public class TocTreeItem implements Serializable
{
    public int id;
    public Boolean parentStart;
    public String title;
    public Integer ordinal;
    public TocTreeItem parent;
    public List<TocTreeItem> children;
    public Boolean partial;

    /*
    public Integer key;
    public String iconType;
    public String parentId;
    public List<String> parentIds;
    public Boolean isLeaf;
    public Boolean open;
    */
}
