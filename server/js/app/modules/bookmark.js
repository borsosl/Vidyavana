
var dom = require('./dom');
var util = require('./util');
var page = require('./page');
var html = require('./html-content');
var load = require('./load');

/** @enum {string} - bookmark page display states */
var pageState = {list: 'list', edit: 'edit', delete: 'delete'};
/** @type {string} - display state */
var currentState;
/** @type {number} - for which bookmark was the subpage opened */
var editedBookmarkId;
/** @type {string} - tracks change in filter input */
var prevFilter = '';
/** @type {number} */
var filterTimer = -1;

/** @type {number} */
var allCount;
/** @type {Array.<number>} */
var recent10;
/** @type {Object.<number, Bookmark>} */
var linkEntityMap, filteredEntityMap;

/**
 * @typedef {Object} Bookmark
 * @property {number} id
 * @property {number} userId
 * @property {string} name
 * @property {boolean} follow
 * @property {number} bookSegmentId
 * @property {number} ordinal
 * @property {string} shortRef
 * @property {number} lastUsed
 */

/**
 * @typedef {ContentPageResult} BookmarksResult
 * @property {number} allCount
 * @property {number} filteredCount
 * @property {Array.<number>} recent10
 * @property {Array.<number>} filtered100
 * @property {Object.<number, Bookmark>} recentEntityMap
 * @property {Object.<number, Bookmark>} filteredEntityMap
 * @property {string} filter
 */


function loadPage() {
    html.load('/app/bookmark/page', null, initPage);
}

/**
 * @param {BookmarksResult} data
 * @param {string} html
 */
function initPage(data, html) {
    allCount = data.allCount;
    recent10 = data.recent10;
    linkEntityMap = data.recentEntityMap;

    var arr = [];
    for(var i in recent10) {
        var id = recent10[i];
        var r = linkEntityMap[id];
        arr.push('<a href="#" data-bm-id="', id, '">', r.name, '</a><br/>');
    }
    if(arr.length) {
        i = html.indexOf('bm-links') + 10;
        html = html.substr(0, i) + arr.join('') + html.substring(i);
    }

    if(!data.filteredCount)
        data.filteredCount = 0;
    var count = '' + data.allCount;
    if(data.filter) {
        if(data.filteredCount < data.allCount)
            count = data.filteredCount + '/' + count;
        if(data.filteredCount > 100)
            count = '100/' + count;
        filteredEntityMap = data.filteredEntityMap;
        var optionsSource = data.filtered100;
    } else {
        if(data.allCount > 10)
            count = '10/' + count;
        filteredEntityMap = linkEntityMap;
        optionsSource = recent10;
    }
    i = html.indexOf('bm-count') + 10;
    html = html.substr(0, i) + count + html.substring(i);

    var mru = !recent10.length ? 0 : recent10[0];
    var chooseOptions = filteredOptionsHtml(optionsSource, filteredEntityMap, mru, data.filteredCount);
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
        e.onclick = linkClick;      // supports enter
    });
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
    var id = $(this).data('bm-id');
    if(!id)
        return;
    id = parseInt(id);
    if(!isNaN(id))
        load.bookmark(id);
}

