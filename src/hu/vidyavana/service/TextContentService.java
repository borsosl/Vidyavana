package hu.vidyavana.service;

import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.db.model.*;
import hu.vidyavana.search.api.Lucene;
import hu.vidyavana.search.model.Hit;
import hu.vidyavana.search.model.Search;
import hu.vidyavana.search.model.Search.Order;
import hu.vidyavana.search.model.SearchResponse;
import hu.vidyavana.search.task.SearchTask;
import hu.vidyavana.search.util.HitListEntry;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.util.Timing;
import hu.vidyavana.web.RequestInfo;
import hu.vidyavana.web.Sessions;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hu.vidyavana.convert.api.ParagraphClass.*;


public class TextContentService
{
	public static final int FIRST_FETCH_PARA_COUNT = 10;
	public static final int RESPONSE_CHARS = 10000;

	private RequestInfo ri;

	public TextContentService(RequestInfo ri) {
		this.ri = ri;
	}

	public void search() throws Exception
	{
		String q = ri.req.getParameter("q");
		String sortStr = ri.req.getParameter("sort");
		String pageStr = ri.req.getParameter("page");
		Log.activity("Search task: " + q);
		Sessions.updateUserAccessTime(ri.ses, ri.user);
		
		Integer searchId = (Integer) ri.ses.getAttribute("searchId");
		if(searchId == null)
			searchId = 0;
		ri.ses.setAttribute("searchId", ++searchId);

		Search details = new Search();
		details.id = searchId;
		details.user = ri.user.name;
		details.bookAccess = ri.user.access;
		details.queryStr = q;
		details.reqHits = 100;
		details.fetchHits = 1000;
		details.order = sortStr == null ? Order.Score : Order.valueOf(sortStr);
		try {
			details.page = Integer.valueOf(pageStr);
		} catch (NumberFormatException ex) {
			details.page = 1;
		}
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
		Map<Integer, Search> smap = getSessionSearchMap();
		smap.put(details.id, details);
		// TODO put search ref into linked lists for timing out and freeing resources

		if(details.page == 1) {
			Hit hit = details.hits.get(0);
			int bookSegmentId = hit.segment<<16 | hit.plainBookId;
			res.endHit = -1;
			res.display = textForOnePara(ri.toc, bookSegmentId, hit.ordinal);
		} else {
			// make sure enough hit data is fetched for allowed page sizes
			int end = details.page > res.hitCount ? res.hitCount : details.page;
			res.endHit = end;
			res.display = textForHitList(details, 0, end, ri.toc);
		}
	}


	public void hit() throws Exception
	{
		int searchId = Integer.parseInt(ri.args[3]);
		int hitOrdinal = Integer.parseInt(ri.args[4]);
		Map<Integer, Search> smap = getSessionSearchMap();
		Search details = smap.get(searchId);

		SearchResponse res = new SearchResponse();
		ri.ajaxResult = res;
		if(details == null)
		{
			res.startHit = res.endHit = -1;
			return;
		}

		int endOrdinal = hitOrdinal + details.page;
		if(endOrdinal > details.hitCount)
			endOrdinal = details.hitCount;

		// tomcat restarted or requested hit out of fetched range
		if(details.hits == null || details.hits.size() < endOrdinal)
		{
			details.bookAccess = ri.user.access;
			details.fetchHits = hitOrdinal + 1000;
			details.reqHits = hitOrdinal + 100;
			Timing.start();
			Globals.searchExecutors.submit(new SearchTask(details)).get();
			Timing.stop("Re-search", Log.instance());
		}

		for(int i = hitOrdinal; i < endOrdinal; ++i) {
			Hit hit = details.hits.get(i);
			if(hit.plainBookId == 0)
			{
				DirectoryReader reader = Lucene.SYSTEM.reader();
				Document doc = reader.document(hit.docId);
				SearchTask.hitDataFromDoc(doc, hit);
			}
		}

		res.hitCount = details.hitCount;
		res.id = details.id;
		res.startHit = hitOrdinal;
		if(details.page == 1) {
			Hit hit = details.hits.get(hitOrdinal);
			int bookSegmentId = hit.segment<<16 | hit.plainBookId;
			res.endHit = -1;
			res.display = textForOnePara(ri.toc, bookSegmentId, hit.ordinal);
		} else {
			res.endHit = endOrdinal;
			res.display = textForHitList(details, hitOrdinal, endOrdinal, ri.toc);
		}
	}

