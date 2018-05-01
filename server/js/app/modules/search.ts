
import dom from './dom';
import * as load from './load';
import * as toc from './toc';
import * as paraTypes from "./paragraph-types";
import * as mru from './mru';

let search: Search, pendingSearch: Search;

/** last selection of sections */
let searchSections: SearchSection;
let $searchSectionLink: JQuery;
let $paraTypesLink: JQuery;

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
     * selected sections
     */
    public nodeFilter: string;
    /**
     * selected sections
     */
    public paraTypes: string;
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


export function accept() {
    search = pendingSearch;
}


/**
 * One-time setup of event handlers.
 */
export function init() {
    const $scoreOrder = $('#score-order');
    const $searchPaging = $('#search-paging');
    $searchSectionLink = $('#search-sect-link');
    $paraTypesLink = $('#para-types-link');

    const spage = localStorage.getItem('spage');
    if(spage)
        $('input[value="'+spage+'"]', $searchPaging).prop('checked', true);

    const storedSections = sessionStorage.getItem('search-sections');
    if(storedSections)
        searchSections = JSON.parse(storedSections);
    else
        resetSearchSections('none');
    searchSectionLinkTitle();
    paraTypesLinkTitle();
    mru.init();

    dom.$searchInput.keydown(inputKeydown).click(() => mru.show(false));
    $searchSectionLink.click((e: JQueryEventObject) => {
        toc.openForSearchSection();
        e.preventDefault();
    });
    $paraTypesLink.click((e: JQueryEventObject) => {
        paraTypes.open(paraTypes.Mode.Search);
        e.preventDefault();
    });
    $('#searchGo').click(() => {
        newSearch(dom.$searchInput.val(), ($scoreOrder[0] as HTMLInputElement).checked,
            $('input:checked', $searchPaging).val(), searchSections.nodeFilter, paraTypes.searchTypes);
    });
}

function inputKeydown(this: HTMLInputElement, e: JQueryEventObject) {
    if(searchMsgShown) {
        $('#search-msg').hide();
        searchMsgShown = false;
    }
    mru.inputKeydown.call(this, e);
}

function newSearch(text: string, scoreOrder: boolean, page: string, nodeFilter: string, paraTypes: string) {
    text = text.trim();
    if(!text)
        return;
    if(searchMsgShown) {
        $('#search-msg').hide();
        searchMsgShown = false;
    }
    pendingSearch = new Search();
    pendingSearch.query = text;
    pendingSearch.sort = scoreOrder ? 'Score' : 'Index';
    pendingSearch.setPage(page);
    pendingSearch.nodeFilter = nodeFilter;
    pendingSearch.paraTypes = paraTypes;
    load.text(load.mode.search);
    localStorage.setItem('spage', page);
    mru.addNewSearchWords(text);
}

export function resetSearchSections(base: string) {
    searchSections = {
        nodeFilter: '',
        displayText: '',
        base,
        nodes: [],
        changed: false
    };
}

export function updateSearchSections() {
    searchSections.changed = false;
    sessionStorage.setItem('search-sections', JSON.stringify(searchSections));
    searchSectionLinkTitle();
}

export function restoreSearchSections(ss: SearchSection) {
    searchSections = ss;
}

function searchSectionLinkTitle() {
    $searchSectionLink.text(searchSections.nodeFilter ? searchSections.displayText : 'Minden könyv');
}

export function paraTypesLinkTitle() {
    $paraTypesLink.text(paraTypes.searchTypes === '' ? 'Minden típus' : paraTypes.searchTypes.length + '-féle típus');
}


export function message(msg: string) {
    $('#search-msg').text(msg).show();
    searchMsgShown = true;
}

/** search page shows multiple results */
export function isHitlist() {
    return getInstance().last.endHit > -1;
}

export function getInstance(): Search {
    return search;
}

export function getPendingInstance(): Search {
    return pendingSearch;
}

export {
    searchSections as sections
};
