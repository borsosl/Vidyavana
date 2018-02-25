
var dom = require('./dom');
var page = require('./page');
var util = require('./util');
var highlight = require('./highlight');
var render = require('./render');
var search = require('./search');
var toc = require('./toc');

/** @enum {number} - text request modes */
var loadMode = {section: 1, down: 2, next: 3, prev: 4, search: 5, currentHit: 6,
    nextHit: 7, prevHit: 8, bookmark: 9};
/** @type {?number} - timestamp of last request to throttle connections */
var lastReqTime;
/** @type {number} - id of bookmark to load */
var bookmarkId;


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
        var ps = page.section();
        switch(mode)
        {
            case m.section:
                return sectionUrl + 'go/' + toc.selectedSection();
            case m.next:
                if(!ps.bookId())
                    return null;
                return sectionUrl + 'next/' + ps.tocId();
            case m.prev:
                if(!ps.bookId())
                    return null;
                return sectionUrl + 'prev/' + ps.tocId();
            case m.down:
                var next = ps.next();
                if(next === null)
                    return null;
                return '/app/txt/follow/'+ps.tocId()+'/'+next;
            case m.search:
                var psr = search.pending();
                data = {
                    q: psr.query(),
                    sort: psr.sort(),
                    page: psr.page()
                };
                return searchUrl;
            case m.currentHit:
            case m.nextHit:
            case m.prevHit:
                var sr = search.inst();
                if(!sr)
                    return null;
                var last = sr.last();
                var hit = last.startHit + (mode == m.nextHit ? sr.page() : mode == m.prevHit ? -sr.page() : 0);
                if(hit < 0 && hit > -sr.page())     // visszafelé 0 és 1 oldalnyi közöttről indulva
                    hit = 0;
                if(hit >= 0 && hit < last.hitCount)
                    return searchUrl + '/hit/' + last.id + '/' + hit;
                return null;
            case m.bookmark:
                return '/app/bookmark/go/'+bookmarkId;
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
function hitSection(tocId) {
    toc.selectedSection(tocId);
    page.hits().scrollPos(dom.$content.scrollTop());
    page.hits().activeElement(document.activeElement);
    text(loadMode.section);
}

function currentHitSection() {
    hitSection(search.inst().last().display.tocId);
}

function contextSwitch() {
    if(page.isSearchResult()) {
        if(!search.isHitlist())
            currentHitSection();
    } else {
        var sr = search.inst();
        if(!sr)
            return null;
        page.current(page.hits());
        util.showHitsPanel();
        util.toggleButtonBars(true, search.isHitlist());
        var sp = page.hits().scrollPos();
        if(sp > -1)
            dom.$content.scrollTop(sp);
        var ae = page.hits().activeElement();
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

function bookmark(id) {
    bookmarkId = id;
    text(loadMode.bookmark);
}

$.extend(exports, {
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
});
