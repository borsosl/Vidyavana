package hu.vidyavana.search.model;

import hu.vidyavana.db.model.StoragePara;

import java.io.Serializable;

public class SearchRange implements Serializable {
    public int fromBook;
    public int fromSegment;
    public int fromOrdinal;

    public int toBook;
    public int toSegment;
    public int toOrdinal;

    public SearchRange(int fromBook, int fromSegment, int fromOrdinal, int toBook, int toSegment, int toOrdinal) {
        this.fromBook = fromBook;
        this.fromSegment = fromSegment;
        this.fromOrdinal = fromOrdinal;
        this.toBook = toBook;
        this.toSegment = toSegment;
        this.toOrdinal = toOrdinal;
    }

    public long fromRangeFilterOrdinal() {
        return StoragePara.rangeFilterOrdinal(fromBook, fromSegment, fromOrdinal);
    }

    public long toRangeFilterOrdinal() {
        return StoragePara.rangeFilterOrdinal(toBook, toSegment, toOrdinal);
    }
}
