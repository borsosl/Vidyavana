
import dom from './dom';
import * as page from './page';
import * as util from './util';
import * as highlight from './highlight';
import * as render from './render';
import * as search from './search';
import * as toc from './toc';

/** text request modes */
const loadMode = {
    section: 1, down: 2, next: 3, prev: 4, search: 5, currentHit: 6,
    nextHit: 7, prevHit: 8, bookmark: 9
};
/** timestamp of last request to throttle connections */
let lastReqTime: number;
/** id of bookmark to load */
let bookmarkId: number;


/**
 * Sends ajax request to get text chunk. Starts rendering based on the
 * scroll direction that started the request.
 * @param mode - one of {@link loadMode}
 */
export function text(mode: number) {
    if(lastReqTime && lastReqTime > Date.now()-60000)
        return;
    const m = loadMode;
    let data = null;

    /**
     * @returns ajax request URI or null if no req needed
     */
    function getUrl(): string {
        const sectionUrl = '/app/txt/section/';
        const searchUrl = '/app/txt/search';
        const ps = page.section;
        switch(mode) {
            case m.section:
                return sectionUrl + 'go/' + toc.selectedSection();
            case m.next:
                if(!ps.bookId)
                    return null;
                return sectionUrl + 'next/' + ps.tocId;
            case m.prev:
                if(!ps.bookId)
                    return null;
                return sectionUrl + 'prev/' + ps.tocId;
            case m.down:
                const next = ps.next();
                if(next === null)
                    return null;
                return '/app/txt/follow/'+ps.tocId+'/'+next;
            case m.search:
                const psr = search.getPendingInstance();
                data = {
                    q: psr.query,
                    sort: psr.sort,
                    page: psr.page,
                    nodeFilter: psr.nodeFilter,
                    paraTypes: psr.paraTypes
                };
                return searchUrl;
            case m.currentHit:
            case m.nextHit:
            case m.prevHit:
                const sr = search.getInstance();
                if(!sr)
                    return null;
                const last = sr.last;
                let hit = last.startHit + (mode === m.nextHit ? sr.page : mode === m.prevHit ? -sr.page : 0);
                if(hit < 0 && hit > -sr.page)     // visszafelé 0 és 1 oldalnyi közöttről indulva
                    hit = 0;
                if(hit >= 0 && hit < last.hitCount)
                    return searchUrl + '/hit/' + last.id + '/' + hit;
                return null;
            case m.bookmark:
                return '/app/bookmark/go/'+bookmarkId;
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

export function prevSection() {
    text(loadMode.prev);
}

export function nextSection() {
    text(loadMode.next);
}

export function prevHit() {
    text(loadMode.prevHit);
}

export function nextHit() {
    text(loadMode.nextHit);
}

export function contextPrev() {
    if(page.isSearchResult())
        prevHit();
    else
        prevSection();
}

export function contextNext() {
    if(page.isSearchResult())
        nextHit();
    else
        nextSection();
}

export function continuation() {
    text(loadMode.down);
}

export function bookmark(id: number) {
    bookmarkId = id;
    text(loadMode.bookmark);
}

export {
    loadMode as mode,
    hitSection as hitlistClick
};