	public void follow()
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


	public void section()
	{
		String dir = ri.args[2];
		int id = Integer.parseInt(ri.args[3]);
		TocTreeItem node = null;
        boolean go = true;
		switch(dir)
		{
			case "prev":
				--id;
				if(id < 1)
					id = 1;
                go = false;
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
                go = false;
				break;
		}

		int ord = node.ordinal;
		if(ord < 0)
			ord = 1;
		if(go) {
			ri.ses.removeAttribute(BookmarkService.FOLLOWED_BOOKMARK_KEY);
			Sessions.updateUserAccessTime(ri.ses, ri.user);
		}
		else
			BookmarkService.updateFollowedBookmark(ri, ri.toc.bookSegmentId(node), ord, TocTree.refs(node, false)[0]);
		ri.ajaxResult = text(ri.toc, node, ord);
	}


	public DisplayBlock text(TocTree toc, TocTreeItem node, int start)
	{
		DisplayBlock db = new DisplayBlock();
		Storage store = Storage.SYSTEM;
		TocTreeItem origTocNode = node;
		int bookSegmentId;
		BookSegment seg;
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
			db.shortRef = TocTree.refs(node, false)[0];
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
				StoragePara p;
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
		String[] refs = TocTree.refs(tocNode, true);
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


	private DisplayBlock textForHitList(Search details, int start, int end, TocTree toc) {
		Timing.start();
		DisplayBlock db = new DisplayBlock();
		db.last = -1;
		Storage store = Storage.SYSTEM;

		StringBuilder sb = new StringBuilder((end-start) * 500);
		sb.append("<table class=\"hitlist-table\">");
		HitListEntry hitListEntry = new HitListEntry(80);
		for(int i = start; i<end; ++i) {
			Hit hit = details.hits.get(i);
			int bookSegmentId = hit.segment<<16 | hit.plainBookId;
			TocTreeItem tocNode = toc.findNodeByOrdinal(bookSegmentId, hit.ordinal);
			String shortRef = TocTree.refs(tocNode, false)[0];
			sb.append("<tr><td><a ")
					.append(tocNode.id)
					.append(">")
					.append(shortRef)
					.append("</a></td><td ")
					.append(i)
					.append('>');
			BookSegment seg = store.segment(bookSegmentId);
			try
			{
				List<StoragePara> para = seg.readRange(store.handle, hit.ordinal, hit.ordinal+1);
				StoragePara p = para.get(0);
				sb.append(hitListEntry.create(p.text, details.queryStr, verseBlock(p), boldParagraph(p)));
			}
			catch(IOException ex)
			{
				throw new RuntimeException("Hit list request", ex);
			}
			sb.append("</td></tr>\n");
		}
		sb.append("</table>");

		db.text = sb.toString();
		db.downtime = Globals.downtime;
		Timing.stop("Hit list ("+details.page+")", Log.instance());
		return db;
	}

	private boolean verseBlock(StoragePara p)
	{
		return p.cls == Vers || p.cls == Uvaca || p.cls == TorzsVers || p.cls == TorzsUvaca
				|| p.cls == Hivatkozas;
	}

	private boolean boldParagraph(StoragePara p)
	{
		return p.cls.name().toLowerCase().contains("cim")  || p.cls == Forditas;
	}

	private Map<Integer, Search> getSessionSearchMap()
	{
		Map<Integer, Search> smap = (Map<Integer, Search>) ri.ses.getAttribute("searchMap");
		if(smap == null)
		{
			smap = new HashMap<>();
			ri.ses.setAttribute("searchMap", smap);
		}
		return smap;
	}
}
