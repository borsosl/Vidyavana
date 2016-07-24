
var util = require('./util');
var load = require('./load');

/**
 * Stores info about current section selection.
 * @type {?{sel:HTMLSelectElement, level:number, id:number, node:TocTreeItem}}
 */
var nodeToUpdate;
/** @type {number} - TOC node id currently selected in UI. */
var selSection;
/** @type {number} - Remember GUI-selected toc id, until hit selection is reflected on TOC GUI  */
var guiSelSection;
/** @type {number} - last section of the database */
var maxTocId;

function initModules() {
    load = require('./load');
}

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
    $.ajax({
        url: '/app/toc/get/'+id,
        dataType: 'json',

        success: function(json)
        {
            if(!util.javaError(json))
                cb.call(this, json);
        },

        error: function(/*xhr, status*/)
        {
            util.ajaxError(/*xhr, status,*/ 'Hiba a tartalomjegyzék ág letöltésekor.', retryFn);
        }
    });
    util.loading(true);
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
        guiSelSection = selSection;
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
    if(!load.text)
        initModules();
    selSection = guiSelSection;
    load.text(load.mode.section);
}


/** @param {number?} sel */
function selectedSection(sel) {
    if(sel !== undefined)
        selSection = sel;
    return selSection;
}


$.extend(exports, {
    initSectionSelect: initSectionSelect,
    selectedSection: selectedSection
});
