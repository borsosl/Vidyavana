
import * as util from './util';
import * as load from './load';
import * as touch from './touch';

interface NodeInfo {
    sel: HTMLSelectElement;
    level: number;
    id: number;
    node: TocTreeItem;
}

/** Stores info about current section selection. */
let nodeToUpdate: NodeInfo;
/** TOC node id currently selected in UI. */
let selSection: number;
/** Remember GUI-selected toc id, until hit selection is reflected on TOC GUI  */
let guiSelSection: number;
/** last section of the database */
let maxTocId: number;
/** how many select boxes are visible */
let levelsShown: number;
/** abbrev cb set? */
let shortTitles: boolean;

const whiteRex = /(.{15,}?)\s/;
const ordinals = [
    'TIZEN', '1', 'TIZEDIK', '10.', 'HUSZON', '2', 'HUSZADIK', '20.', 'HARMINC', '3', 'NEGYVEN', '4',
    'ÖTVEN', '5', 'HATVAN', '6', 'HETVEN', '7', 'NYOLCVAN', '8', 'KILENCVEN', '9',
    'NEGYEDIK', '4.', 'ELSŐ', '1.', 'EGYEDIK', '1.', 'MÁSODIK', '2.', 'KETTEDIK', '2.', 'HARMADIK', '3.',
    'ÖTÖDIK', '5.', 'HATODIK', '6.', 'HETEDIK', '7.', 'NYOLCADIK', '8.', 'KILENCEDIK', '9.',
    'ADIK', '0.', 'EDIK', '0.'];
let ordinalsRex: RegExp[];

/**
 * Get unloaded children of selected node. Redraw selects when they are available.
 */
function updateTocNode() {
    const o = nodeToUpdate;
    if(o.node.partial) {
        getTocChildren(o.id, updateTocNode, function(fullNode: TocTreeItem) {
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
function findTocNodeById(parent: TocTreeItem, id: number): TocTreeItem {
    const ch = parent.children;
    if(ch == null)
        return parent;
    const len = ch.length;
    for(let i=0; i<len; ++i) {
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
function getTocChildren(id: number, retryFn: () => void, cb: (json: TocTreeItem) => void) {
    $.ajax({
        url: '/app/toc/get/'+id,
        dataType: 'json',

        success(json) {
            if(!util.javaError(json))
                cb.call(this, json);
        },

        error(/*xhr, status*/) {
            util.ajaxError(/*xhr, status,*/ 'Hiba a tartalomjegyzék ág letöltésekor.', retryFn);
        }
    });
    util.loading(true);
}


/**
 * Replaces pipe characters in TOC node text.
 */
function replacePipes(node: TocTreeItem) {
    const ch = node.children;
    const rex = /ǀ/;
    for(const i in ch) {
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
export function initSectionSelect() {
    shortTitles = touch.isMobile();
    replacePipes(pg.toc);
    updateSectionSelects(pg.toc, 1);
    maxTocId = pg.maxTocId;

    // event handlers
    $('.sectionSelect').change(function(this: HTMLSelectElement) {
        const level = parseInt(this.id.substr(4));
        const id = parseInt(this.value);
        const node = findTocNodeById(pg.toc, id);
        nodeToUpdate = {
            sel: this,
            level,
            id,
            node
        };
        updateTocNode();
    });

    const $abbrev = $('#section-abbrev');
    if(shortTitles)
        $abbrev.prop('checked', shortTitles);
    $abbrev.click(function(this: HTMLInputElement) {
        shortTitles = this.checked;
        redrawSelectBoxes();
    });

    $('#sectionGo').click(gotoSection);
}


/**
 * Sets section select boxes from a level downwards.
 * @param parent - the children of which are a level's items
 * @param level - to update, with child levels
 */
function updateSectionSelects(parent: TocTreeItem, level: number) {
    while(parent) {
        const ch = parent.children;
        selSection = parent.id;
        if(parent.parentStart)
            --selSection;
        guiSelSection = selSection;
        if(!ch)
            break;
        const $e = fillSelectBox(level, ch);
        $e.show();
        levelsShown = level++;
        parent = ch[0];
    }
    while(level <= 9) {
        $('#sect'+(level++)).hide();
    }
}

function fillSelectBox(level: number, ch: TocTreeItem[], $e?: JQuery): JQuery {
    $e = $e || $('#sect' + level);
    const opt = [];
    let maxLen = 0;
    const shorten = shortTitles && !ch[0].shortTitle;
    if(shorten && level > 1)
        for(const i in ch) {
            const len = ch[i].title.length;
            if(len > maxLen)
                maxLen = len;
        }
    for(const i in ch) {
        const it = ch[i];
        if(shorten) {
            if(level === 1 || maxLen < 22)
                it.shortTitle = it.title;
            else
                shortenTitle(it);
        }
        opt.push('<option value="', it.id, '">', shortTitles ? it.shortTitle : it.title, '</option>');
    }
    $e.html(opt.join(''));
    return $e;
}

function shortenTitle(item: TocTreeItem): void {
    if(!ordinalsRex) {
        ordinalsRex = [];
        for(let i = 0; i < ordinals.length; i += 2)
            ordinalsRex[i] = new RegExp(ordinals[i], 'i');
    }

    const sep = ' – ';
    const parts = item.title.split(sep);
    let s = parts[0];
    let ix: number;
    for(let i = 0; i < ordinals.length; i += 2) {
        if(s.length < ordinals[i].length)
            continue;
        ix = s.search(ordinalsRex[i]);
        if(ix > -1)
            s = s.substr(0, ix) + ordinals[i+1] + s.substr(ix+ordinals[i].length);
    }
    ix = s.indexOf('.');
    if(ix > -1 && s.length - ix < 10)       // if there is a lot more after dot, don't cut
        s = s.substr(0, ix);
    if(parts.length === 1) {
        item.shortTitle = s[0] !== parts[0][0] ? s : parts[0];      // only if ordinal was the first word
    } else {
        parts[0] = s;
        s = parts.slice(1).join(sep);
        const res = whiteRex.exec(s);
        if(res)
            s = res[1] + '…';
        item.shortTitle = parts[0] + sep + s;
    }
}

function redrawSelectBoxes() {
    let parent: TocTreeItem = pg.toc;
    for(let level = 1; level <= levelsShown; level++) {
        const $e = $('#sect' + level);
        const el = $e[0] as HTMLSelectElement;
        const ix = el.selectedIndex;
        if(level > 1) {
            fillSelectBox(level, parent.children, $e);
            el.selectedIndex = ix;
        }
        parent = parent.children[ix];
    }
}

/**
 * Start loading of selected section.
 */
function gotoSection() {
    $('#sectionPop').hide();
    selSection = guiSelSection;
    load.text(load.mode.section);
}


export function selectedSection(sel?: number) {
    if(sel !== undefined)
        selSection = sel;
    return selSection;
}
