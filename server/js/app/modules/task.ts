
import util from './util';

function searchDialog() {
    if(util.dialog(0, true)) {
        const el = $('#searchInput')[0] as HTMLInputElement;
        el.focus();
        el.select();
    }
}

let origFontSize: number, fontSize: string, viewMsgShown: boolean;

function initView() {
    const size: string = $(':root').css('font-size').replace('px', '');
    origFontSize = Math.round(parseFloat(size) * 72 / 96);
    fontSize = '' + origFontSize;
    const storedSize = util.cookie('fontsize');
    if(storedSize) {
        fontSize = storedSize;
        setSizes();
    }

    $('#viewPop').keydown(function()
    {
        if(viewMsgShown)
        {
            $('#view-msg').hide();
            viewMsgShown = false;
        }
    });
}

function viewDialog() {
    if(util.dialog(2, true)) {
        $('#view-msg').hide();
        viewMsgShown = false;
        const $e = $('#viewFontInput');
        $e.val(fontSize);
        const el = $e[0] as HTMLInputElement;
        el.focus();
        el.select();
    }
}

function applyView() {
    const size: string = $('#viewFontInput').val();
    const val = parseFloat(size);
    if(isNaN(val) || val < 6 || val > 24) {
        $('#view-msg').text('Érvénytelen betűméret, 6 és 24 között lehet').show();
        viewMsgShown = true;
        return;
    }
    fontSize = size;
    setSizes();
    util.cookie('fontsize', fontSize);
    util.dialog(-1, false);
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


function logout() {
    $.ajax({
        url: '/app/auth/logout',
        dataType: 'json',

        success: function(json) {
            if(util.javaError(json))
                return;
            if(!json.ok)
                failed();
            else
                document.location.href = '/app';
        },

        error: function(/*xhr, status*/) {
            failed();
        }
    });
    util.loading(true);

    function failed() {
        util.ajaxError('Sikertelen művelet.', logout);
    }
}


export default {
    searchDialog: searchDialog,
    initView: initView,
    viewDialog: viewDialog,
    applyView: applyView,
    logout: logout
};
