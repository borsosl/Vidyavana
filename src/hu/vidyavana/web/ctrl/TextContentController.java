package hu.vidyavana.web.ctrl;

import static hu.vidyavana.convert.api.ParagraphClass.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.db.model.*;
import hu.vidyavana.search.api.Lucene;
import hu.vidyavana.search.model.Hit;
import hu.vidyavana.search.model.Search;
import hu.vidyavana.search.model.SearchResponse;
import hu.vidyavana.search.task.SearchTask;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.util.Timing;
import hu.vidyavana.web.RequestInfo;


public class TextContentController
{
	public static final int FIRST_FETCH_PARA_COUNT = 10;
	public static final int RESPONSE_CHARS = 10000;
	
	public void service(RequestInfo ri) throws Exception
	{
		ri.ajax = true;
		synchronized(Storage.SYSTEM)
		{
			Storage.SYSTEM.openForRead();
		}
		ri.toc = TocTree.getView(ri.user);
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
		Log.activity("Search task: " + q);
		
		Integer searchId = (Integer) ri.ses.getAttribute("searchId");
		if(searchId == null)
			searchId = 0;
		ri.ses.setAttribute("searchId", ++searchId);

		Search details = new Search();
		details.id = searchId;
		details.user = ri.user.name;
		details.bookAccess = ri.user.access;
		details.queryStr = q;
		details.reqHits = 1000;
		details.fetchHits = 1000;
		Timing.start();
		Globals.searchExecutors.submit(new SearchTask(details)).get();
		Timing.stop("Search", Log.instance());

		SearchResponse res = new SearchResponse();
		ri.ajaxResult = res;
		res.id = details.id;
		res.hitCount = details.hitCount;
		if(res.hitCount == 0)
			return;

		// we have hits
		Map<Integer, Search> smap = getSessionSearchMap(ri);
		smap.put(details.id, details);
		// TODO put search ref into linked lists for timing out and freeing resources

		Hit hit = details.hits.get(0);
		int bookSegmentId = hit.segment<<16 | hit.plainBookId;
		res.display = textForOnePara(ri.toc, bookSegmentId, hit.ordinal);
	}

	
	private void hit(RequestInfo ri) throws Exception
	{
		int searchId = Integer.parseInt(ri.args[3]);
		int hitNum = Integer.parseInt(ri.args[4]);
		Map<Integer, Search> smap = getSessionSearchMap(ri);
		Search details = smap.get(searchId);

		SearchResponse res = new SearchResponse();
		ri.ajaxResult = res;
		if(details == null)
		{
			res.hit = -1;
			return;
		}

		// tomcat restarted or requested hit out of fetched range
		if(details.hits == null || details.hits.size() <= hitNum)
		{
			details.bookAccess = ri.user.access;
			details.fetchHits = hitNum + 1000;
			details.reqHits = hitNum + 1000;
			Timing.start();
			Globals.searchExecutors.submit(new SearchTask(details)).get();
			Timing.stop("Re-search", Log.instance());
		}

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
		res.display = textForOnePara(ri.toc, bookSegmentId, hit.ordinal);
	}

	protected Map<Integer, Search> getSessionSearchMap(RequestInfo ri)
	{
		Map<Integer, Search> smap = (Map<Integer, Search>) ri.ses.getAttribute("searchMap");
		if(smap == null)
		{
			smap = new HashMap<>();
			ri.ses.setAttribute("searchMap", smap);
		}
		return smap;
	}

	
	private void follow(RequestInfo ri)
	{
		int tocId = Integer.parseInt(ri.args[2]);
		int start = Integer.parseInt(ri.args[3]);
		if(ri.toc.checkTocIdRange(tocId, false) != tocId)
		{
			ri.resp.setStatus(404);
			return;
		}
		TocTreeItem node = ri.toc.findNodeById(tocId);
		ri.ajaxResult = text(ri.toc, node, start);
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
				id = ri.toc.checkTocIdRange(id, false);
				node = ri.toc.findNodeById(id);
				while(node.prev != null && node.prev == node.parent && 
					(node.prev.ordinal < 0 || node.prev.ordinal >= node.ordinal-3))
						node = node.prev;
				break;
			case "next":
				node = ri.toc.findNodeById(id);
				while(node.next != null && node.next.parent == node && 
					(node.ordinal < 0 || node.ordinal >= node.next.ordinal-3))
						node = node.next;
				if(node.next != null)
					node = node.next;
				id = ri.toc.checkTocIdRange(node.id, true);
				if(id != node.id)
					node = ri.toc.findNodeById(id);
				break;
		}
		int ord = node.ordinal;
		if(ord < 0)
			ord = 1;
		ri.ajaxResult = text(ri.toc, node, ord);
	}

	
	private DisplayBlock text(TocTree toc, TocTreeItem node, int start)
	{
		DisplayBlock db = new DisplayBlock();
		Storage store = Storage.SYSTEM;
		TocTreeItem origTocNode = node;
		int bookSegmentId = 0;
		BookSegment seg = null;
		while(true)
		{
			bookSegmentId = toc.bookSegmentId(node);
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
			db.shortRef = TocTree.refs(node)[0];
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
		db.downtime = Globals.downtime;
		return db;
	}

	
	private DisplayBlock textForOnePara(TocTree toc, int bookSegmentId, int ordinal)
	{
		DisplayBlock db = new DisplayBlock();
		TocTreeItem tocNode = toc.findNodeByOrdinal(bookSegmentId, ordinal);
		db.bookSegmentId = bookSegmentId;
		db.tocId = tocNode.id;
		db.last = -1;
		String[] refs = TocTree.refs(tocNode);
		db.shortRef = refs[0];
		db.longRef = refs[1];
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
			int i = 0;
			while(true)
			{
				sb.append("<p class=\"").append(p.cls.name())
					.append("\" data-ix=\"").append(i).append("\">")
					.append(p.text).append("</p>");
				if(verse)
				{
					if(i == 0)
					{
						para = seg.readRange(store.handle, ordinal+1, ordinal+3);
						ParagraphClass canFollow = p.cls == Uvaca ? Vers : 
							p.cls == ParagraphClass.TorzsUvaca ? TorzsVers : Hivatkozas;
						p = para.get(i);
						if(p.cls != canFollow)
							break;
					}
					else if(i == 1)
					{
						p = para.get(i);
						if(p.cls != Hivatkozas)
							break;
					}
					else
						break;
					++i;
				}
				else
					break;
			}
			if(verse)
				sb.append("</div></div>");
			db.text = sb.toString();
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Search text request", ex);
		}
		db.downtime = Globals.downtime;
		return db;
	}

	private boolean verseBlock(StoragePara p)
	{
		return p.cls == Vers || p.cls == Uvaca || p.cls == TorzsVers || p.cls == TorzsUvaca
			|| p.cls == Hivatkozas;
	}
}
