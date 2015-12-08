package hu.vidyavana.db.dao;

import static hu.vidyavana.convert.api.ParagraphClass.*;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import hu.vidyavana.db.model.*;
import hu.vidyavana.search.api.Lucene;
import hu.vidyavana.search.model.Hit;
import hu.vidyavana.search.model.Search;
import hu.vidyavana.search.model.SearchResponse;
import hu.vidyavana.search.task.SearchTask;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.web.RequestInfo;


public class TextContent
{
	public static final int FIRST_FETCH_PARA_COUNT = 10;
	public static final int RESPONSE_CHARS = 10000;
	
	public void service(RequestInfo ri) throws Exception
	{
		synchronized(Storage.SYSTEM)
		{
			Storage.SYSTEM.openForRead();
		}
		ri.ajax = true;
		if("search".equals(ri.args[1]))
			if(ri.args.length > 2 && "hit".equals(ri.args[2]))
				hit(ri);
			else
				search(ri);
		else if("section".equals(ri.args[1]))
			section(ri);
		else if("follow".equals(ri.args[1]))
			follow(ri);
		else
			ri.resp.setStatus(404);
	}

	private void search(RequestInfo ri) throws Exception
	{
		String q = ri.req.getParameter("q");
		Log.debug("Search task: " + q);

		Search details = new Search();
		details.user = "lnd";
		details.queryStr = q;
		details.reqHits = 20;
		Globals.searchExecutors.submit(new SearchTask(details)).get();

		SearchResponse res = new SearchResponse();
		ri.ajaxResult = res;
		res.id = details.id;
		res.hitCount = details.hitCount;
		if(res.hitCount == 0)
			return;

		// we have hits
		Globals.search.put(details.id, details);
		// TODO put search ref into linked lists for timing out and freeing resources

		Hit hit = details.hits.get(0);
		int bookSegmentId = hit.segment<<16 | hit.plainBookId;
		res.display = textForOnePara(bookSegmentId, hit.ordinal);
	}

	private void hit(RequestInfo ri) throws IOException
	{
		int searchId = Integer.parseInt(ri.args[3]);
		int hitNum = Integer.parseInt(ri.args[4]);
		Search details = Globals.search.get(searchId);

		SearchResponse res = new SearchResponse();
		ri.ajaxResult = res;
		if(details == null)
		{
			res.hit = -1;
			return;
		}
		
		// TODO rerun search above 100 hits

		Hit hit = details.hits.get(hitNum);
		if(hit.plainBookId == 0)
		{
			DirectoryReader reader = Lucene.SYSTEM.reader();
			Document doc = reader.document(hit.docId);
			SearchTask.hitDataFromDoc(doc, hit);
		}

		int bookSegmentId = hit.segment<<16 | hit.plainBookId;
		res.id = details.id;
		res.hitCount = details.hitCount;
		res.hit = hitNum;
		res.display = textForOnePara(bookSegmentId, hit.ordinal);
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
		Storage store = Storage.SYSTEM;
		TocTreeItem origTocNode = node;
		int bookSegmentId = 0;
		BookSegment seg = null;
		while(true)
		{
			bookSegmentId = TocTree.inst.bookSegmentId(node);
			seg = store.segment(bookSegmentId);
			if(seg == null)
				node = node.next;
			else
				break;
		}
		node = origTocNode;
		try
		{
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
			db.bookSegmentId = bookSegmentId;
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

				List<StoragePara> para = seg.readRange(store.handle, start, end);
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
			throw new RuntimeException("Text request", ex);
		}
		return db;
	}

	
	private DisplayBlock textForOnePara(int bookSegmentId, int ordinal)
	{
		DisplayBlock db = new DisplayBlock();
		TocTreeItem tocNode = TocTree.inst.findNodeByOrdinal(bookSegmentId, ordinal);
		db.bookSegmentId = bookSegmentId;
		db.tocId = tocNode.id;
		db.last = -1;
		db.longRef = TocTree.longRef(tocNode);
		Storage store = Storage.SYSTEM;
		BookSegment seg = store.segment(bookSegmentId);
		try
		{
			List<StoragePara> para = seg.readRange(store.handle, ordinal, ordinal+1);
			StoragePara p = para.get(0);
			StringBuilder sb = new StringBuilder(p.text.length()+200);
			boolean verse = verseBlock(p);
			if(verse)
				sb.append("<div class=\"VsWrap1\"><div class=\"VsWrap2\">");
			sb.append("<p class=\"").append(p.cls.name())
				.append("\" data-ix=\"1\">").append(p.text).append("</p>");
			if(verse)
				sb.append("</div></div>");
			db.text = sb.toString();
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Search text request", ex);
		}
		return db;
	}

	private boolean verseBlock(StoragePara p)
	{
		return p.cls == Vers || p.cls == Uvaca || p.cls == TorzsVers || p.cls == TorzsUvaca
			|| p.cls == Hivatkozas;
	}
}
