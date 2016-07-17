
var page = require('./page').instance;
var util = require('./util');
var highlight = require('./highlight');
var render = require('./render');
var search = require('./search');
var toc = require('./toc');

/** @enum {number} - text request modes */
var loadMode = {section: 1, down: 2, next: 3, prev: 4, search: 5, currentHit: 6, nextHit: 7, prevHit: 8};
/** @type {?number} - timestamp of last request to throttle connections */
var lastReqTime;


/**
 * Sends ajax request to get text chunk. Starts rendering based on the
 * scroll direction that started the request.
 * @param {number} mode - one of {@link loadMode}
 */
function text(mode) {
    if(lastReqTime && lastReqTime > Date.now()-60000)
        return;
    var m = loadMode;
    var data = null;

    /**
     * @returns {?string} - ajax request URI or null if no req needed
     */
    function getUrl()
    {
        var sectionUrl = '/app/txt/section/';
        var searchUrl = '/app/txt/search';
        switch(mode)
        {
            case m.section:
                return sectionUrl + 'go/' + toc.selectedSection();
            case m.next:
                if(!page.bookId())
                    return null;
                return sectionUrl + 'next/' + page.tocId();
            case m.prev:
                if(!page.bookId())
                    return null;
                return sectionUrl + 'prev/' + page.tocId();
            case m.down:
                var next = page.next();
                if(next === null)
                    return null;
                return '/app/txt/follow/'+page.tocId()+'/'+next;
            case m.search:
                var ps = search.pending();
                data = {
                    q: ps.query(),
                    sort: ps.sort(),
                    page: ps.page()
                };
                return searchUrl;
            case m.currentHit:
            case m.nextHit:
            case m.prevHit:
                var sr = search.get();
                if(!sr)
                    return null;
                var last = sr.last();
                var hit = last.startHit + (mode == m.nextHit ? sr.page() : mode == m.prevHit ? -sr.page() : 0);
                if(hit < 0 && hit > -sr.page())     // visszafelé 0 és 1 oldalnyi közöttről indulva
                    hit = 0;
                if(hit >= 0 && hit < last.hitCount)
                    return searchUrl + '/hit/' + last.id + '/' + hit;
                return null;
        }
    }

    var url = getUrl();
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
        highlight.init(search.pending().query());
}

/** @param {number} tocId */
function loadSection(tocId) {
    toc.selectedSection(tocId);
    text(loadMode.section);
}

function currentHitSection() {
    loadSection(search.get().last().display.tocId);
}

function currentHits() {
    text(loadMode.currentHit);
}

function contextSwitch() {
    if(page.isSearchResult()) {
        if(!search.isHitlist())
            currentHitSection();
    } else
        currentHits();
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

$.extend(exports, {
    mode: loadMode,
    text: text,
    hitlistClick: loadSection,
    currentHitSection: currentHitSection,
    currentHits: currentHits,
    contextSwitch: contextSwitch,
    prevSection: prevSection,
    nextSection: nextSection,
    prevHit: prevHit,
    nextHit: nextHit,
    contextPrev: contextPrev,
    contextNext: contextNext,
    continuation: continuation
});
