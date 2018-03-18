
interface DisplayBlock {
    bookSegmentId: number;
    tocId?: number;
    last: number;
    text: string;
    shortRef?: string;
    longRef?: string;
    downtime?: string;
}

interface HitResponse {
    shortRef: string;
}

interface SearchResponse {
    id: number;
    hitCount: number;
    startHit: number;
    endHit: number;
    ordinal: number;
    display: DisplayBlock;
    hits?: HitResponse[];
    errorText: string;
}

interface TocTreeItem {
    id: number;         // Ordinal in the whole TOC for each TOC node.
    parentStart: boolean;
    title: string;
    ordinal: number;
    parent: TocTreeItem;
    children: TocTreeItem[];
    partial: boolean;
    // client-only
    shortTitle: string;
}

type AjaxResultCallback<T> = (json: T) => void;

interface ContentPageData {
    skipRender?: boolean;
}

interface ContentPageResult<T extends ContentPageData> {
    html?: string;
    data?: T;
}

type BookSpanArray = BookMap[];
type Package = string|BookSpanArray;

/**
 * Properties are strings abbr|id|abbr|id|... from server,
 * converted to BookSpanArray on client.
 */
interface BookPackageMap {
    Sraddha: Package;
    SadhuSanga: Package;
    BhajanaKriya: Package;
    Ruci: Package;
    [key: string]: Package;
}
