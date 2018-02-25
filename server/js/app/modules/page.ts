
class Page {
    /** book id (segment # (eg. canto/lila) is << 16 bits) */
    private bookSegmentId: number;
    /** current TOC id */
    public tocId: number;
    /** 1-based index of the next, unloaded paragraph in current section.
     *      0=fully loaded section. -1=search render. */
    private last: number;
    /** saved scroll position for repositioning */
    public scrollPos?: number;
    /** saved active link of hitlist */
    public activeElement?: HTMLElement;

    public shortRef: string = '';


    /**
     * Sets fields for current page content.
     * @param json - loaded section info
     * @returns was reset
     */
    init(json: DisplayBlock): boolean
    {
        current = this;
        this.last = json.last;
        this.scrollPos = -1;
        this.activeElement = null;

        if(!json.tocId)
            return false;
        this.bookSegmentId = json.bookSegmentId;
        this.tocId = json.tocId;
        return true;
    }

    get bookId(): number {
        return this.bookSegmentId;
    }

    /**
     * Sets last request data.
     * @param json - loaded chunk and book info
     */
    down(json: DisplayBlock)
    {
        this.last = json.last;
    }


    /**
     * Gets next para ordinal, if section has more to load.
     * @return next para ordinal or null
     */
    next(): number
    {
        return this.last ? this.last : null;
    }
}

/** section display */
const section = new Page();

/** hits display */
const hits = new Page();

/** current display = section|hits */
let current: Page = section;


export default {
    section,
    hits,
    current: function(o?: Page) { if(o) current = o; return current; },
    isSearchResult: function(): boolean { return current === hits; }
};
