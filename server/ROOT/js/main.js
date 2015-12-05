// internal singletons
/** @type {Page} */
var page;
/** @type {Search} */
var search, pendingSearch;
/** @type {Highlight} */
var highlight;
/** @enum {number} - text request modes */
var loadMode = {section: 1, down: 2, next: 3, prev: 4, search: 5, nextHit: 6, prevHit: 7};
/** @type {JQuery} - points to centered message box */
var $msg;
/**
 * Stores info about current section selection.
 * @type {{sel:HTMLSelectElement, level:number, id:number, node:TocTreeItem}}
 */
var nodeToUpdate;
/** @type {number} - TOC node id currently selected in UI. */
var selSection;
/** @type {JQuery} - container for text and buttons */
var $content;
/** @type {JQuery} - container for text content */
var $txt;
/** @type {JQuery} - button rows */
var $textBtns, $hitBtns;
/** @type {JQuery} - buttons to load more of section or go to full section */
var $sectDown, $thisSect;
/** @type {number} - last section of the database */
var maxTocId;
/** @type {boolean} - if a search-related message is visible */
var searchMsgShown;
/** @type {number} - timestamp of last request to throttle connections */
var lastReqTime;


/**
 * Sends ajax request to get text chunk. Starts rendering based on the
 * scroll direction that started the request.
 * @param {number} mode - one of {@link loadMode}
 */
function loadText(mode)
{
    if(lastReqTime && lastReqTime > Date.now()-60000)
        return;
    var m = loadMode;
    var data = null;

    /**
     * @returns {?string} - ajax request URI or null if no req needed
     */
    function loadModeUrl()
    {
        var sectionUrl = '/app/txt/section/';
        var searchUrl = '/app/txt/search';
        switch(mode)
        {
            case m.section:
                return sectionUrl + 'go/' + selSection;
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
                    q: pendingSearch.query()
                };
                return searchUrl;
            case m.nextHit:
            case m.prevHit:
                if(!search)
                    return null;
                var last = search.last();
                var hit = last.hit + (mode == m.nextHit ? 1 : -1);
                if(hit >= 0 && hit < last.hitCount)
                    return searchUrl + '/hit/' + last.id + '/' + hit;
                return null;
        }
    }

    var url = loadModeUrl();
    if(!url)
        return;
    lastReqTime = Date.now();

    $.ajax(
    {
        url: url,
        dataType: 'json',
        data: data,

        success: function(json)
        {
            lastReqTime = null;
            if(!javaError(json))
                renderText(json, mode);
        },

        error: function(/*xhr, status*/)
        {
            lastReqTime = null;
            ajaxError(/*xhr, status,*/ 'Hiba a szöveg letöltésekor.', function()
            {
                loadText(mode);
            });
        }
    });

    if(mode === m.search)
        highlight = new Highlight(pendingSearch.query());
}


/**
 * On successful load, add text into DOM.
 * @param {DisplayBlock|SearchResponse} json - loaded text details
 * @param {number} mode - one of {@link loadMode}
 */
function renderText(json, mode)
{
    var isSearch = json.hitCount !== undefined;
    if(isSearch)
    {
        if(json.hitCount)
        {
            if(mode == loadMode.search)
            {
                search = pendingSearch;
                dialog(-1, false);
            }
            search.last(json);
        }
        else
        {
            $('#search-msg').text('Nincs találat.').show();
            searchMsgShown = true;
            return;
        }
    }
    var display = isSearch ? json.display : json;
    var initPage = mode != loadMode.down;
    if(initPage)
        page.init(display, false);
    page.load(display);
    var h = display.text;
    if(h)
    {
        if(initPage)
        {
            $content.scrollTop(0);
            if(isSearch)
                $txt.html('<div class="long-ref">'+(json.hit+1)+' / '+json.hitCount+' : '+display.longRef+'</div>'+h);
            else
                $txt.html(h);
            $textBtns.toggle(!isSearch);
            $hitBtns.toggle(!!search);
        }
        else
            $txt.append(h);
        $sectDown.toggle(page.next() !== null);
        $thisSect.toggle(page.isSearchResult());
        if(highlight)
            highlight.run(h);
    }
}


