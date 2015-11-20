// internal singletons
var page;
/**
 * @enum {number} - text request modes
 */
var loadMode = {section: 1, down: 2, next: 3, prev: 4};
/**
 * @type {JQuery} - points to centered message box
 */
var $msg;
/**
 * Stores info about current section selection.
 * @type {{sel:HTMLSelectElement, level:number, id:number, node:TocTreeItem}}
 */
var nodeToUpdate;
/**
 * @type {number} - TOC node id currently selected in UI.
 */
var selSection;
/**
 * @type {number} - px height of text canvas
 */
var txtViewHgt;
/**
 * @type {JQuery} - container for text content
 */
var $txt;
/**
 * @type {number} - last section of the database
 */
var maxTocId;


/**
 * Sends ajax request to get text chunk. Starts rendering based on the
 * scroll direction that started the request.
 * @param {number} mode - one of {@link loadMode}
 */
function loadText(mode)
{
    var m = loadMode;

    /**
     * @returns {?string} - ajax request URI or null if no req needed
     */
    function loadModeUrl()
    {
        var sectionUrl = '/app/txt/section/';
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
        }
    }

    var url = loadModeUrl();
    if(!url)
        return;

    $.ajax(
    {
        url: url,
        dataType: 'json',

        success: function(json)
        {
            if(!javaError(json))
                renderText(json, mode);
        },

        error: function(/*xhr, status*/)
        {
            ajaxError(/*xhr, status,*/ 'Hiba a szöveg letöltésekor.', function()
            {
                loadText(mode);
            });
        }
    });
}


/**
 * On successful load, add text into DOM.
 * @param {DisplayBlock} json - loaded text details
 * @param {number} mode - one of {@link loadMode}
 */
function renderText(json, mode)
{
    var initPage = mode != loadMode.down;
    if(initPage)
        page.init(json, false);
    page.load(json);
    var h = json.text;
    if(h)
    {
        if(initPage)
        {
            $txt.scrollTop(0);
            $txt.html(h);
        }
        else
            $txt.append(h);
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
     * @type {number} - 1-based index of the next, unloaded paragraph
     */
    var last;


    /**
     * Resets fields if book has changed.
     * @param {DisplayBlock} json - loaded chunk and book info
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
        bookId: function(){return bookId;},
        tocId: function(){return tocId;}
    });
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
        $e.css('visibility', 'visible');
        parent = ch[0];
    }
    while(level <= 9)
    {
        $('#sect'+(level++))[0].style.visibility = 'hidden';
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
    $msg.html(msg);
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


$(function()
{
    $txt = $('#text');
    page = new Page();

    initSectionSelect();
    $('#sectionPop').show();
    $('#sectionLnk').click(function()
    {
        $('#sectionPop').toggle();
    });

    $(window).keydown(function(e)
    {
        if(e.keyCode === 39)     		    // right
            loadText(loadMode.down);
        else if(e.keyCode === 13)		    // enter
            loadText(loadMode.next);
        else if(e.keyCode === 8)	    	// backspace
        {
            loadText(loadMode.prev);
            e.preventDefault();
        }
        else if(e.keyCode === 83)           // s
        {
            $('#sectionPop').toggle();
            $('#sect1')[0].focus();
            e.preventDefault();
        }
        else if(e.keyCode === 27)		    // esc
            $('#sectionPop').hide();
    });

    window.onresize = throttle(true, 100, function()
    {
        var $m = $('#measure');
        var winHgt = $m.height();
        var winWid = $m.width();
        var headHgt = $('#header').height();
        $txt[0].style.top = headHgt+'px';
        $txt.innerHeight(winHgt-headHgt);
        $txt.innerWidth(winWid);
        txtViewHgt = $txt.height();
    });
});
