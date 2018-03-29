package hu.vidyavana.search.util;

import hu.vidyavana.db.model.TocTree;
import hu.vidyavana.db.model.TocTreeItem;
import hu.vidyavana.search.model.SearchRange;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SearchRangeUtil {

    private static Pattern TOKEN = Pattern.compile("([+-])(\\d+)");

    private static class Boundary {
        int id;
        boolean start;
        int tocId;
        int book;
        int segment;
        int ordinal;

        public Boundary(int id, boolean start, int tocId, int book, int segment, int ordinal) {
            this.id = id;
            this.start = start;
            this.tocId = tocId;
            this.book = book;
            this.segment = segment;
            this.ordinal = ordinal;
        }
    }

    public static List<SearchRange> nodeFilterStringToSearchRangeList(String nodeFilter, TocTree toc) {
        if(nodeFilter == null)
            return null;
        nodeFilter = nodeFilter.trim();
        if(nodeFilter.isEmpty())
            return null;

        List<Boundary> bounds = new ArrayList<>();
        boolean baseAll = nodeFilter.startsWith("all");
        int counter = 0;
        if(baseAll) {
            bounds.add(new Boundary(++counter, true, 1, 1, 0, 0));
            bounds.add(new Boundary(++counter, false, toc.maxId, Short.MAX_VALUE, 0, 0));
        }
        Matcher m = TOKEN.matcher(nodeFilter);
        while(m.find()) {
            boolean add = m.group(1).equals("+");
            int tocId = Integer.valueOf(m.group(2));
            TocTreeItem node = toc.findNodeById(tocId);
            int bookSegmentId = toc.bookSegmentId(node);
            int plainBookId = bookSegmentId & ((1<<16)-1);
            int segment = bookSegmentId >> 16;
            Boundary boundary1 = new Boundary(++counter, add, tocId, plainBookId, segment, node.ordinal < 0 ? 0 : node.ordinal);
            bounds.add(boundary1);

            node = toc.nextSibling(node);
            if(node != null) {
                bookSegmentId = toc.bookSegmentId(node);
                plainBookId = bookSegmentId & ((1<<16)-1);
                segment = bookSegmentId >> 16;
                Boundary boundary = new Boundary(++counter, !add, node.id, plainBookId, segment, node.ordinal < 0 ? 0 : node.ordinal);
                bounds.add(boundary);
            } else {
                Boundary boundary = new Boundary(++counter, !add, toc.maxId, Short.MAX_VALUE, 0, 0);
                bounds.add(boundary);
            }
        }

        bounds.sort(Comparator.comparingInt(o -> o.tocId));

        List<SearchRange> ranges = new ArrayList<>();
        Boundary start = null, end, prevEnd = null;
        for(Boundary bound : bounds) {
            if(start == null) {
                start = bound;
                if (!start.start)
                    start = null;
            } else {
                end = bound;
                if(!end.start) {
                    if(prevEnd != null && start.tocId - prevEnd.tocId <= 2) {
                        SearchRange prev = ranges.get(ranges.size() - 1);
                        prev.toBook = end.book;
                        prev.toSegment = end.segment;
                        prev.toOrdinal = end.ordinal;
                        prevEnd = end;
                    } else if(end.tocId - start.tocId > 2) {
                        ranges.add(new SearchRange(start.book, start.segment, start.ordinal, end.book, end.segment, end.ordinal));
                        prevEnd = end;
                    }
                    start = null;
                }
            }
        }
        return ranges;
    }

    public static int paraTypesBits(String types) {
        return types.chars().reduce(0, (sum, numberAscii) -> sum + (1 << (numberAscii-48)));
    }

}
