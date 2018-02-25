
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
}

interface TocTreeItem {
    id: number;         // Ordinal in the whole TOC for each TOC node.
    parentStart: boolean;
    title: string;
    ordinal: number;
    parent: TocTreeItem;
    children: TocTreeItem[];
    partial: boolean;
}

interface AjaxResultCallback<T> {
    // noinspection JSUnusedLocalSymbols
    (json: T): void;
}
