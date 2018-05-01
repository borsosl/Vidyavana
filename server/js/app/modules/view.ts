
import * as util from './util';
import * as paraTypes from "./paragraph-types";
import * as load from './load';


let origFontSize: number, fontSize: string, viewMsgShown: boolean;
let displayTypes = '';
let $paraTypesLink: JQuery;

export function init() {
    const size: string = $(':root').css('font-size').replace('px', '');
    origFontSize = Math.round(parseFloat(size) * 72 / 96);
    fontSize = '' + origFontSize;
    const storedSize = localStorage.getItem('fontsize');
    if(storedSize) {
        fontSize = storedSize;
        setSizes();
    }

    $paraTypesLink = $('#view-para-types-link');
    $paraTypesLink.click((e: JQueryEventObject) => {
        paraTypes.open(paraTypes.Mode.Display);
        e.preventDefault();
    });

    $('#viewPop').keydown(function() {
        if(viewMsgShown) {
            $('#view-msg').hide();
            viewMsgShown = false;
        }
    });

    $('#viewGo').click(function() {
        apply();
    });
}

export function showDialog(forMenu = true) {
    if(util.dialog(util.dialog.id.view, true)) {
        $('#view-msg').hide();
        viewMsgShown = false;
        const $e = $('#viewFontInput');
        $e.val(fontSize);
        const el = $e[0] as HTMLInputElement;
        el.focus();
        el.select();
        if(forMenu) {
            paraTypes.setDisplayTypes(displayTypes);
            paraTypesLinkTitle();
        }
    }
}

function apply() {
    const size: string = $('#viewFontInput').val();
    const val = parseFloat(size);
    if(isNaN(val) || val < 6 || val > 24) {
        $('#view-msg').text('Érvénytelen betűméret, 6 és 24 között lehet').show();
        viewMsgShown = true;
        return;
    }
    fontSize = size;
    setSizes();
    localStorage.setItem('fontsize', fontSize);
    util.hideAllDialogs();
    util.refreshMenu();
    util.focusContent(true);

    if(displayTypes !== paraTypes.displayTypes) {
        displayTypes = paraTypes.displayTypes;
        load.filterStart();
    }
}

function setSizes() {
    $(':root').css('font-size', fontSize + 'pt');
    const size = parseFloat(fontSize);
    const headerPx = size < 8 ? 16 : size < 10 ? 18 : size < 17 ? 20 : size < 21 ? 22 : 24;
    $('#header').css('font-size', headerPx + 'px');
    const fixedPt = size < 10 ? origFontSize - 1 : size < 17 ? origFontSize : origFontSize + 1;
    $('#fixed-size').css('font-size', fixedPt + 'pt');
}

export function paraTypesLinkTitle() {
    $paraTypesLink.text(paraTypes.displayTypes === '' ? 'Minden típus' : paraTypes.displayTypes.length + '-féle típus');
}

export function urlSegment() {
    return displayTypes ? '/'+displayTypes : '';
}
