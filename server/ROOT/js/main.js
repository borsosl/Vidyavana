// internal singletons
var book, measure;
// const collection
var loadMode = {section: 1, next: 2, down: 3, up: 4};
/**
 * @type {JQuery} - points to centered message box
 */
var $msg;
/**
 * Stores info about current section selection.
 * @type {{sel:HTMLElement, level:number, id:number, node:TocTreeItem}}
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
 * @type {number} - measured height of text currently in DOM (scrolled)
 */
var loadedHgt;
/**
 * @type {number} - current scroll position of text in DOM
 */
var scrollTop;
/**
 * @type {boolean} - the previous up/down request hit the edge of the book
 */
var textBoundary;
/**
 * @type {JQuery} - container for text content
 */
var $txt;
/**
 * @type {boolean} - an empty spacer div has been added to book end
 */
var emptyEndBlock;
/**
 * @type {string} - top|bottom : where reduce() should shorten text in DOM
 */
var reduceAt;


/**
 * Sends ajax request to get text chunk. Starts rendering based on the
 * scroll direction that started the request.
 * @param {number} mode - one of loadMode's
 * @param {number?} px - scroll amount to pass on after loading
 */
function loadText(mode, px)
{
    var m = loadMode;

    /**
     * @returns {?string} - ajax request URI or null if no req needed
     */
    function loadModeUrl()
    {
        switch(mode)
        {
            case m.section:
                return '/app/txt/section/'+selSection;
            case m.next:
            case m.down:
                var last = book.last();
                if(last > book.paraNum())
                    return null;
                return '/app/txt/next/'+book.id()+'/'+last;
            case m.up:
                return '/app/txt/prev/'+book.id()+'/'+book.first();
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
                {
                    renderText(json, mode);
                    // scroll
                    if(mode == m.down)
                        down(px);
                    else if(mode == m.up)
                        up(px);
                }
            },

            error: function(/*xhr, status*/)
            {
                ajaxError(/*xhr, status,*/ 'Hiba a szöveg letöltésekor.', function()
                {
                    loadText(mode);
                });
            }
        });
    if(mode === m.up)
    {
        var $first = $txt.children(':first');
        $first.data('pos', $first.position().top);
    }
}


/**
 * On successful load, add text into DOM.
 * @param {DisplayBlock} json - loaded text details
 * @param {number} mode - one of loadMode's
 */
function renderText(json, mode)
{
    var m = loadMode;
    var init = mode==m.section;
    init = init || book.init(json, init);
    book.load(json);
    var h = json.text;
    var down = mode==m.down,
        up = mode==m.up;
    if(h)
    {
        if(init)
        {
            $txt.html(h);
            scrollTop = 0;
            emptyEndBlock = false;
        }
        else if(up)
        {
            $txt.prepend(h);
            reduceAt = 'bottom';
        }
        else
        {
            $txt.append(h);
            reduceAt = 'top';
        }
        measure.eachPara(up);

        // fill screen without scrolling
        if(init && loadedHgt < txtViewHgt)
            loadText(m.next);
    }
    else if(down || up)
    {
        textBoundary = true;
        if(down && !emptyEndBlock)
        {
            $('<div style="height: '+txtViewHgt+'px;" />').appendTo($txt);
            emptyEndBlock = true;
        }
    }
}


/**
 * Scrolls text down if enough is loaded, otherwise starts loading and will
 * come back to scroll later.
 * @param {number} px - pixels to scroll down
 */
function down(px)
{
    //console.log(''+loadedHgt+','+scrollTop+','+txtViewHgt+','+px);
    if(loadedHgt > scrollTop+txtViewHgt+px)
    {
        scrollTextBy(px);
        measure.reduce();
    }
    else if(textBoundary)
    {
        if(loadedHgt > scrollTop+px)
            scrollTextBy(px);
        textBoundary = false;
    }
    else
    {
        loadText(loadMode.down, px);
        //console.log('load');
    }
}


/**
 * Scrolls text up if enough is loaded, otherwise starts loading and will
 * come back to scroll later.
 * @param {number} px - pixels to scroll up
 */
function up(px)
{
    //console.log(''+scrollTop+','+px);
    if(textBoundary)
    {
        scrollTop = 0;
        scrollTextBy(0);
        textBoundary = false;
    }
    else if(scrollTop >= px)
    {
        scrollTextBy(-px);
        measure.reduce();
    }
    else
    {
        loadText(loadMode.up, px);
        //console.log('load up');
    }
}


/**
 * Actual relative scrolling of the DOM node of text.
 * @param {number} ofs - offset by pixels
 */
function scrollTextBy(ofs)
{
    scrollTop += ofs;
    $txt.scrollTop(scrollTop);
}


