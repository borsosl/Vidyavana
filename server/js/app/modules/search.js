
var load = require('./load');
var util = require('./util');

/** @type {Search} */
var search, pendingSearch;

/** @type {boolean} - if a search-related message is visible */
var searchMsgShown;

function Search() {
    /**
     * @type {string} - entered text
     */
    var query;
    /**
     * @type {string} - selected order
     */
    var _sort;
    /**
     * @type {number} - hitlist page size
     */
    var _page;
    /**
     * @type {SearchResponse} - details of last hit shown
     */
    var last;

    /**
     * @param {string?} q
     * @return {string}
     */
    function queryFn(q) {
        if(q === undefined)
            return query;
        query = q;
    }

    /**
     * @param {string?} sort
     * @return {string}
     */
    function sortFn(sort) {
        if(sort === undefined)
            return _sort;
        _sort = sort;
    }

    /**
     * @param {string?} page
     * @return {number}
     */
    function pageFn(page) {
        if(page === undefined)
            return _page;
        _page = Number(page);
        if(isNaN(_page))
            _page = 1;
    }


    /**
     * @param {SearchResponse?} l
     * @return {SearchResponse}
     */
    function lastFn(l) {
        if(l === undefined)
            return last;
        last = l;
    }

    this.query = queryFn;
    this.sort = sortFn;
    this.page = pageFn;
    this.last = lastFn;
}


function accept() {
    search = pendingSearch;
}


/**
 * One-time setup of event handlers.
 */
function init() {
    var $inp = $('#searchInput');
    /** @type {JQuery|Array.<HTMLInputElement>} */
    var $scoreOrder = $('#score-order');
    var $searchPaging = $('#search-paging');

    var spage = util.cookie('spage');
    if(spage) {
        $('input[value="'+spage+'"]', $searchPaging).prop('checked', true);
    }

    $inp.keydown(function(e)
    {
        if(searchMsgShown)
        {
            $('#search-msg').hide();
            searchMsgShown = false;
        }
        if(e.keyCode == 13)
        {
            search();
        }
        if(!util.menuModifier(e))
            //noinspection JSUnresolvedFunction
            e.stopPropagation();
    });
    $('#searchGo').click(function()
    {
        search();
    });

    function search() {
        newSearch($inp.val(), $scoreOrder[0].checked, $('input:checked', $searchPaging).val());
    }
}


function newSearch(text, scoreOrder, page) {
    if(searchMsgShown)
    {
        $('#search-msg').hide();
        searchMsgShown = false;
    }
    var ps = pendingSearch = new Search();
    ps.query(text);
    ps.sort(scoreOrder ? 'Score' : 'Index');
    ps.page(page);
    load.text(load.mode.search);
    util.cookie('spage', page);
}


function message(msg) {
    $('#search-msg').text(msg).show();
    searchMsgShown = true;

}


function isHitlist() {
    return getInstance().last().endHit > -1;
}

/**
 * @param {boolean?} pending
 * @return {Search}
 */
function getInstance(pending) {
    return pending ? pendingSearch : search;
}


$.extend(exports, {
    get: getInstance,
    pending: getInstance.bind(null, true),
    init: init,
    accept: accept,
    message: message,
    isHitlist: isHitlist
});
