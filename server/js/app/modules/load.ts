
import dom from './dom';
import page from './page';
import util from './util';
import highlight from './highlight';
import render from './render';
import search from './search';
import toc from './toc';

/** text request modes */
const loadMode: {[key: string]: number} = {
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
function text(mode: number) {
    if(lastReqTime && lastReqTime > Date.now()-60000)
        return;
    const m = loadMode;
    let data = null;

    /**
     * @returns ajax request URI or null if no req needed
     */
    function getUrl(): string
    {
        const sectionUrl = '/app/txt/section/';
        const searchUrl = '/app/txt/search';
        const ps = page.section;
        switch(mode)
        {
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
                const psr = search.pending();
                data = {
                    q: psr.query,
                    sort: psr.sort,
                    page: psr.page
                };
                return searchUrl;
            case m.currentHit:
            case m.nextHit:
            case m.prevHit:
                let sr = search.inst();
                if(!sr)
                    return null;
                const last = sr.last;
                let hit = last.startHit + (mode == m.nextHit ? sr.page : mode == m.prevHit ? -sr.page : 0);
                if(hit < 0 && hit > -sr.page)     // visszafelé 0 és 1 oldalnyi közöttről indulva
                    hit = 0;
                if(hit >= 0 && hit < last.hitCount)
                    return searchUrl + '/hit/' + last.id + '/' + hit;
                return null;
            case m.bookmark:
                return '/app/bookmark/go/'+bookmarkId;
        }
    }

    let url = getUrl();
    if(!url)
        return;
    lastReqTime = Date.now();

    $.ajax({
        url: url,
        dataType: 'json',
        data: data,

        success: function(json)
        {
            lastReqTime = null;
            if(!util.javaError(json))
                render.text(json, mode);
        },

        error: function(/*xhr, status*/)
        {
            lastReqTime = null;
            util.ajaxError(/*xhr, status,*/ 'Hiba a szöveg letöltésekor.', function()
            {
                text(mode);
            });
        }
    });
    util.loading(true);

    if(mode === loadMode.search)
        highlight.init(search.pending().query);
}

function hitSection(tocId: number) {
    toc.selectedSection(tocId);
    page.hits.scrollPos = dom.$content.scrollTop();
    page.hits.activeElement = document.activeElement as HTMLElement;
    text(loadMode.section);
}

function currentHitSection() {
    hitSection(search.inst().last.display.tocId);
}

/** Switches b/w text content and search result */
function contextSwitch(): void {
    if(page.isSearchResult()) {
        if(!search.isHitlist())
            currentHitSection();
    } else {
        let sr = search.inst();
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

function prevSection() {
    text(loadMode.prev);
}

function nextSection() {
    text(loadMode.next);
}

function prevHit() {
    text(loadMode.prevHit);
}

function nextHit() {
    text(loadMode.nextHit);
}

function contextPrev() {
    if(page.isSearchResult())
        prevHit();
    else
        prevSection();
}

function contextNext() {
    if(page.isSearchResult())
        nextHit();
    else
        nextSection();
}

function continuation() {
    text(loadMode.down);
}

function bookmark(id: number) {
    bookmarkId = id;
    text(loadMode.bookmark);
}

export default {
    mode: loadMode,
    text: text,
    hitlistClick: hitSection,
    currentHitSection: currentHitSection,
    contextSwitch: contextSwitch,
    prevSection: prevSection,
    nextSection: nextSection,
    prevHit: prevHit,
    nextHit: nextHit,
    contextPrev: contextPrev,
    contextNext: contextNext,
    continuation: continuation,
    bookmark: bookmark
};