function filterChange(e) {
    setTimeout(function() {
        var val = $(e.target).val();
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
    /** @type {string} */
    var filter = $('#bm-filter').val();
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
    var req = {
        filter: filter
    };
    ajax('/app/bookmark/filter', req, filterRequest, filterResponse);
}
/**
 * @param {BookmarksResult} res
 */
function filterResponse(res) {
    filteredEntityMap = res.filteredEntityMap;
    var count = '' + allCount;
    var c = res.filteredCount;
    if(c !== allCount)
        count = c + '/' + count;
    if(c > 100)
        count = '100/' + count;
    $('#bm-count').text(count);

    var mru = !recent10.length ? 0 : recent10[0];
    var chooseOptions = filteredOptionsHtml(res.filtered100, filteredEntityMap, mru, c);
    replaceChooseOptions(chooseOptions);
    toggleButtons();
}

function newClick() {
    if(!page.current().bookId()) {
        util.message('Nincs választott szakasz', true);
        return;
    }
    editedBookmarkId = 0;
    var ref = page.current().shortRef;
    var ix = ref.indexOf('.');
    var bookRef = ix > 0 ? ref.substr(0, ix) : ref;
    var $name = $('#bm-name');
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
    var id = parseInt($('#bm-choose').val());
    if(!id || isNaN(id))
        return;
    editedBookmarkId = id;
    var bookmark = filteredEntityMap[id];
    var $name = $('#bm-name');
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
    var id = parseInt($('#bm-choose').val());
    if(!id || isNaN(id))
        return;
    editedBookmarkId = id;
    $('#bm-delete-name').text(filteredEntityMap[id].name);
    toggleState(pageState.delete);
}

function storeClick() {
    var id = parseInt($('#bm-choose').val());
    if(!id || isNaN(id))
        return;
    var bookmark = filteredEntityMap[id];
    setCurrentPosition(bookmark);
    saveRequest(bookmark);
}

function goClick() {
    var id = parseInt($('#bm-choose').val());
    if(!id || isNaN(id))
        return;
    load.bookmark(id);
}

function saveClick() {
    if(editedBookmarkId === 0) {
        var bookmark = {};
        setCurrentPosition(bookmark);
    } else {
        bookmark = filteredEntityMap[editedBookmarkId];
    }
    bookmark.name = $('#bm-name').val();
    bookmark.follow = $('#bm-follow').prop('checked');
    saveRequest(bookmark);
}

/**
 * @param {Bookmark} bookmark
 */
function setCurrentPosition(bookmark) {
    bookmark.bookSegmentId = page.current().bookId();
    bookmark.ordinal = util.bookOrdinalOnTop();
    bookmark.shortRef = page.current().shortRef;
}

/**
 * @param {Bookmark} bookmark
 */
function saveRequest(bookmark) {
    var $f = $('#bm-filter');
    var filter = $f ? $f.val() : null;
    var req = {
        bookmark: JSON.stringify(bookmark),
        filter: filter
    };

    ajax('/app/bookmark/save', req, saveClick, function(json) {
        initPage(json.data, json.html);
    });
}

function doDeleteClick() {
    var $f = $('#bm-filter');
    var filter = $f ? $f.val() : null;
    var req = {
        id: editedBookmarkId,
        filter: filter
    };
    ajax('/app/bookmark/delete', req, doDeleteClick, function(json) {
        initPage(json.data, json.html);
    });
}

function cancelClick() {
    toggleState(pageState.list);
}

function closeClick() {
    dom.$formContent.hide();
}

/**
 * @param {Array.<number>} indexList
 * @param {Object.<number, Bookmark>} entityMap
 * @param {number} mostRecentId
 * @param {number} filteredCount
 * @return {string}
 */
function filteredOptionsHtml(indexList, entityMap, mostRecentId, filteredCount) {
    var arr = [];
    for(var i in indexList) {
        var id = indexList[i];
        var bm = entityMap[id];
        var selected = id === mostRecentId ? '" selected>' : '">';
        arr.push('<option value="', id, selected , bm.name, '</option>');
    }
    if(!arr.length)
        arr.push('<option value="0">--- Nincs találat ---</option>');
    else if(filteredCount > 100)
        arr.push('<option value="0">--- ', filteredCount-100, ' régebbi kihagyva ---</option>');
    return arr.join('');
}

/**
 * @param {string} optionsHtml
 */
function replaceChooseOptions(optionsHtml) {
    var $wrap = $('#bm-choose-wrap');
    var html = $wrap.html();
    html = html.replace(/<option.*<\/option>/, '');
    var i = html.indexOf('bm-choose"');
    i = html.indexOf('<', i);
    html = html.substr(0, i) + optionsHtml + html.substring(i);
    $wrap.html(html);
    $('#bm-choose').change(toggleButtons);
}

/**
 * @param {string} state
 */
function toggleState(state) {
    currentState = state;
    for(var i in pageState)
        $('#bm-state-'+pageState[i]).toggle(currentState === pageState[i]);
}

function toggleButtons() {
    var show = $('#bm-choose').val() !== '0';
    $('#bm-buttons').toggle(show);
}

function ajax(url, data, retryFn, cb) {
    $.ajax({
        url: url,
        dataType: 'json',
        data: data,

        success: function(json)
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

$.extend(exports, {
    load: loadPage,
    initPage: initPage
});
