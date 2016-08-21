
var util = require('./util');

function searchDialog() {
    if(util.dialog(0, true)) {
        var el = $('#searchInput')[0];
        el.focus();
        el.select();
    }
}

var origFontSize, fontSize, viewMsgShown;

function initView() {
    var size = $(':root').css('font-size').replace('px', '');
    origFontSize = Math.round(parseFloat(size) * 72 / 96);
    fontSize = '' + origFontSize;
    var storedSize = util.cookie('fontsize');
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
        var $e = $('#viewFontInput');
        $e.val(fontSize);
        var el = $e[0];
        el.focus();
        el.select();
    }
}

function applyView() {
    var size = $('#viewFontInput').val();
    var val = parseFloat(size);
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
    var headerPx = fontSize < 8 ? 16 : fontSize < 10 ? 18 : fontSize < 17 ? 20 : fontSize < 21 ? 22 : 24;
    $('#header').css('font-size', headerPx + 'px');
    var fixedPt = fontSize < 10 ? origFontSize - 1 : fontSize < 17 ? origFontSize : origFontSize + 1;
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
                document.location = '/app';
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


$.extend(exports, {
    searchDialog: searchDialog,
    initView: initView,
    viewDialog: viewDialog,
    applyView: applyView,
    logout: logout
});