/**
 * @constructor
 */
function Page()
{
    /**
     * @type {number} - book id (segment # (eg. canto/lila) is << 16 bits)
     */
    var bookId;
    /**
     * @type {number} - current TOC id
     */
    var tocId;
    /**
     * @type {number} - 1-based index of the next, unloaded paragraph. 0=fully loaded. -1=search render.
     */
    var last;


    /**
     * Sets fields for current page content.
     * @param {DisplayBlock} json - loaded section info
     * @param {boolean} force - force reload even if the same section was loaded
     * @returns {boolean} - was reset
     */
    function init(json, force)
    {
        if(!json.tocId && !force)
            return false;
        bookId = json.bookId;
        tocId = json.tocId;
        last = 0;
        return true;
    }


    /**
     * Sets last request data.
     * @param {DisplayBlock} json - loaded chunk and book info
     */
    function load(json)
    {
        last = json.last;
    }


    /**
     * Gets next para ordinal, if section has more to load.
     * @return {?number} - next para or null
     */
    function next()
    {
        return last ? last : null;
    }


    $.extend(this, {
        init: init,
        load: load,
        next: next,
        isSearchResult: function(){return last === -1;},
        bookId: function(){return bookId;},
        tocId: function(){return tocId;}
    });
}

// ********** Search **********


/**
 * @constructor
 */
function Search()
{
    /**
     * @type {string} - originally entered text
     */
    var query;
    /**
     * @type {SearchResponse} - details of last hit shown
     */
    var last;


    $.extend(this, {
        query: function(q){if(q==undefined) return query; query = q;},
        last: function(l){if(l==undefined) return last; last = l;}
    });
}


/**
 * One-time setup of event handlers.
 */
function initSearch()
{
    var $inp = $('#searchInput');
    $inp.keydown(function(/** @type JQueryKeyEventObject */ e)
    {
        if(searchMsgShown)
        {
            $('#search-msg').hide();
            searchMsgShown = false;
        }
        if(e.keyCode == 13)
        {
            newSearch($inp.val());
        }
        if(!menuModifier(e))
            e.stopPropagation();
    });
    $('#searchGo').click(function()
    {
        newSearch($inp.val());
    });
}


function newSearch(text)
{
    if(searchMsgShown)
    {
        $('#search-msg').hide();
        searchMsgShown = false;
    }
    var ps = pendingSearch = new Search();
    ps.query(text);
    loadText(loadMode.search);
}


//********** TOC data **********


/**
 * Get unloaded children of selected node. Redraw selects when they are available.
 */
function updateTocNode()
{
    var o = nodeToUpdate;
    if(o.node.partial)
    {
        getTocChildren(o.id, updateTocNode, function(fullNode)
        {
            o.node.children = fullNode.children;
            o.node.partial = false;
            replacePipes(o.node);
            updateTocNode();
        });
        return;
    }
    nodeToUpdate = null;
    updateSectionSelects(o.node, o.level+1);
}


/**
 * Traverse loaded TOC nodes to find TOC id.
 * @param {TocTreeItem} parent - root to start search from
 * @param {number} id - TOC id to find
 * @return {TocTreeItem} - found node or its parent
 */
function findTocNodeById(parent, id)
{
    /** @type Array.<TocTreeItem> */
    var ch = parent.children;
    if(ch == null)
        return parent;
    var len = ch.length;
    for(var i=0; i<len; ++i)
    {
        /** @type TocTreeItem */
        var ti = ch[i];
        if(id > ti.id)
            continue;
        if(id < ti.id && i > 0)
            return findTocNodeById(ch[i-1], id);
        return ti;
    }
    return findTocNodeById(ch[len-1], id);
}


