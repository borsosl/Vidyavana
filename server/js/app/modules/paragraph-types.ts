import * as util from "./util";
import * as task from './task';
import * as search from './search';
import * as view from './view';

export const enum Mode {
    Search,
    Display
}

const sessionStorageSearchKey = 'search-paragraph-types';

export let mode: Mode;
export let searchTypes = '', displayTypes = '';
let types: string;
let $root: JQuery;
let $checkboxes: JQuery;


export function init() {
    $root = $('#typesPop');
    $checkboxes = $('input', $root);

    searchTypes = sessionStorage.getItem(sessionStorageSearchKey);
    if(!searchTypes)
        searchTypes = '';
    search.paraTypesLinkTitle();

    $('#types-all').click(all);
    $('#types-none').click(none);
    $('#types-go').click(go);
    $('#types-cancel').click(cancel);
}

export function setDisplayTypes(dt: string) {
    displayTypes = dt;
}

function setCheckboxes() {
    const all = types === '';
    $checkboxes.each(function(this: HTMLInputElement) {
        $(this).prop('checked', all || types.includes(this.name.slice(-1)));
    });
}

function setTypes() {
    types = '';
    let all = true;
    $checkboxes.each(function(this: HTMLInputElement) {
        if(this.checked)
            types += this.name.slice(-1);
        else
            all = false;
    });
    if(all)
        types = '';
}

export function open(openMode: Mode) {
    if(mode !== openMode) {
        mode = openMode;
        types = mode === Mode.Search ? searchTypes : displayTypes;
        setCheckboxes();
    }
    util.dialog(util.dialog.id.types, true);
    $checkboxes[0].focus();
}

function all() {
    types = '';
    setCheckboxes();
}

function none() {
    types = 'X';
    setCheckboxes();
}

function go() {
    setTypes();
    if(mode === Mode.Search) {
        searchTypes = types;
        sessionStorage.setItem(sessionStorageSearchKey, types);
        search.paraTypesLinkTitle();
        task.searchDialog();
    } else {
        displayTypes = types;
        view.paraTypesLinkTitle();
        view.showDialog(false);
    }
}

function cancel() {
    if(mode === Mode.Search)
        task.searchDialog();
    else
        view.showDialog(false);
}
