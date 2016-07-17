
var dom = require('./dom');

/** @type {JQuery} - points to menu box, lazy init'd */
var $menu;

/** @type {JQuery} - points to centered message box, lazy init'd */
var $msg;

/** @type {boolean} */
var menuVisible;

/** @type {number} - loading timeout handle */
var loadingHandle;

/** @type {number} - concurrent loading contexts */
var reentrantLoading = 0;

/** @type {string} */
var downtimeText;


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
        dialog(-1, false);
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
    loading(false);
    if(json.error)
    {
        if(json.error === 'expired')
            document.location = '/app';
        else if(json.error === 'maintenance')
            document.location = '/app/maintenance';
        else
            message('Hiba történt. <a href="mailto:dev@pandit.hu?subject=Hibajelentés ('+
                json.error+')">Beszámolok róla</a>', true);
    }
    if(json.downtime)
        downtime(json.downtime);
    else if(downtimeText)
        downtime(null);
    return !!json.error;
}


/**
 * Show error dialog.
 * @param {string} msg - error text
 * @param {function} retryFn - callback for retrying operation that had failed
 */
function ajaxError(/*xhr, status,*/ msg, retryFn) {
    loading(false);
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


function loading(state) {
    if(!state) {
        if(--reentrantLoading <= 0) {
            dom.$loading.hide();
            reentrantLoading = 0;
        }
        if(!reentrantLoading && loadingHandle) {
            clearTimeout(loadingHandle);
            loadingHandle = 0;
        }
        return;
    }
    var alreadyVisibleOrAboutToShow = reentrantLoading++;
    if(alreadyVisibleOrAboutToShow)
        return;
    loadingHandle = setTimeout(showIndicator, 500);

    function showIndicator() {
        dom.$loading.show();
        loadingHandle = 0;
    }
}


function downtime(text) {
    downtimeText = text;
    if(!text)
        text = '';
    $('#info-icon').css('display', text ? 'inline-block' : 'none').attr('title', 'Karbantartás: '+text);
}


function downtimeMsg() {
    message('Karbantartási leállás ideje: ' + downtimeText + '<br/>', true);
}


function menuModifier(e) {
    return e.altKey && (!client.system.mac || e.ctrlKey);
}


function resizeEvent() {
    window.onresize = throttle(true, 100, function() {
        resizeContent();
        refreshMenu();
    });
}


function resizeContent() {
    var $m = $('#measure');
    var winHgt = $m.height();
    var winWid = $m.width();
    var headHgt = $('#header').height();
    var arr = [dom.$content, dom.$formContent];
    for(var i in arr) {
        var $e = arr[i];
        $e[0].style.top = headHgt + 'px';
        $e.innerHeight(winHgt - headHgt);
        $e.innerWidth(winWid);
    }
}


function cookie(key, value) {
    if(value === undefined) {
        value = document.cookie.match('(^|;)\\s*' + key + '\\s*=\\s*([^;]+)');
        return value ? value.pop() : '';
    }
    document.cookie = encodeURIComponent(key) + "=" + encodeURIComponent(value) + '; path=/';
}


$.extend(exports, {
    toggleMenu: toggleMenu,
    isMenuVisible: isMenuVisible,
    refreshMenu: refreshMenu,
    javaError: javaError,
    ajaxError: ajaxError,
    message: message,
    dialog: dialog,
    loading: loading,
    downtime: downtime,
    downtimeMsg: downtimeMsg,
    menuModifier: menuModifier,
    resizeEvent: resizeEvent,
    resizeContent: resizeContent,
    cookie: cookie
});
