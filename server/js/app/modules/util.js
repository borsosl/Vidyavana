
var dom = require('./dom');

/** @type {JQuery} - points to centered message box, lazy init'd */
var $msg;


/**
 * Shows server-side error and returns its presence.
 * @param {Object} json - result of any ajax
 * @return {boolean} - error happened
 */
function javaError(json) {
    if(json.error)
        message(json.error);
    return !!json.error;
}


/**
 * Show error dialog.
 * @param {string} msg - error text
 * @param {function} retryFn - callback for retrying operation that had failed
 */
function ajaxError(/*xhr, status,*/ msg, retryFn) {
    message(msg + '...<br><a href="#" id="retry">Ismétlés</a>&nbsp;&nbsp;<a href="#" id="cancelMsg">Mégse</a>');
    $('#retry', $msg).click(function() {
        retryFn.call(this);
    });
    $('#cancelMsg', $msg).click(function() {
        $msg.hide();
    });
}


/**
 * Set message text and reposition its window.
 * @param {string} msg - message
 */
function message(msg) {
    if(!$msg)
        $msg = $('#message');
    $msg.html(msg + '&nbsp;<a href="#" id="cancelMsg">Bezár</a>');
    $('#cancelMsg', $msg).click(function() {
        $msg.hide();
    });
    var $win = $(window);
    $msg.css({
        'top': Math.floor(($win.height() - $msg.height() - 20) / 2) + 'px',
        'left': Math.floor(($win.width() - $msg.width() - 20) / 2) + 'px',
        'display': 'block'
    });
}


function throttle(init, delay, cb) {
    var timer;

    if(init)
        cb.call();
    return function() {
        clearTimeout(timer);
        timer = setTimeout(cb, delay);
    };
}


/**
 * Shows/hides dialogs.
 * @param {number} index - of dialog in ids array
 * @param {boolean} toggle - or show
 */
function dialog(index, toggle) {
    var ids = [$('#searchPop'), $('#sectionPop')];
    for(var i in ids)
        if(i == index)
            if(toggle)
                ids[i].toggle();
            else
                ids[i].show();
        else
            ids[i].hide();
}


function menuModifier(e) {
    return e.altKey && (!client.system.mac || e.ctrlKey);
}


function resizeEvent() {
    window.onresize = throttle(true, 100, function() {
        var $m = $('#measure');
        var winHgt = $m.height();
        var winWid = $m.width();
        var headHgt = $('#header').height();
        dom.$content[0].style.top = headHgt + 'px';
        dom.$content.innerHeight(winHgt - headHgt);
        dom.$content.innerWidth(winWid);
    });
}


$.extend(exports, {
    javaError: javaError,
    ajaxError: ajaxError,
    dialog: dialog,
    menuModifier: menuModifier,
    resizeEvent: resizeEvent
});
