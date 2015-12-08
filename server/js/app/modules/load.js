
var page = require('./page').instance;
var util = require('./util');
var highlight = require('./highlight');
var render = require('./render');
var search = require('./search');
var toc = require('./toc');

/** @enum {number} - text request modes */
var loadMode = {section: 1, down: 2, next: 3, prev: 4, search: 5, nextHit: 6, prevHit: 7};
/** @type {?number} - timestamp of last request to throttle connections */
var lastReqTime;


/**
 * Sends ajax request to get text chunk. Starts rendering based on the
 * scroll direction that started the request.
 * @param {number} mode - one of {@link loadMode}
 */
function text(mode)
{
    /**
     * @returns {?string} - ajax request URI or null if no req needed
     */
    function getUrl()
    {
        var m = loadMode;
        var sectionUrl = '/app/txt/section/';
        var searchUrl = '/app/txt/search';
        switch(mode)
        {
            case m.section:
                return sectionUrl + 'go/' + toc.selectedSection();
            case m.next:
                return sectionUrl + 'next/' + page.tocId();
            case m.prev:
                return sectionUrl + 'prev/' + page.tocId();
            case m.down:
                var next = page.next();
                if(next === null)
                    return null;
                return '/app/txt/follow/'+page.tocId()+'/'+next;
            case m.search:
                data = {
                    q: search.pending().query()
                };
                return searchUrl;
            case m.nextHit:
            case m.prevHit:
                if(!search.get())
                    return null;
                var sr = search.get();
                var last = sr.last();
                var hit = last.hit + (mode == m.nextHit ? 1 : -1);
                if(hit >= 0 && hit < last.hitCount)
                    return searchUrl + '/hit/' + last.id + '/' + hit;
                return null;
        }
    }

    var data = null;
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

    if(mode === loadMode.search)
        highlight.init(search.pending().query());
}


function thisNextSection()
{
    if(page.isSearchResult())
    {
        toc.selectedSection(search.get().last().display.tocId);
        text(loadMode.section);
    }
    else if(page.bookId())
        text(loadMode.next);
}

$.extend(exports, {
    mode: loadMode,
    text: text,
    thisNextSection: thisNextSection
});
