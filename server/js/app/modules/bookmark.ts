
import dom from './dom';
import * as util from './util';
import * as page from './page';
import * as html from './html-content';
import * as load from './load';

/** bookmark page display states */
const pageState: {[key: string]: string} = {list: 'list', edit: 'edit', delete: 'delete'};
/** displayed state */
let currentState: string;
/** for which bookmark was the subpage opened */
let editedBookmarkId: number;
/** tracks change in filter input */
let prevFilter = '';
let filterTimer = -1;

let allCount: number;
let recent10: number[];

interface EntityMap {
    [prop: number]: Bookmark;
}

let linkEntityMap: EntityMap;
let filteredEntityMap: EntityMap;

interface Bookmark {
    id: number;
    userId: number;
    name: string;
    follow: boolean;
    bookSegmentId: number;
    ordinal: number;
    shortRef: string;
    lastUsed: number;
}

interface BookmarksResult extends ContentPageData {
    allCount?: number;
    filteredCount: number;
    recent10?: number[];
    filtered100: number[];
    recentEntityMap?: EntityMap;
    filteredEntityMap: EntityMap;
    filter?: string;
}


export function loadPage() {
    html.load('/app/bookmark/page', null, initPage);
}

export function initPage(data: BookmarksResult, html: string) {
    allCount = data.allCount;
    recent10 = data.recent10;
    linkEntityMap = data.recentEntityMap;

    const arr = [];
    for(let i in recent10) {
        const id = recent10[i];
        const r = linkEntityMap[id];
        arr.push('<a href="#" data-bm-id="', id, '">', r.name, '</a><br/>');
    }
    if(arr.length) {
        const i = html.indexOf('bm-links') + 10;
        html = html.substr(0, i) + arr.join('') + html.substring(i);
    }

    if(!data.filteredCount)
        data.filteredCount = 0;
    let count = '' + data.allCount;
    let optionsSource;
    if(data.filter) {
        if(data.filteredCount < data.allCount)
            count = data.filteredCount + '/' + count;
        if(data.filteredCount > 100)
            count = '100/' + count;
        filteredEntityMap = data.filteredEntityMap;
        optionsSource = data.filtered100;
    } else {
        if(data.allCount > 10)
            count = '10/' + count;
        filteredEntityMap = linkEntityMap;
        optionsSource = recent10;
    }
    let i = html.indexOf('bm-count') + 10;
    html = html.substr(0, i) + count + html.substring(i);

    const mru = !recent10.length ? 0 : recent10[0];
    const chooseOptions = filteredOptionsHtml(optionsSource, filteredEntityMap, mru, data.filteredCount);
    i = html.indexOf('bm-choose"');
    i = html.indexOf('<', i);
    html = html.substr(0, i) + chooseOptions + html.substring(i);

    dom.$formContent.html(html).show().scrollTop(0);
    if(data.filter)
        $('#bm-filter').val(data.filter);
    $('.bm-links>a:first', '#bm-state-list').focus();
    util.resizeContent();
    toggleState(pageState.list);
    toggleButtons();

    $('a[data-bm-id]', dom.$formContent).each(function(ix, e) {
        (e as HTMLLinkElement).onclick = linkClick;      // supports enter
    });
    // noinspection JSJQueryEfficiency
    $('#bm-filter').on('keydown input', filterChange);
    $('#bm-choose').on('change', toggleButtons);
    $('#bm-new').click(newClick);
    $('#bm-edit').click(editClick);
    $('#bm-delete').click(deleteClick);
    $('#bm-store').click(storeClick);
    $('#bm-go').click(goClick);
    $('#bm-name').keydown(util.textKeyDefaults);
    $('#bm-save').click(saveClick);
    $('.bm-cancel').click(cancelClick);
    $('#bm-del-ok').click(doDeleteClick);
    $('#bm-close').click(closeClick);
}

function linkClick() {
    let id = $(this).data('bm-id');
    if(!id)
        return;
    id = parseInt(id);
    if(!isNaN(id))
        load.bookmark(id);
}

function filterChange(e: JQueryKeyEventObject) {
    setTimeout(function() {
        const val = $(e.target).val();
        if(val === prevFilter && (filterTimer === -1 || e.which !== 13))
            return;
        prevFilter = val;
        if(filterTimer !== -1) {
            clearTimeout(filterTimer);
            filterTimer = -1;
        }
        if(e.which === 13)
            filterRequest();
        else
            filterTimer = setTimeout(filterRequest, 800);
    }, 0);
    util.textKeyDefaults(e);
}

function filterRequest() {
    filterTimer = -1;
    let filter = $('#bm-filter').val();
    filter = filter.replace(/\s+/, '');
    if(!filter) {
        filterResponse({
            filteredCount: recent10.length,
            filtered100: recent10,
            filteredEntityMap: linkEntityMap
        });
        return;
    }
    replaceChooseOptions('<option value="0">--- töltés ---</option>');
    const req = {
        filter: filter
    };
    ajax('/app/bookmark/filter', req, filterRequest, filterResponse);
}

function filterResponse(res: BookmarksResult) {
    filteredEntityMap = res.filteredEntityMap;
    let count = '' + allCount;
    const c = res.filteredCount;
    if(c !== allCount)
        count = c + '/' + count;
    if(c > 100)
        count = '100/' + count;
    $('#bm-count').text(count);

    const mru = !recent10.length ? 0 : recent10[0];
    const chooseOptions = filteredOptionsHtml(res.filtered100, filteredEntityMap, mru, c);
    replaceChooseOptions(chooseOptions);
    toggleButtons();
}

