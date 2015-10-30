package hu.vidyavana.db.dao;

import hu.vidyavana.db.model.BookSegment;
import hu.vidyavana.db.model.StorageRoot;
import hu.vidyavana.db.model.StorageTocItem;
import hu.vidyavana.db.model.TocTreeItem;
import hu.vidyavana.web.RequestInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TocTree
{
	public static TocTree inst = new TocTree();
	private TocTreeItem root;
	private TocTreeItem shortRoot;

	
	private TocTree()
	{
	}


	public void service(RequestInfo ri) throws Exception
	{
		// args[1] == get
		ri.ajax = true;
		int id = Integer.parseInt(ri.args[2]);
		ri.ajaxResult = treeNode(id);
	}
	
	
	public void readFromFile() throws IOException
	{
		StorageRoot st = StorageRoot.SYSTEM;
		st.useFile(StorageRoot.SYSTEM_FILE);
		st.openForRead();
		TreeMap<Integer, Integer> segmentOrder = st.segmentOrder();
		root = new TocTreeItem();
		root.children = new ArrayList<TocTreeItem>();
		TocTreeItem[] levels = new TocTreeItem[10];
		int curLevel = 0;
		int id = 0;
		int prevBook = -1;
		for(Entry<Integer, Integer> e : segmentOrder.entrySet())
		{
			BookSegment seg = st.segment(e.getValue());
			// book title: level 0
			if(seg.bookId != prevBook)
			{
				TocTreeItem tti = treeItem(++id, seg.title);
				tti.ordinal = -seg.id();
				tti.parent = root;
				root.children.add(tti);
				levels[0] = tti;
			}
			StorageTocItem[] ct = seg.contents;
			for(StorageTocItem cti : ct)
			{
				if(cti.level > curLevel+1)
					throw new RuntimeException("Invalid level for para ordinal "+cti.paraOrdinal);
				curLevel = cti.level;
				TocTreeItem parent = levels[curLevel-1];
				if(parent.children == null)
				{
					parent.children = new ArrayList<TocTreeItem>();
					// an id before the actal children is reserved for parent-start nodes
					++id;
				}
				TocTreeItem tti = treeItem(++id, cti.title);
				tti.ordinal = (int) cti.paraOrdinal;
				tti.parent = parent;
				parent.children.add(tti);
				levels[curLevel] = tti;
			}
		}
		st.close();
	}
	
	
	private TocTreeItem treeItem(int id, String title)
	{
		TocTreeItem tti = new TocTreeItem();
		tti.id = id;
		tti.title = title;
		return tti;
	}
	
	
	public TocTreeItem initialShortTree() throws IOException
	{
		if(root == null)
			readFromFile();
//		if(shortRoot == null)
		{
			shortRoot = new TocTreeItem();
			shortRoot.children = new ArrayList<TocTreeItem>();
			addChildren(root, shortRoot);
		}
		return shortRoot;
	}
	
	
	public TocTreeItem treeNode(int id)
	{
		TocTreeItem srvNode = findNodeById(root, id);
		TocTreeItem cliNode = treeItem(srvNode.id, srvNode.title);
		cliNode.children = new ArrayList<TocTreeItem>();
		addChildren(srvNode, cliNode);
		return cliNode;
	}

	
	public TocTreeItem findNodeById(int id)
	{
		return findNodeById(root, id);
	}


	private TocTreeItem findNodeById(TocTreeItem parent, int id)
	{
		List<TocTreeItem> ch = parent.children;
		if(ch == null)
			return parent;
		int len = ch.size();
		for(int i=0; i<len; ++i)
		{
			TocTreeItem ti = ch.get(i);
			if(id > ti.id)
				continue;
			if(id < ti.id && i > 0)
				return findNodeById(ch.get(i-1), id);
			return ti;
		}
		return findNodeById(ch.get(len-1), id);
	}


	private void addChildren(TocTreeItem src, TocTreeItem dest)
	{
		dest.children = new ArrayList<TocTreeItem>();
		if(src != root)
		{
			TocTreeItem ti = treeItem(src.id+1, "Kezdete");
			ti.parentStart = true;
			dest.children.add(ti);
		}
		for(int i=0; i<src.children.size(); ++i)
		{
			TocTreeItem it = src.children.get(i);
			TocTreeItem ch = treeItem(it.id, it.title);
			dest.children.add(ch);
			if(it.children != null)
				if(src == root && i==0)
					addChildren(it, ch);
				else
					ch.partial = true;
		}
	}
	
	
	public int bookId(TocTreeItem ti)
	{
		while(ti != null && ti.ordinal > 0)
			ti = ti.parent;
		if(ti.ordinal < 0)
			return -ti.ordinal;
		throw new IllegalStateException("In TOC no parent is under root");
	}
}
