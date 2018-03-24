
import * as util from './util';

export function searchDialog() {
    if(util.dialog(util.dialog.id.search, true)) {
        const el = $('#searchInput')[0] as HTMLInputElement;
        el.focus();
        el.select();
    }
}

let origFontSize: number, fontSize: string, viewMsgShown: boolean;

export function initView() {
    const size: string = $(':root').css('font-size').replace('px', '');
    origFontSize = Math.round(parseFloat(size) * 72 / 96);
    fontSize = '' + origFontSize;
    const storedSize = localStorage.getItem('fontsize');
    if(storedSize) {
        fontSize = storedSize;
        setSizes();
    }

    $('#viewPop').keydown(function() {
        if(viewMsgShown) {
            $('#view-msg').hide();
            viewMsgShown = false;
        }
    });
}

export function viewDialog() {
    if(util.dialog(util.dialog.id.view, true)) {
        $('#view-msg').hide();
        viewMsgShown = false;
        const $e = $('#viewFontInput');
        $e.val(fontSize);
        const el = $e[0] as HTMLInputElement;
        el.focus();
        el.select();
    }
}

export function applyView() {
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
}

function setSizes() {
    $(':root').css('font-size', fontSize + 'pt');
    const size = parseFloat(fontSize);
    const headerPx = size < 8 ? 16 : size < 10 ? 18 : size < 17 ? 20 : size < 21 ? 22 : 24;
    $('#header').css('font-size', headerPx + 'px');
    const fixedPt = size < 10 ? origFontSize - 1 : size < 17 ? origFontSize : origFontSize + 1;
    $('#fixed-size').css('font-size', fixedPt + 'pt');
}


export function logout() {
    $.ajax({
        url: '/app/auth/logout',
        dataType: 'json',

        success(json) {
            if(util.javaError(json))
                return;
            if(!json.ok)
                failed();
            else
                document.location.href = '/app';
        },

        error(/*xhr, status*/) {
            failed();
        }
    });
    util.loading(true);

    function failed() {
        util.ajaxError('Sikertelen művelet.', logout);
    }
}
