
var dom = require('./dom');
var page = require('./page');

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

function showSectionPanel() {
    toggleContent(1);
}

function showHitsPanel() {
    toggleContent(2);
}

function toggleContent(mode) {
    dom.$txt.toggle(mode === 1);
    dom.$hits.toggle(mode === 2);
}


function focusContent(scrollToTop) {
    dom.$content.focus();
    if(scrollToTop)
        dom.$content.scrollTop(0);
}


function toggleButtonBars(isHits, hitlist) {
    dom.$textBtns.toggle(!isHits);
    dom.$hitBtns.toggle(isHits);
    dom.$sectDown.toggle(page.current().next() !== null);
    dom.$thisSect.toggle(page.isSearchResult() && !hitlist);
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
    $('#info-icon').css('display', text ? 'inline-block' : 'none').attr('title', text);
}


function downtimeMsg() {
    message(downtimeText + '<br/>', true);
}


function menuModifier(e) {
    //noinspection JSUnresolvedVariable
    return e.altKey && (!client.system.mac ^ e.ctrlKey);
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
        //noinspection JSCheckFunctionSignatures
        value = document.cookie.match('(^|;)\\s*' + key + '\\s*=\\s*([^;]+)');
        return value ? value.pop() : '';
    }
    document.cookie = encodeURIComponent(key) + "=" + encodeURIComponent(value) + '; path=/';
}


function findClickableButton(e) {
    var $group = $(e).closest('.has-button');
    return $group ? $('button', $group) : null;
}

function bookOrdinalOnTop() {
    if(page.isSearchResult())
        return -page.hits().tocId();

    var top = dom.$content.scrollTop();
    var $ch = dom.$txt.children();
    var $prev;
    for(var i in $ch) {
        var $e = $($ch[i]);
        var pos = $e.position().top;
        if(pos == top) {
            $prev = $e;
            break;
        }
        if(pos > top) {
            if(!$prev)
                $prev = $e;
            break;
        }
        $prev = $e;
    }
    return $prev.data('ix') + 1;    // ix 0-based, ordinal in toc tree 1-based
}

function inputDefaults(e) {
    if(!menuModifier(e))
        e.stopPropagation();
}


$.extend(exports, {
    toggleMenu: toggleMenu,
    isMenuVisible: isMenuVisible,
    refreshMenu: refreshMenu,
    showSectionPanel: showSectionPanel,
    showHitsPanel: showHitsPanel,
    focusContent: focusContent,
    toggleButtonBars: toggleButtonBars,
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
    cookie: cookie,
    findClickableButton: findClickableButton,
    bookOrdinalOnTop: bookOrdinalOnTop,
    textKeyDefaults: inputDefaults
});
