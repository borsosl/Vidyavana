package hu.vidyavana.db.model;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class TocTree
{
	static class BookRange
	{
		int plainBookId;
		int tocIdStart, tocIdEnd;
	}
	
	public static TocTree inst = new TocTree();
	public static Map<Integer, TocTree> viewCache = new HashMap<>();
	private TocTreeItem root;
	private TocTreeItem shortRoot;
	private TocTreeItem prevItem;
	public Map<String, Integer> abbrevToPlainBookId;
	public List<BookRange> bookRanges = new ArrayList<>();
	public int maxId;

	
	private TocTree()
	{
	}

	
	private TocTree(BookAccess access)
	{
		root = new TocTreeItem();
		root.children = new ArrayList<TocTreeItem>();
		for(TocTreeItem tti : inst.root.children)
			if(access.contains(-tti.ordinal))
				root.children.add(tti);
		for(BookRange br : inst.bookRanges)
			if(access.contains(br.plainBookId))
				bookRanges.add(br);
		maxId = bookRanges.get(bookRanges.size()-1).tocIdEnd;
	}


	public static TocTree getView(User user)
	{
		if(user.access == null)
			user.setAccess();
		if(user.access.fullAccess)
			return inst;
		TocTree tt = viewCache.get(user.access.hashCode());
		if(tt != null)
			return tt;
		tt = new TocTree(user.access);
		viewCache.put(user.access.hashCode(), tt);
		return tt;
	}
	
	
	public void readFromFile() throws IOException
	{
		Storage st = Storage.SYSTEM;
		st.openForRead();
		TreeMap<Integer, Integer> segmentOrder = st.segmentOrder();
		root = new TocTreeItem();
		root.children = new ArrayList<TocTreeItem>();
		prevItem = null;
		abbrevToPlainBookId = new HashMap<>();
		BookRange currentRange = null;
		TocTreeItem[] levels = new TocTreeItem[10];
		int curLevel = 0;
		int id = 0;
		int prevBook = -1;
		for(Entry<Integer, Integer> e : segmentOrder.entrySet())
		{
			BookSegment seg = st.segment(e.getValue());
			StorageTocItem[] ct = seg.contents;
			// book title: level 0
			boolean markSegment = false;
			if(seg.plainBookId != prevBook)
			{
				TocTreeItem tti = treeItem(++id, seg.title, seg.abbrev, true);
				tti.level = curLevel = 0;
				tti.ordinal = -seg.bookSegmentId();
				tti.parent = root;
				root.children.add(tti);
				levels[0] = tti;
				prevBook = seg.plainBookId;
				abbrevToPlainBookId.put(seg.abbrev, (int) seg.plainBookId);
				if(currentRange != null)
					currentRange.tocIdEnd = id;
				currentRange = new BookRange();
				currentRange.plainBookId = seg.plainBookId;
				currentRange.tocIdStart = id;
				bookRanges.add(currentRange);
			}
			if(seg.segment > 0)
				markSegment = true;
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
				TocTreeItem tti = treeItem(++id, cti.title, cti.abbrev, true);
				tti.level = curLevel;
				tti.parent = parent;
				tti.ordinal = (int) cti.paraOrdinal;
				if(markSegment)
				{
					tti.ordinal = -seg.bookSegmentId();
					tti.parent.ordinal = -seg.plainBookId;
					markSegment = false;
				}
				parent.children.add(tti);
				levels[curLevel] = tti;
			}
		}
		if(currentRange != null)
			currentRange.tocIdEnd = id + 1;
		maxId = id;
		st.close();
	}
	
	
	private TocTreeItem treeItem(int id, String title, String abbrev, boolean server)
	{
		TocTreeItem tti = new TocTreeItem();
		tti.id = id;
		tti.title = title;
		tti.abbrev = abbrev;
		if(server)
		{
			tti.prev = prevItem;
			if(prevItem != null)
				prevItem.next = tti;
			prevItem = tti;
		}
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
		TocTreeItem srvNode = findNodeById(root, id, false);
		TocTreeItem cliNode = treeItem(srvNode.id, srvNode.title, srvNode.abbrev, false);
		cliNode.children = new ArrayList<TocTreeItem>();
		addChildren(srvNode, cliNode);
		return cliNode;
	}

	
	public TocTreeItem findNodeById(int id)
	{
		return findNodeById(root, id, false);
	}


	private TocTreeItem findNodeById(TocTreeItem parent, int id, boolean byOrdinal)
	{
		List<TocTreeItem> ch = parent.children;
		if(ch == null)
			return parent;
		int len = ch.size();
		for(int i=0; i<len; ++i)
		{
			TocTreeItem ti = ch.get(i);
			int tiId = byOrdinal ? ti.ordinal : ti.id;
			if(id > tiId)
				continue;
			if(id < tiId)
			{
				if(i > 0)
					return findNodeById(ch.get(i-1), id, byOrdinal);
				if(ti.prev != null)
					return ti.prev;
			}
			return ti;
		}
		return findNodeById(ch.get(len-1), id, byOrdinal);
	}

	
	public TocTreeItem findNodeByOrdinal(int bookSegmentId, int ordinal)
	{
		TocTreeItem bookRoot = findBookRoot(bookSegmentId, root);
		return findNodeById(bookRoot, ordinal, true);
	}


	public TocTreeItem findBookNode(int bookSegmentId)
	{
		return findBookRoot(bookSegmentId, root);
	}

	
	private TocTreeItem findBookRoot(int bookSegmentId, TocTreeItem parent)
	{
		int plainBookId = bookSegmentId & ((1<<16)-1);
		for(TocTreeItem tti : parent.children)
			if(tti.ordinal == -bookSegmentId)
				return tti;
			else if(tti.ordinal == -plainBookId)
				return findBookRoot(bookSegmentId, tti);
		return null;
	}


	private void addChildren(TocTreeItem src, TocTreeItem dest)
	{
		dest.children = new ArrayList<TocTreeItem>();
		if(src != root)
		{
			TocTreeItem ti = treeItem(src.id+1, "Kezdete", src.abbrev, false);
			ti.parentStart = true;
			dest.children.add(ti);
		}
		for(int i=0; i<src.children.size(); ++i)
		{
			TocTreeItem it = src.children.get(i);
			TocTreeItem ch = treeItem(it.id, it.title, it.abbrev, false);
			dest.children.add(ch);
			if(it.children != null)
				if(src == root && i==0)
					addChildren(it, ch);
				else
					ch.partial = true;
		}
	}
	
	
	public int bookSegmentId(TocTreeItem ti)
	{
		while(ti != null && ti.ordinal > 0)
			ti = ti.parent;
		if(ti.ordinal < 0)
			return -ti.ordinal;
		throw new IllegalStateException("In TOC no parent is under root");
	}
	
	
	public int checkTocIdRange(int tocId, boolean forwardStep)
	{
		BookRange prev = null;
		for(BookRange br : bookRanges)
		{
			if(br.tocIdStart > tocId)
			{
				if(forwardStep || prev == null)
					return br.tocIdStart;
				return prev.tocIdEnd - 1;
			}
			else if(br.tocIdEnd > tocId)
				return tocId;
			prev = br;
		}
		return prev.tocIdEnd - 1;
	}

	public TocTreeItem nextSibling(TocTreeItem node) {
		if(node == root)
			return null;
		boolean nodeReached = false;
		for(TocTreeItem pnode : node.parent.children) {
			if(nodeReached)
				return pnode;
			if(pnode == node)
				nodeReached = true;
		}
		// node is last item of its parent
		return nextSibling(node.parent);
	}


	public static DisplayBlock refs(TocTreeItem node, boolean needLong, DisplayBlock response)
	{
		if(response == null)
			response = new DisplayBlock();
		List<String> abbrevs = new ArrayList<>();
		List<String> titles = new ArrayList<>();
		int len = 0;
		do
		{
			if(node.id != 0)
		        response.bookTocId = node.id;
			if(node.abbrev != null)
				abbrevs.add(node.abbrev);
			if(needLong && node.title != null)
			{
				String t = node.title;
				int ix = t.indexOf('ǀ');
				if(ix > -1)
					t = t.substring(0, ix) + " (" + t.substring(ix+1) + ')';
				titles.add(t);
				len += t.length() + 2;
			}
			node = node.parent;
		} while(node != null);

		StringBuilder sbShort = new StringBuilder(len);
		for(int i = abbrevs.size()-1; i>=0; --i)
			sbShort.append(abbrevs.get(i)).append('.');
		if(sbShort.length() > 1)
			sbShort.setLength(sbShort.length()-1);
		response.shortRef = sbShort.toString();

		if(needLong) {
			StringBuilder sbLong = new StringBuilder(len);
			for(int i = titles.size()-1; i>=0; --i)
				sbLong.append(titles.get(i)).append(", ");
			if(sbLong.length() > 2)
				sbLong.setLength(sbLong.length()-2);
			response.longRef = sbLong.toString();
		}
		return response;
	}
}
