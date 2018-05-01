import * as load from './load';

interface FilteredDisplayHistoryItem {
    bookSegmentId: number;
    ordinal: number;
    last: number;
}

export let filteredDisplayHistoryItem: FilteredDisplayHistoryItem[] = [];

class Page {
    /** book id (segment # (eg. canto/lila) is << 16 bits) */
    private bookSegmentId: number;
    /** current TOC id */
    public tocId: number;
    /** true if not all paragraph types are fetched to display */
    public filtered: boolean;
    /** 1-based index of the start para ordinal, for filtered backtracking.*/
    public first: number;
    /** 1-based index of the next, unloaded paragraph in current section.
      *      0=fully loaded section. -1=search render.*/
    public last: number;
    /** saved scroll position for repositioning */
    public scrollPos?: number;
    /** saved active link of hitlist */
    public activeElement?: HTMLElement;

    public shortRef: string = '';


    /**
     * Sets fields for current page content.
     * @param json - loaded section info
     * @param mode - load mode
     * @returns was reset
     */
    init(json: DisplayBlock, mode: number): boolean {
        current = this;
        this.filtered = json.filtered;
        this.first = json.first;
        this.last = json.last;
        this.scrollPos = -1;
        this.activeElement = null;

        if(!json.tocId)
            return false;
        this.bookSegmentId = json.bookSegmentId;
        this.tocId = json.tocId;

        if(this.filtered) {
            const historyItem = {
                bookSegmentId: json.bookSegmentId,
                ordinal: json.first,
                last: json.last
            };
            if(mode >= load.mode.filterStart && mode <= load.mode.filterPrev) {
                const len = filteredDisplayHistoryItem.length;
                if(len === 0 || json.last !== 0 ||
                        filteredDisplayHistoryItem[len-1].last !== 0) {
                    filteredDisplayHistoryItem.push(historyItem);
                    if(len > 99)
                        filteredDisplayHistoryItem.shift();
                }
            } else {
                filteredDisplayHistoryItem = [historyItem];
            }
        }
        return true;
    }

    get bookId(): number {
        return this.bookSegmentId;
    }

    /**
     * Sets last request data.
     * @param json - loaded chunk and book info
     */
    down(json: DisplayBlock) {
        this.last = json.last;
    }


    /**
     * Gets next para ordinal, if section has more to load.
     * @return next para ordinal or null
     */
    downOrdinal(): number {
        return this.last && !this.filtered ? this.last : null;
    }

    isBackAvailable(): boolean {
        return !this.filtered || filteredDisplayHistoryItem.length > 1;
    }
}

/** section display */
export const section = new Page();

/** hits display */
export const hits = new Page();

/** current display = section|hits */
let current: Page = section;

function currentPage(page?: Page) {
    if(page)
        current = page;
    return current;
}

export function isSearchResult(): boolean {
    return current === hits;
}

export function prevFilteredPage() {
    filteredDisplayHistoryItem.pop();
    return filteredDisplayHistoryItem.pop();
}

export {
    currentPage as current
};
