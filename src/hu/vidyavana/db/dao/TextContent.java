package hu.vidyavana.db.dao;

import static hu.vidyavana.convert.api.ParagraphClass.*;
import hu.vidyavana.db.model.*;
import hu.vidyavana.web.RequestInfo;
import java.io.IOException;
import java.util.List;


public class TextContent
{
	public static final int SERVED_PARA_COUNT = 2;
	
	public void service(RequestInfo ri) throws Exception
	{
		synchronized(StorageRoot.SYSTEM)
		{
			StorageRoot.SYSTEM.openForRead();
		}
		ri.ajax = true;
		if("section".equals(ri.args[1]))
			section(ri);
		else if("next".equals(ri.args[1]))
			move(ri, false);
		else if("prev".equals(ri.args[1]))
			move(ri, true);
	}

	private void move(RequestInfo ri, boolean prev)
	{
		int bookId = Integer.parseInt(ri.args[2]);
		int start = Integer.parseInt(ri.args[3]);
		int end = start + SERVED_PARA_COUNT;
		if(prev)
		{
			end = start;
			start -= SERVED_PARA_COUNT;
		}
		ri.ajaxResult = text(bookId, start, end, false);
	}

	private void section(RequestInfo ri)
	{
		int id = Integer.parseInt(ri.args[2]);
		TocTreeItem node = TocTree.inst.findNodeById(id);
		int bookId = TocTree.inst.bookId(node);
		int ord = node.ordinal;
		if(ord < 0)
			ord = 1;
		ri.ajaxResult = text(bookId, ord, ord+SERVED_PARA_COUNT, true);
	}

	
	private DisplayBlock text(int bookId, int start, int end, boolean addLen)
	{
		DisplayBlock db = new DisplayBlock();
		StorageRoot sr = StorageRoot.SYSTEM;
		BookSegment seg = sr.segment(bookId);
		try
		{
			int show = start;
			if(show < 1)
				show = 1;
			--start;
			--end;
//			if(start < 5)
//				start = 0;
			if(start < 0)
				start = 0;
			db.book = bookId;
			db.first = start+1;
			db.show = show;
			if(addLen)
				db.paraNum = seg.paraNum;
			if(end > seg.paraNum-5)
				end = seg.paraNum;
			StringBuilder sb = new StringBuilder(50000);
			int last = start;
			int len = 0;
			boolean firstPass = true;
			boolean prevVerse = false;

			outer: while(true)
			{
				if(start > seg.paraNum)
				{
					if(firstPass)
						db.first = seg.paraNum;
					break;
				}
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
						if(len > 20000)
							break;
					}
					if(!prevVerse && verse)
						sb.append("<div class=\"VsWrap1\"><div class=\"VsWrap2\">");
					sb.append("<p class=\"").append(p.cls.name())
						.append("\" data-ix=\"").append(i).append("\">")
						.append(p.text).append("</p>");
					last = i;
					prevVerse = verse;
				}
				if(p == null || !verse)
					break;
				start = last+1;
				end = start+3;
				if(end > seg.paraNum)
					end = seg.paraNum;
				firstPass = false;
			}
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