/**
 * Loads node and children of a TOC id.
 * @param {number} id - TOC id
 * @param {function} retryFn - on failed ajax, call to retry
 * @param {function} cb - call back on success
 */
function getTocChildren(id, retryFn, cb)
{
    $.ajax(
        {
            url: '/app/toc/get/'+id,
            dataType: 'json',

            success: function(json)
            {
                if(!javaError(json))
                    cb.call(this, json);
            },

            error: function(/*xhr, status*/)
            {
                ajaxError(/*xhr, status,*/ 'Hiba a tartalomjegyzék ág letöltésekor.', retryFn);
            }
        });
}


/**
 * Replaces pipe characters in TOC node text.
 * @param {TocTreeItem} node
 */
function replacePipes(node)
{
    var ch = node.children;
    var rex = /ǀ/;
    for(var i in ch)
    {
        var it = ch[i];
        it.title = it.title.replace(rex, ' – ');
        if(it.children)
            replacePipes(it);
    }
}


// ********** Section select **********


/**
 * One-time setup of event handlers.
 */
function initSectionSelect()
{
    replacePipes(pg.toc);
    updateSectionSelects(pg.toc, 1);
    maxTocId = pg.maxTocId;

    // event handlers
    $('.sectionSelect').change(/** @this HTMLSelectElement */ function()
    {
        var level = parseInt(this.id.substr(4));
        var id = parseInt(this.value);
        var node = findTocNodeById(pg.toc, id);
        nodeToUpdate = {
            sel: this,
            level: level,
            id: id,
            node: node
        };
        updateTocNode();
    });
    $('#sectionGo').click(gotoSection);
}


/**
 * Sets section select boxes from a level downwards.
 * @param {TocTreeItem} parent - the children of which are a level's items
 * @param {number} level - to update, with child levels
 */
function updateSectionSelects(parent, level)
{
    while(parent)
    {
        /** @type {Array.<TocTreeItem>} */
        var ch = parent.children;
        selSection = parent.id;
        if(parent.parentStart)
            --selSection;
        if(!ch)
            break;
        var $e = $('#sect'+(level++));
        var opt = [];
        for(var i in ch)
        {
            if(!ch.hasOwnProperty(i))
                continue;
            var it = ch[i];
            opt.push('<option value="', it.id, '">', it.title, '</option>');
        }
        $e.html(opt.join(''));
        $e.show();
        parent = ch[0];
    }
    while(level <= 9)
    {
        $('#sect'+(level++)).hide();
    }
}


/**
 * Start loading of selected section.
 */
function gotoSection()
{
    $('#sectionPop').hide();
    loadText(loadMode.section);
}


//********** Util **********


/**
 * Shows server-side error and returns its presence.
 * @param {Object} json - result of any ajax
 * @return {boolean} - error happened
 */
function javaError(json)
{
    if(json.error)
        message(json.error);
    return !!json.error;
}


/**
 * Show error dialog.
 * @param {string} msg - error text
 * @param {function} retryFn - callback for retrying operation that had failed
 */
function ajaxError(/*xhr, status,*/ msg, retryFn)
{
    message(msg + '...<br><a href="#" id="retry">Ismétlés</a>&nbsp;&nbsp;<a href="#" id="cancelMsg">Mégse</a>');
    $('#retry', $msg).click(function()
    {
        retryFn.call(this);
    });
    $('#cancelMsg', $msg).click(function()
    {
        $msg.hide();
    });
}


/**
 * Set message text and reposition its window.
 * @param {string} msg - message
 */
function message(msg)
{
    if(!$msg)
        $msg = $('#message');
    $msg.html(msg + '&nbsp;<a href="#" id="cancelMsg">Bezár</a>');
    $('#cancelMsg', $msg).click(function()
    {
        $msg.hide();
    });
    var $win = $(window);
    $msg.css({
        'top': Math.floor(($win.height() - $msg.height() - 20)/2) + 'px',
        'left': Math.floor(($win.width() - $msg.width() - 20)/2) + 'px',
        'display': 'block'
    });
}


