
import dom from './dom';
import * as page from './page';
import * as util from './util';
import * as highlight from './highlight';
import * as render from './render';
import * as search from './search';
import * as toc from './toc';
import * as view from './view';

/** text request modes */
const enum loadMode {
    section, down, next, prev, search, currentHit, nextHit,
    prevHit, bookmark, filterStart, filterNext, filterPrev
}
/** timestamp of last request to throttle connections */
let lastReqTime: number;
/** id of bookmark to load */
let bookmarkId: number;

const sectionUrl = '/app/txt/section/';
const filterUrl = '/app/txt/filter/';
const searchUrl = '/app/txt/search';


/**
 * Sends ajax request to get text chunk. Starts rendering based on the
 * scroll direction that started the request.
 * @param mode - one of {@link loadMode}
 */
export function text(mode: number) {
    if(lastReqTime && lastReqTime > Date.now()-60000)
        return;
    let data = null;

    /**
     * @returns ajax request URI or null if no req needed
     */
    function getUrl(): string {
        const ps = page.section;
        switch(mode) {
            case loadMode.section:
                return sectionUrl + 'go/' + toc.selectedSection() + view.urlSegment();
            case loadMode.next:
                return sectionUrl + 'next/' + ps.tocId + view.urlSegment();
            case loadMode.prev:
                return sectionUrl + 'prev/' + ps.tocId + view.urlSegment();
            case loadMode.filterStart:
                return filterUrl + ps.bookId + '/' + ps.first + view.urlSegment();
            case loadMode.filterNext:
                return filterUrl + ps.bookId + '/' + ps.last + view.urlSegment();
            case loadMode.filterPrev:
                const prev = page.prevFilteredPage();
                return filterUrl + prev.bookSegmentId + '/' + prev.ordinal + view.urlSegment();
            case loadMode.down:
                const next = ps.downOrdinal();
                if(next === null)
                    return null;
                return '/app/txt/follow/'+ps.tocId+'/'+next;
            case loadMode.search:
                const psr = search.getPendingInstance();
                data = {
                    q: psr.query,
                    sort: psr.sort,
                    page: psr.page,
                    nodeFilter: psr.nodeFilter,
                    paraTypes: psr.paraTypes
                };
                return searchUrl;
            case loadMode.currentHit:
            case loadMode.nextHit:
            case loadMode.prevHit:
                const sr = search.getInstance();
                if(!sr)
                    return null;
                const last = sr.last;
                let hit = last.startHit + (mode === loadMode.nextHit ? sr.page :
                    mode === loadMode.prevHit ? -sr.page : 0);
                if(hit < 0 && hit > -sr.page)     // visszafelé 0 és 1 oldalnyi közöttről indulva
                    hit = 0;
                if(hit >= 0 && hit < last.hitCount)
                    return searchUrl + '/hit/' + last.id + '/' + hit;
                return null;
            case loadMode.bookmark:
                return '/app/bookmark/go/'+bookmarkId+view.urlSegment();
        }
    }

    const url = getUrl();
    if(!url)
        return;
    lastReqTime = Date.now();

    $.ajax({
        url,
        dataType: 'json',
        data,

        success(json) {
            lastReqTime = null;
            if(!util.javaError(json))
                render.text(json, mode);
        },

        error(/*xhr, status*/) {
            lastReqTime = null;
            util.ajaxError(/*xhr, status,*/ 'Hiba a szöveg letöltésekor.', function() {
                text(mode);
            });
        }
    });
    util.loading(true);

    if(mode === loadMode.search)
        highlight.init(search.getPendingInstance().query);
}

function hitSection(tocId: number) {
    toc.selectedSection(tocId);
    page.hits.scrollPos = dom.$content.scrollTop();
    page.hits.activeElement = document.activeElement as HTMLElement;
    text(loadMode.section);
}

export function currentHitSection() {
    hitSection(search.getInstance().last.display.tocId);
}

/** Switches b/w text content and search result */
export function contextSwitch(): void {
    if(page.isSearchResult()) {
        if(!search.isHitlist())
            currentHitSection();
        else if(page.section.tocId) {
            util.showSectionPanel();
            page.current(page.section);
        }
    } else {
        const sr = search.getInstance();
        if(!sr)
            return;
        page.current(page.hits);
        util.showHitsPanel();
        util.toggleButtonBars(true, search.isHitlist());
        const sp = page.hits.scrollPos;
        if(sp > -1)
            dom.$content.scrollTop(sp);
        const ae = page.hits.activeElement;
        if(ae) {
            ae.focus();
        }
    }
}

export function contextPrev() {
    if(page.isSearchResult())
        text(loadMode.prevHit);
    else if(page.section.bookId) {
        if(page.section.filtered) {
            if(page.section.isBackAvailable())
                text(loadMode.filterPrev);
        } else
            text(loadMode.prev);
    }
}

export function contextNext() {
    if(page.isSearchResult())
        text(loadMode.nextHit);
    else if(page.section.bookId) {
        if(page.section.filtered)
            text(loadMode.filterNext);
        else
            text(loadMode.next);
    }
}

export function continuation() {
    text(loadMode.down);
}

export function filterStart() {
    text(loadMode.filterStart);
}

export function bookmark(id: number) {
    bookmarkId = id;
    text(loadMode.bookmark);
}

export {
    loadMode as mode,
    hitSection as hitlistClick
};
