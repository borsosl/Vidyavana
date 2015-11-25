package hu.vidyavana.db.dao;

import static hu.vidyavana.convert.api.ParagraphClass.*;
import java.io.IOException;
import java.util.List;
import hu.vidyavana.db.model.*;
import hu.vidyavana.web.RequestInfo;


public class TextContent
{
	public static final int FIRST_FETCH_PARA_COUNT = 10;
	public static final int RESPONSE_CHARS = 10000;
	
	public void service(RequestInfo ri) throws Exception
	{
		synchronized(StorageRoot.SYSTEM)
		{
			StorageRoot.SYSTEM.openForRead();
		}
		ri.ajax = true;
		if("section".equals(ri.args[1]))
			section(ri);
		else if("follow".equals(ri.args[1]))
			follow(ri);
	}

	private void follow(RequestInfo ri)
	{
		int tocId = Integer.parseInt(ri.args[2]);
		int start = Integer.parseInt(ri.args[3]);
		TocTreeItem node = TocTree.inst.findNodeById(tocId);
		ri.ajaxResult = text(node, start);
	}

	private void section(RequestInfo ri)
	{
		String dir = ri.args[2];
		int id = Integer.parseInt(ri.args[3]);
		TocTreeItem node = null;
		switch(dir)
		{
			case "prev":
				--id;
				if(id < 1)
					id = 1;
				// intentional fallthru
			case "go":
				node = TocTree.inst.findNodeById(id);
				while(node.prev != null && node.prev == node.parent && 
					(node.prev.ordinal < 0 || node.prev.ordinal >= node.ordinal-3))
						node = node.prev;
				break;
			case "next":
				node = TocTree.inst.findNodeById(id);
				while(node.next != null && node.next.parent == node && 
					(node.ordinal < 0 || node.ordinal >= node.next.ordinal-3))
						node = node.next;
				if(node.next != null)
					node = node.next;
				break;
		}
		int ord = node.ordinal;
		if(ord < 0)
			ord = 1;
		ri.ajaxResult = text(node, ord);
	}

	
	private DisplayBlock text(TocTreeItem node, int start)
	{
		DisplayBlock db = new DisplayBlock();
		StorageRoot sr = StorageRoot.SYSTEM;
		int bookId = TocTree.inst.bookId(node);
		BookSegment seg = sr.segment(bookId);
		try
		{
			TocTreeItem origTocNode = node;
			int end = start + FIRST_FETCH_PARA_COUNT;
			// merge titles with text: find text block TOC node
			while(node.next != null && node.next.parent == node &&
				(node.ordinal < 0 || node.ordinal >= node.next.ordinal-3))
			{
				node = node.next;
				++end;
			}
			int nodeEnd;
			if(node.next != null && node.next.ordinal > node.ordinal)
				nodeEnd = node.next.ordinal;
			else
				nodeEnd = seg.paraNum + 1;
			--start;
			--end;
			--nodeEnd;
			if(start < 0)
				start = 0;
			db.bookId = bookId;
			db.tocId = origTocNode.id;
			StringBuilder sb = new StringBuilder(RESPONSE_CHARS + 20000);
			int last = start;
			int len = 0;
			int cPara = 0;
			boolean firstPass = true;
			boolean prevVerse = false;

			outer: while(true)
			{
				if(end > nodeEnd-2)
					end = nodeEnd;
				if(start >= end)
					break;

				List<StoragePara> para = seg.readRange(sr.handle, start, end);
				int pix = 0;
				StoragePara p = null;
				boolean verse = false;
				for(int i=start; i<end; ++i)
				{
					p = para.get(pix++);
					verse = verseBlock(p);
					if(prevVerse && !verse)
						sb.append("</div></div>");
					if(!firstPass && !verse)
						break outer;
					if(firstPass)
					{
						len += p.text.length();
						if(len > RESPONSE_CHARS)
							break;
					}
					if(!prevVerse && verse)
						sb.append("<div class=\"VsWrap1\"><div class=\"VsWrap2\">");
					sb.append("<p class=\"").append(p.cls.name())
						.append("\" data-ix=\"").append(i).append("\">")
						.append(p.text).append("</p>");
					last = i;
					++cPara;
					prevVerse = verse;
				}
				start = last+1;
				if(start >= nodeEnd || !verse && len > RESPONSE_CHARS)
					break;
				if(len > RESPONSE_CHARS)
				{
					// if inside verse, prolong range, and stop at the end of verse inside inner loop
					end = start+3;
					firstPass = false;
				}
				else
				{
					int avgParaLen = len / cPara;
					int predict = ((RESPONSE_CHARS - len) / avgParaLen) + 1;
					end = start + predict;
				}
			}
			if(last == nodeEnd-1)
				db.last = 0;
			else
				db.last = last+2;
			db.text = sb.toString();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		return db;
	}

	private boolean verseBlock(StoragePara p)
	{
		return p.cls == Vers || p.cls == Uvaca || p.cls == TorzsVers || p.cls == TorzsUvaca
			|| p.cls == Hivatkozas;
	}
}