function newClick() {
    if(!page.current().bookId) {
        util.message('Nincs választott szakasz', true);
        return;
    }
    editedBookmarkId = 0;
    const ref = page.current().shortRef;
    const ix = ref.indexOf('.');
    const bookRef = ix > 0 ? ref.substr(0, ix) : ref;
    const $name = $('#bm-name');
    $name.val(bookRef);
    $('#bm-ref-mode').text('Alternatív név: ');
    $('#bm-ref-link').text(ref).off('click').click(function() {
        $('#bm-name').val(ref);
    });
    $('#bm-follow').prop('checked', true);
    $name.focus().select();
    toggleState(pageState.edit);
}

function editClick() {
    let id = parseInt($('#bm-choose').val());
    if(!id || isNaN(id))
        return;
    editedBookmarkId = id;
    const bookmark = filteredEntityMap[id];
    const $name = $('#bm-name');
    $name.val(bookmark.name);
    $('#bm-follow').prop('checked', bookmark.follow);
    $('#bm-ref-mode').text('Pozíció: ');
    $('#bm-ref-link').text(bookmark.shortRef).off('click').click(function() {
        load.bookmark(id);
    });
    $name.focus().select();
    toggleState(pageState.edit);
}

function deleteClick() {
    let id = parseInt($('#bm-choose').val());
    if(!id || isNaN(id))
        return;
    editedBookmarkId = id;
    $('#bm-delete-name').text(filteredEntityMap[id].name);
    toggleState(pageState.delete);
}

function storeClick() {
    let id = parseInt($('#bm-choose').val());
    if(!id || isNaN(id))
        return;
    const bookmark = filteredEntityMap[id];
    setCurrentPosition(bookmark);
    saveRequest(bookmark);
}

function goClick() {
    let id = parseInt($('#bm-choose').val());
    if(!id || isNaN(id))
        return;
    load.bookmark(id);
}

function saveClick() {
    let bookmark: Bookmark;
    if(editedBookmarkId === 0) {
        bookmark = {} as Bookmark;
        setCurrentPosition(bookmark);
    } else {
        bookmark = filteredEntityMap[editedBookmarkId];
    }
    bookmark.name = $('#bm-name').val();
    bookmark.follow = $('#bm-follow').prop('checked');
    saveRequest(bookmark);
}

function setCurrentPosition(bookmark: Bookmark) {
    bookmark.bookSegmentId = page.current().bookId;
    bookmark.ordinal = util.bookOrdinalOnTop();
    bookmark.shortRef = page.current().shortRef;
}

function saveRequest(bookmark: Bookmark) {
    const $f = $('#bm-filter');
    const filter = $f ? $f.val() : null;
    const req = {
        bookmark: JSON.stringify(bookmark),
        filter: filter
    };

    ajax('/app/bookmark/save', req, saveClick, function(json: ContentPageResult<any>) {
        initPage(json.data, json.html);
    });
}

function doDeleteClick() {
    const $f = $('#bm-filter');
    const filter = $f ? $f.val() : null;
    const req = {
        id: editedBookmarkId,
        filter: filter
    };
    ajax('/app/bookmark/delete', req, doDeleteClick, function(json: ContentPageResult<BookmarksResult>) {
        initPage(json.data, json.html);
    });
}

function cancelClick() {
    toggleState(pageState.list);
}

function closeClick() {
    dom.$formContent.hide();
}

function filteredOptionsHtml(indexList: number[], entityMap: EntityMap, mostRecentId: number, filteredCount: number): string {
    const arr = [];
    for(let i in indexList) {
        const id = indexList[i];
        const bm = entityMap[id];
        const selected = id === mostRecentId ? '" selected>' : '">';
        arr.push('<option value="', id, selected , bm.name, '</option>');
    }
    if(!arr.length)
        arr.push('<option value="0">--- Nincs találat ---</option>');
    else if(filteredCount > 100)
        arr.push('<option value="0">--- ', filteredCount-100, ' régebbi kihagyva ---</option>');
    return arr.join('');
}

function replaceChooseOptions(optionsHtml: string) {
    const $wrap = $('#bm-choose-wrap');
    let html = $wrap.html();
    html = html.replace(/<option.*<\/option>/, '');
    let i = html.indexOf('bm-choose"');
    i = html.indexOf('<', i);
    html = html.substr(0, i) + optionsHtml + html.substring(i);
    $wrap.html(html);
    $('#bm-choose').change(toggleButtons);
}

function toggleState(state: string) {
    currentState = state;
    for(let i in pageState)
        $('#bm-state-'+ pageState[i]).toggle(currentState === pageState[i]);
}

function toggleButtons() {
    const show = $('#bm-choose').val() !== '0';
    $('#bm-buttons').toggle(show);
}

function ajax<T>(url: string, data: any, retryFn: Function, cb: AjaxResultCallback<T>) {
    $.ajax({
        url: url,
        dataType: 'json',
        data: data,

        success: function(json: T)
        {
            if(!util.javaError(json) && cb)
                cb.call(null, json);
        },

        error: function(/*xhr, status*/)
        {
            util.ajaxError(/*xhr, status,*/ 'Hálózati hiba.', retryFn);
        }
    });
}
