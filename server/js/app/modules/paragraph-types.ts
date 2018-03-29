import * as util from "./util";
import * as task from './task';
import * as search from './search';

export let types = '';
let backupTypes: string;
let $root: JQuery;
let $checkboxes: JQuery;


export function init() {
    $root = $('#typesPop');
    $checkboxes = $('input', $root);

    types = sessionStorage.getItem('paragraph-types');
    if(!types)
        types = '';
    search.paraTypesLinkTitle();

    setCheckboxes();

    $('#types-all').click(all);
    $('#types-none').click(none);
    $('#types-go').click(go);
    $('#types-cancel').click(cancel);
    $root.keydown(handleLocalKeys);
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

export function open() {
    backupTypes = types;
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
    sessionStorage.setItem('paragraph-types', types);
    search.paraTypesLinkTitle();
    task.searchDialog();
}

function cancel() {
    types = backupTypes;
    task.searchDialog();
}

function handleLocalKeys(e: JQueryEventObject) {
    if(e.keyCode === 13) {
        go();
        e.stopPropagation();
    } else if(e.keyCode === 27) {
        cancel();
        e.stopPropagation();
    }
}