function throttle(init, delay, cb)
{
    var timer;

    if(init)
        cb.call();
    return function()
    {
        clearTimeout(timer);
        timer = setTimeout(cb, delay);
    };
}


/**
 * Shows/hides dialogs.
 * @param {number} index - of dialog in ids array
 * @param {boolean} toggle - or show
 */
function dialog(index, toggle)
{
    var ids = [$('#searchPop'), $('#sectionPop')];
    for(var i in ids)
        if(i == index)
            if(toggle)
                ids[i].toggle();
            else
                ids[i].show();
        else
            ids[i].hide();
}


function menuModifier(e)
{
    return e.altKey && (!client.system.mac || e.ctrlKey);
}


function thisNextSection()
{
    if(page.isSearchResult())
    {
        selSection = search.last().display.tocId;
        loadText(loadMode.section);
    }
    else if(page.bookId())
        loadText(loadMode.next);
}


$(function()
{
    tests();
    $content = $('#content');
    $txt = $('#text');
    $textBtns = $('#text-buttons');
    $hitBtns = $('#hit-buttons');
    $sectDown = $('#sect-down');
    $thisSect = $('#this-sect');
    page = new Page();

    initSearch();
    initSectionSelect();
    dialog(0, false);
    $('#searchLnk').click(function()
    {
        dialog(0, true);
    });
    $('#sectionLnk').click(function()
    {
        dialog(1, true);
    });

    $(window).keydown(function(e)
    {
        var c = e.keyCode;
        if(c === 39)     		    // right
            loadText(loadMode.down);
        else if(c === 13)		    // enter
        {
            thisNextSection();
        }
        else if(c === 8)	    	// backspace
        {
            if(page.bookId())
                loadText(loadMode.prev);
            e.preventDefault();
        }
        else if(c === 75)           // k
        {
            dialog(0, true);
            var el = $('#searchInput')[0];
            el.focus();
            el.select();
            e.preventDefault();
        }
        else if(c === 83)           // s
        {
            dialog(1, true);
            $('#sect1')[0].focus();
            e.preventDefault();
        }
        else if(c === 27)		    // esc
        {
            dialog(-1, false);
        }
        else if(c === 188 || c === 109)		    // , or -
        {
            if(search)
                loadText(loadMode.prevHit);
        }
        else if(c === 190 || c === 107)		    // . or +
        {
            if(search)
                loadText(loadMode.nextHit);
        }
    });

    $('.prev-sect').click(function()
    {
        if(page.bookId())
            loadText(loadMode.prev);
    });

    $('.next-sect').click(thisNextSection);

    $sectDown.click(function()
    {
        loadText(loadMode.down);
    });

    $('.prev-hit').click(function()
    {
        if(search)
            loadText(loadMode.prevHit);
    });

    $('.next-hit').click(function()
    {
        if(search)
            loadText(loadMode.nextHit);
    });

    $thisSect.click(thisNextSection);

    var cs = client.system;
    if(cs.android || cs.ios || cs.iphone || cs.ipad || cs.winMobile)
    {
        $(window).on('swipeleft', function()
        {
            if(page.isSearchResult())
                loadText(loadMode.nextHit);
            else if(page.bookId())
                loadText(loadMode.next);

        }).on('swiperight', function()
        {
            if(page.isSearchResult())
                loadText(loadMode.prevHit);
            else if(page.bookId())
                loadText(loadMode.prev);
        });
    }

    window.onresize = throttle(true, 100, function()
    {
        var $m = $('#measure');
        var winHgt = $m.height();
        var winWid = $m.width();
        var headHgt = $('#header').height();
        $content[0].style.top = headHgt+'px';
        $content.innerHeight(winHgt-headHgt);
        $content.innerWidth(winWid);
    });
});


function tests()
{
}
