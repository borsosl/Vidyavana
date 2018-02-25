
import load from './load';
import util from './util';

let search: Search, pendingSearch: Search;

/** if a search-related message is visible */
let searchMsgShown: boolean;

class Search {
    /**
     * entered text
     */
    public query: string;
    /**
     * selected order
     */
    public sort: string;
    /**
     * hitlist page size
     */
    public page: number;
    /**
     * details of last hit shown
     */
    public last: SearchResponse;


    setPage(page: string): number {
        if(page === undefined)
            return this.page;
        this.page = Number(page);
        if(isNaN(this.page))
            this.page = 1;
    }
}


function accept() {
    search = pendingSearch;
}


/**
 * One-time setup of event handlers.
 */
function init() {
    const $inp = $('#searchInput');
    const $scoreOrder: JQuery = $('#score-order');
    const $searchPaging: JQuery = $('#search-paging');

    const spage = util.cookie('spage');
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
        else if(e.keyCode == 27)
        {
            util.dialog(-1, false);
            util.focusContent();
        }
        if(!util.menuModifier(e))
            e.stopPropagation();
    });
    $('#searchGo').click(function()
    {
        search();
    });

    function search() {
        newSearch($inp.val(), ($scoreOrder[0] as HTMLInputElement).checked, $('input:checked', $searchPaging).val());
    }
}


function newSearch(text: string, scoreOrder: boolean, page: string) {
    if(searchMsgShown)
    {
        $('#search-msg').hide();
        searchMsgShown = false;
    }
    const ps = pendingSearch = new Search();
    ps.query = text;
    ps.sort = scoreOrder ? 'Score' : 'Index';
    ps.setPage(page);
    load.text(load.mode.search);
    util.cookie('spage', page);
}


function message(msg: string) {
    $('#search-msg').text(msg).show();
    searchMsgShown = true;

}

/** search page shows multiple results */
function isHitlist() {
    return getInstance().last.endHit > -1;
}

function getInstance(pending?: boolean): Search {
    return pending ? pendingSearch : search;
}


export default {
    inst: getInstance,
    pending: getInstance.bind(null, true),
    init: init,
    accept: accept,
    message: message,
    isHitlist: isHitlist
};
