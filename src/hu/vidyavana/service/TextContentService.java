package hu.vidyavana.service;

import hu.vidyavana.convert.api.ParagraphCategory;
import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.db.model.*;
import hu.vidyavana.search.api.Lucene;
import hu.vidyavana.search.model.Hit;
import hu.vidyavana.search.model.Search;
import hu.vidyavana.search.model.Search.Order;
import hu.vidyavana.search.model.SearchResponse;
import hu.vidyavana.search.task.SearchTask;
import hu.vidyavana.search.util.HitListEntry;
import hu.vidyavana.search.util.SearchRangeUtil;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.util.Timing;
import hu.vidyavana.web.RequestInfo;
import hu.vidyavana.web.Sessions;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;

import java.io.IOException;
import java.util.*;

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
		String nodeFilterStr = ri.req.getParameter("nodeFilter");
		String paraTypes = ri.req.getParameter("paraTypes");
		Log.activity("Search task: " + q);
		Sessions.updateUserAccessTime(ri.ses, ri.user);
		
		Integer searchId = (Integer) ri.ses.getAttribute("searchId");
		if(searchId == null)
			searchId = 0;
		else
			sweepOldSearchResults();
		ri.ses.setAttribute("searchId", ++searchId);

		Search details = new Search();
		details.id = searchId;
		details.user = ri.user.name;
		details.bookAccess = ri.user.access;
		details.queryStr = q;
		details.reqHits = 100;
		details.fetchHits = 1000;
		details.order = sortStr == null ? Order.Score : Order.valueOf(sortStr);
		details.lastAccess = new Date();
		try {
			details.page = Integer.valueOf(pageStr);
		} catch (NumberFormatException ex) {
			details.page = 1;
		}
		details.searchRanges = SearchRangeUtil.nodeFilterStringToSearchRangeList(nodeFilterStr, ri.toc);
		details.paraTypesBits = SearchRangeUtil.paraTypesBits(paraTypes);
		Timing.start();
		Globals.searchExecutors.submit(new SearchTask(details)).get();
		Timing.stop("Search", Log.instance());

		SearchResponse res = new SearchResponse();
		ri.ajaxResult = res;
		res.id = details.id;
		res.hitCount = details.hitCount;
		if(res.hitCount == 0) {
			if(details.errorCode != null)
				res.errorText = details.errorCode.text;
			return;
		}

		// we have hits
		Map<Integer, Search> smap = getSessionSearchMap();
		smap.put(details.id, details);

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

	private void sweepOldSearchResults() {
		long old = new Date().getTime() - 30*60*1000;
		getSessionSearchMap().values().stream()
			.filter(search -> search.hits != null && search.lastAccess.getTime() < old)
			.forEach(search -> {
				search.hits = null;
				search.bookAccess = null;
			});
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
		details.lastAccess = new Date();

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
		ri.ajaxResult = text(ri.toc, node, start, null);
	}


	public void section()
	{
		String dir = ri.args[2];
		int id = Integer.parseInt(ri.args[3]);
		String paraTypes = ri.args.length > 4 ? ri.args[4] : null;
		TocTreeItem node;
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
			default:
				return;
		}

		int ord = node.ordinal;
		if(ord < 0)
			ord = 1;
		if(go) {
			ri.ses.removeAttribute(BookmarkService.FOLLOWED_BOOKMARK_KEY);
			Sessions.updateUserAccessTime(ri.ses, ri.user);
		}
		else
			BookmarkService.updateFollowedBookmark(ri, ri.toc.bookSegmentId(node), ord, TocTree.refs(node, false, null).shortRef);
		ri.ajaxResult = text(ri.toc, node, ord, paraTypes);
	}

	public void filter()
	{
		int bookSegmentId = Integer.parseInt(ri.args[2]);
		int ord = Integer.parseInt(ri.args[3]);
		String paraTypes = ri.args.length > 4 ? ri.args[4] : null;

		TocTreeItem node;
		if(ord == 0) {
			// move to next book
			node = ri.toc.nextSibling(ri.toc.findBookNode(bookSegmentId));
			// no more: last section of this last book
			if(node == null) {
				node = ri.toc.findNodeById(ri.toc.maxId);
				ord = node.ordinal;
			} else
				ord = 1;
		} else {
			node = ri.toc.findNodeByOrdinal(bookSegmentId, ord);
			while(node.next != null && node.next.parent == node &&
					(node.ordinal < 0 || node.ordinal >= node.next.ordinal-3))
				node = node.next;
		}
		int id = ri.toc.checkTocIdRange(node.id, true);
		if(id != node.id)
			node = ri.toc.findNodeById(id);

		BookmarkService.updateFollowedBookmark(ri, ri.toc.bookSegmentId(node), ord, TocTree.refs(node, false, null).shortRef);
		ri.ajaxResult = text(ri.toc, node, ord, paraTypes);
	}


	public DisplayBlock text(TocTree toc, TocTreeItem node, int start, String paraTypes)
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
			db.first = start;
			db.filtered = paraTypes != null && !paraTypes.isEmpty();
			EnumSet<ParagraphCategory> displayTypes = db.filtered ? ParagraphCategory.enumSetOf(paraTypes) : null;

			int end = start + FIRST_FETCH_PARA_COUNT;
			// merge titles with text: find text block TOC node
			while(node.next != null && node.next.parent == node &&
				(node.ordinal < 0 || node.ordinal >= node.next.ordinal-3))
			{
				node = node.next;
				++end;
			}
			int nodeEnd;
			if(!db.filtered && node.next != null && node.next.ordinal > node.ordinal)
				nodeEnd = node.next.ordinal;
			else
				nodeEnd = seg.paraNum + 1;
			--start;
			--end;
			--nodeEnd;
			if(start < 0)
				start = 0;
			int firstStart = start;
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
				if(db.filtered) {
                    if(end > firstStart+100)
                        end = firstStart+100;
                    if(start >= end) {
						if(len == 0) {
							String part = end == nodeEnd ? "A kötet végéig már" : "100 bekezdésen belül";
							sb.append("<p class=\"TorzsKezdet\">")
									.append(part)
									.append(" nem volt megjeleníthető bekezdés-típus.</p>");
						}
						break;
					}
				} else if(start >= end) {
					break;
				}

				List<StoragePara> para = seg.readRange(store.handle, start, end);
				int pix = 0;
				StoragePara p;
				boolean verse = false;
				for(int i=start; i<end; ++i)
				{
					p = para.get(pix++);
					if(db.filtered) {
						if(!displayTypes.contains(p.getParagraphCategory())) {
							last = i;
							continue;
						} else if(len == 0) {
							db.first = i+1;
							node = ri.toc.findNodeByOrdinal(bookSegmentId, i+1);
						}
					}
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
				if(!db.filtered && start >= nodeEnd || !verse && len > RESPONSE_CHARS)
					break;
				if(len > RESPONSE_CHARS) {
					// if inside verse, prolong range, and stop at the end of verse inside inner loop
					end = start+3;
					firstPass = false;
				} else if(len > 0) {
					int avgParaLen = len / cPara;
					int predict = ((RESPONSE_CHARS - len) / avgParaLen) + 1;
					end = start + predict;
				} else {
					end = start + 3*FIRST_FETCH_PARA_COUNT;
				}
			}
			db.last = !db.filtered && last >= nodeEnd-1 || last >= seg.paraNum-1 ? 0 : last+2;
			db.text = sb.toString();
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Text request", ex);
		}
		db.bookSegmentId = bookSegmentId;
		db.tocId = origTocNode.id;
		TocTree.refs(node, false, db);
		db.downtime = Globals.downtime;
		return db;
	}


	private DisplayBlock textForOnePara(TocTree toc, int bookSegmentId, int ordinal)
	{
		DisplayBlock db = new DisplayBlock();
		TocTreeItem tocNode = toc.findNodeByOrdinal(bookSegmentId, ordinal);
		db.bookSegmentId = bookSegmentId;
		db.tocId = tocNode.id;
		TocTree.refs(tocNode, true, db);
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
		Storage store = Storage.SYSTEM;

		StringBuilder sb = new StringBuilder((end-start) * 500);
		sb.append("<table class=\"hitlist-table\">");
		HitListEntry hitListEntry = new HitListEntry(80);
		for(int i = start; i<end; ++i) {
			Hit hit = details.hits.get(i);
			int bookSegmentId = hit.segment<<16 | hit.plainBookId;
			TocTreeItem tocNode = toc.findNodeByOrdinal(bookSegmentId, hit.ordinal);
			String shortRef = TocTree.refs(tocNode, false, null).shortRef;
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
		return p.cls != null && p.cls.verse;
	}

	private boolean boldParagraph(StoragePara p)
	{
		return p.cls.name().toLowerCase().contains("cim")  || p.cls == Forditas;
	}

	private Map<Integer, Search> getSessionSearchMap()
	{
		@SuppressWarnings("unchecked")
		Map<Integer, Search> smap = (Map<Integer, Search>) ri.ses.getAttribute("searchMap");
		if(smap == null)
		{
			smap = new HashMap<>();
			ri.ses.setAttribute("searchMap", smap);
		}
		return smap;
	}
}
