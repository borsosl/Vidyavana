
import * as util from './util';
import * as load from './load';

interface NodeInfo {
    sel:HTMLSelectElement;
    level:number;
    id:number;
    node:TocTreeItem
}

/** Stores info about current section selection. */
let nodeToUpdate: NodeInfo;
/** TOC node id currently selected in UI. */
let selSection: number;
/** Remember GUI-selected toc id, until hit selection is reflected on TOC GUI  */
let guiSelSection: number;
/** last section of the database */
let maxTocId: number;

/**
 * Get unloaded children of selected node. Redraw selects when they are available.
 */
function updateTocNode()
{
    const o = nodeToUpdate;
    if(o.node.partial)
    {
        getTocChildren(o.id, updateTocNode, function(fullNode: TocTreeItem)
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
 * @param parent - root to start search from
 * @param id - TOC id to find
 * @return found node or its parent
 */
function findTocNodeById(parent: TocTreeItem, id: number): TocTreeItem
{
    const ch = parent.children;
    if(ch == null)
        return parent;
    const len = ch.length;
    for(let i=0; i<len; ++i)
    {
        const ti = ch[i];
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
 * @param id - TOC id
 * @param retryFn - on failed ajax, call to retry
 * @param cb - call back on success
 */
function getTocChildren(id: number, retryFn: Function, cb: Function)
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
 */
function replacePipes(node: TocTreeItem)
{
    const ch = node.children;
    const rex = /ǀ/;
    for(let i in ch)
    {
        const it = ch[i];
        it.title = it.title.replace(rex, ' – ');
        if(it.children)
            replacePipes(it);
    }
}


// ********** Section select **********


/**
 * One-time setup of event handlers.
 */
export function initSectionSelect()
{
    replacePipes(pg.toc);
    updateSectionSelects(pg.toc, 1);
    maxTocId = pg.maxTocId;

    // event handlers
    $('.sectionSelect').change(function(this: HTMLSelectElement)
    {
        const level = parseInt(this.id.substr(4));
        const id = parseInt(this.value);
        const node = findTocNodeById(pg.toc, id);
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
 * @param parent - the children of which are a level's items
 * @param level - to update, with child levels
 */
function updateSectionSelects(parent: TocTreeItem, level: number)
{
    while(parent)
    {
        let ch = parent.children;
        selSection = parent.id;
        if(parent.parentStart)
            --selSection;
        guiSelSection = selSection;
        if(!ch)
            break;
        const $e = $('#sect' + (level++));
        const opt = [];
        for(let i in ch)
        {
            if(!ch.hasOwnProperty(i))
                continue;
            const it = ch[i];
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
    selSection = guiSelSection;
    load.text(load.mode.section);
}


export function selectedSection(sel?: number) {
    if(sel !== undefined)
        selSection = sel;
    return selSection;
}