function Book()
{
    /**
     * @type {number} - book id (segment # (eg. canto/lila) is << 16 bits)
     */
    var id;
    /**
     * @type {number} - 1-based index of first loaded paragraph
     */
    var first;
    /**
     * @type {number} - 1-based index of the next, unloaded paragraph
     */
    var last;
    /**
     * @type {number} - index of first visible paragraph
     */
    var show;
    /**
     * @type {number} - # of paragraphs in the book
     */
    var paraNum;


    /**
     * Resets fields if book has changed.
     * @param {DisplayBlock} json - loaded chunk and book info
     * @param {boolean} force - force reload even if the same book was loaded
     * @returns {boolean} - was reset
     */
    function init(json, force)
    {
        if(json.book == id && !force)
            return false;
        id = json.book;
        first = 50000;
        last = -1;
        if(json.paraNum)
            paraNum = json.paraNum;
        if(measure)
            measure.init();
        return true;
    }


    /**
     * Adjusts first/last depending on how loaded chunk grew.
     * Sets last req. parag.
     * @param {DisplayBlock} json - loaded chunk and book info
     */
    function load(json)
    {
        if(json.first < first)
            first = json.first;
        if(json.last > last)
            last = json.last;
        show = json.show;
    }


    $.extend(this, {
        init: init,
        load: load,
        id: function(){return id;},
        first: function(f){return f===undefined ? first : (first=f);},
        last: function(l){return l===undefined ? last : (last=l);},
        paraNum: function(){return paraNum;},
        show: function(){return show;}
    });
}


function Measure()
{
    /**
     * @type {JQuery} - Invisible DOM to measure char sizes.
     */
    var $shadow = $('#shadowText');
    /**
     * @type {number} - line height
     */
    var lnHgt;
    /**
     * @type {number} - estimated # of chars that fit in a row
     */
    var charPerRow;
    var hgtMap;
    /**
     * @type {JQuery} - collection of <p> elements in rendered text
     */
    var $paras;

    init();

    /**
     * Reset stored measurements.
     */
    function init()
    {
        $shadow.html('M');
        lnHgt = $shadow.height();
        $shadow.html('M<br/>M');
        lnHgt = $shadow.height() - lnHgt;
        var wid = $txt.width();
        charPerRow = Math.ceil(wid / lnHgt / 0.35);
        resize();
    }


    function resize()
    {
        hgtMap = {};
    }


    /**
     * Adjust loaded height and re-scrolls to previously visible paragraph.
     * @param {boolean?} up
     */
    function eachPara(up)
    {
        loadedHgt = $txt[0].scrollHeight;
        if(up)
        {
            $paras = $txt.children();
            for(var i=0; i<$paras.length; ++i)
            {
                var $p = $($paras[i]);
                var prevPos = $p.data('pos');
                if(prevPos === undefined)
                    continue;
                var newPos = $p.position().top;
                scrollTop += newPos-prevPos;
                return;
            }
            alert('No old pos');
        }
    }


    /**
     * Remove para's from DOM that user has scrolled far from.
     */
    function reduce()
    {
        var KEEP_HGT = txtViewHgt*3;
        if(loadedHgt < KEEP_HGT)
            return;
        if(!reduceAt)
            return;
        var atTop = reduceAt === 'top';
        reduceAt = null;
        var $ch = $txt.children(),
            len = $ch.length;

        if(atTop)
        {
            var till = -KEEP_HGT;
            var $p;
            var ofs, topOfs;
            for(var i=0; i<len; ++i)
            {
                $p = $($ch[i]);
                ofs = $p.position().top;
                if(!topOfs)
                    topOfs = ofs;
                if(ofs >= till)
                    break;
            }
            if(i < len && i > 0)
            {
                $ch.slice(0, i).remove();
                scrollTextBy(topOfs - ofs);
                book.first(book.first()+i);
            }
        }
        else
        {
            till = txtViewHgt+KEEP_HGT;
            for(i=len-1; i>=0; --i)
            {
                $p = $($ch[i]);
                ofs = $p.position().top;
                if(ofs <= till)
                    break;
            }
            if(i < len-1 && i >= 0)
            {
                $ch.slice(i+1).remove();
                book.last(book.last()-(len-i-1));
            }
        }
    }


    $.extend(this,
        {
            init: init,
            resize: resize,
            eachPara: eachPara,
            reduce: reduce,
            lnHgt: function(){return lnHgt;}
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


// ********** Section select **********


/**
 * One-time setup of event handlers.
 */
function initSectionSelect()
{
    updateSectionSelects(pg.toc, 1);

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
    book = new Book();
    measure = new Measure();

    initSectionSelect();
    $('#sectionPop').show();
    $('#sectionLnk').click(function()
    {
        $('#sectionPop').toggle();
    });

    $(window).keydown(function(e)
    {
        if(e.keyCode == 40)		// down
            down(measure.lnHgt());
        else if(e.keyCode == 38)	// up
            up(measure.lnHgt());
        else if(e.keyCode == 34)	// pg dn
            down(txtViewHgt);
        else if(e.keyCode == 33)	// pg up
            up(txtViewHgt);
    });

    $(window).on('mousewheel', function(e)
    {
        if(e.deltaY > 0)
            up(measure.lnHgt());
        else if(e.deltaY < 0)
            down(measure.lnHgt());
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
        measure.resize();
        measure.eachPara();
    });
});
