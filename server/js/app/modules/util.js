
var dom = require('./dom');

/** @type {JQuery} - points to menu box, lazy init'd */
var $menu;

/** @type {JQuery} - points to centered message box, lazy init'd */
var $msg;

/** @type {boolean} */
var menuVisible;


function toggleMenu(close, onEmpty) {
    if(!$menu)
        $menu = $('#menu');
    var visible = $menu.css('display') === 'block';
    var hgt = $menu.height();
    if(close) {
        if(!visible)
            return;
        if(onEmpty && hgt > 5)
            return;
    }
    if($menu.height() > 5 || visible) {
        $menu.toggle();
        menuVisible = !visible;
    }
}


function isMenuVisible() {
    return menuVisible;
}


function refreshMenu() {
    setTimeout(function() {
        dom.$header.children().each(function(ix, el) {
            var $menuEl = $('#menu-' + el.id);
            $menuEl.toggle(el.offsetTop > 30);
        });
        toggleMenu(true, true);
    }, 1);
}


/**
 * Shows server-side error and returns its presence.
 * @param {Object} json - result of any ajax
 * @return {boolean} - error happened
 */
function javaError(json) {
    if(json.error)
    {
        if(json.error === 'expired')
            document.location = '/app';
        else
            message(json.error, true);
    }
    return !!json.error;
}


/**
 * Show error dialog.
 * @param {string} msg - error text
 * @param {function} retryFn - callback for retrying operation that had failed
 */
function ajaxError(/*xhr, status,*/ msg, retryFn) {
    message(msg + '...<br><a href="#" id="retry">Ismétlés</a>&nbsp;&nbsp;<a href="#" id="cancelMsg">Mégse</a>', false);
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
 * @param {boolean} needsCloser - need to add link to close?
 */
function message(msg, needsCloser) {
    if(!$msg)
        $msg = $('#message');
    if(needsCloser)
        msg += '&nbsp;<a href="#" id="cancelMsg">Bezár</a>';
    $msg.html(msg);
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
        refreshMenu();
    });
}


$.extend(exports, {
    toggleMenu: toggleMenu,
    isMenuVisible: isMenuVisible,
    refreshMenu: refreshMenu,
    javaError: javaError,
    ajaxError: ajaxError,
    dialog: dialog,
    menuModifier: menuModifier,
    resizeEvent: resizeEvent
});
