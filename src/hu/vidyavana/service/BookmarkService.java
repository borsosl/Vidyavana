package hu.vidyavana.service;

import com.google.gson.GsonBuilder;
import hu.vidyavana.db.dao.BookmarkDao;
import hu.vidyavana.db.model.Bookmark;
import hu.vidyavana.db.model.TocTree;
import hu.vidyavana.db.model.TocTreeItem;
import hu.vidyavana.web.RequestInfo;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class BookmarkService {

    public static String FOLLOWED_BOOKMARK_KEY = "followBm";

    public static class BookmarksResult {
        public int allCount;
        public int filteredCount;
        public List<Integer> recent10;
        public List<Integer> filtered100;
        public Map<Integer, Bookmark> recentEntityMap;
        public Map<Integer, Bookmark> filteredEntityMap;
        public String filter;
        public boolean skipRender;
    }

    private RequestInfo ri;

    public BookmarkService(RequestInfo ri) {
        this.ri = ri;
    }

    public void page(String filter)
    {
        try
        {
            String html = ri.getTemplate("/dialog/bookmarks.html");
            if(filter == null)
                filter = ri.req.getParameter("filter");
            BookmarksResult data = initPage(filter);
            ri.renderAjaxTemplateString(html, data);
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private BookmarksResult initPage(String filter) {
        BookmarksResult res = new BookmarksResult();
        List<Bookmark> recent10 = BookmarkDao.getRecent10(ri.user.id);
        res.allCount = BookmarkDao.count(ri.user.id);
        res.recent10 = recent10.stream().map(bm -> bm.id).collect(toList());
        res.recentEntityMap = recent10.stream()
                .collect(toMap(bm -> bm.id, Function.identity()));
        addFilteredBookmarks(filter, res);
        res.filter = filter;
        res.skipRender = true;
        return res;
    }

    private void addFilteredBookmarks(String filter, BookmarksResult res) {
        if(filter == null || (filter = filter.trim()).isEmpty())
            return;
        if(filter.equals("*"))
            filter = "";
        List<Bookmark> filtered100 = BookmarkDao.getFiltered100(ri.user.id, filter);
        res.filteredCount = filtered100.size() < 100 ? filtered100.size() : BookmarkDao.countFiltered(ri.user.id, filter);
        res.filtered100 = filtered100.stream().map(bm -> bm.id).collect(toList());
        res.filteredEntityMap = filtered100.stream()
                .collect(toMap(bm -> bm.id, Function.identity()));
    }

    public void gotoBookmark() {
        int id = Integer.parseInt(ri.args[2]);
        Bookmark bookmark = BookmarkDao.findById(id);
        if(bookmark.userId != ri.user.id) {
            ri.resp.setStatus(404);
            return;
        }

        BookmarkDao.updateLastUsed(id);
        if(bookmark.follow)
            ri.ses.setAttribute(FOLLOWED_BOOKMARK_KEY, id);
        else
            ri.ses.removeAttribute(FOLLOWED_BOOKMARK_KEY);

        ri.toc = TocTree.getView(ri.user);
        TocTreeItem node = ri.toc.findNodeByOrdinal(bookmark.bookSegmentId, bookmark.ordinal);
        while(node.prev != null && node.prev == node.parent &&
                (node.prev.ordinal < 0 || node.prev.ordinal >= node.ordinal-3))
            node = node.prev;
        id = ri.toc.checkTocIdRange(node.id, true);
        if(id != node.id)
            node = ri.toc.findNodeById(id);
        int ord = node.ordinal;
        if(ord < 0)
            ord = 1;
        ri.ajaxResult = new TextContentService(ri).text(ri.toc, node, ord);
    }

    public void filter() {
        String filter = ri.req.getParameter("filter");
        BookmarksResult res = new BookmarksResult();
        addFilteredBookmarks(filter, res);
        ri.ajaxResult = res;
    }

    public void save() {
        String json = ri.req.getParameter("bookmark");
        Bookmark bookmark = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .fromJson(json, Bookmark.class);
        bookmark.userId = ri.user.id;
        if(bookmark.ordinal < 0) {
            int tocId = -bookmark.ordinal;
            ri.toc = TocTree.getView(ri.user);
            bookmark.ordinal = ri.toc.findNodeById(tocId).ordinal;
        }
        if(bookmark.id == 0)
            BookmarkDao.insert(bookmark);
        else {
            if(bookmark.bookSegmentId == 0) {
                Bookmark saved = BookmarkDao.findById(bookmark.id);
                bookmark.bookSegmentId = saved.bookSegmentId;
                bookmark.ordinal = saved.ordinal;
                bookmark.shortRef = saved.shortRef;
            }
            BookmarkDao.update(bookmark);
        }
        page(ri.req.getParameter("filter"));
    }

    public void delete() {
        int id = Integer.parseInt(ri.req.getParameter("id"));
        BookmarkDao.delete(id, ri.user.id);
        page(ri.req.getParameter("filter"));
    }

    public static void updateFollowedBookmark(RequestInfo ri, int bookSegmentId, int ordinal, String shortRef) {
        Integer id = (Integer) ri.ses.getAttribute(FOLLOWED_BOOKMARK_KEY);
        if(id != null)
            BookmarkDao.updateFollowed(id, ri.user.id, bookSegmentId, ordinal, shortRef);
    }

}
