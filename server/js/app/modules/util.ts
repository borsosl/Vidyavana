
import dom from './dom';
import * as page from './page';

/** points to menu box, lazy init'd */
let $menu: JQuery;

/** points to centered message box, lazy init'd */
let $msg: JQuery;

let menuVisible: boolean;

/** loading timeout handle */
let loadingHandle: number;

/** concurrent loading contexts */
let reentrantLoading: number = 0;

let downtimeText: string;


export function toggleMenu(close?: boolean, onEmpty?: boolean) {
    if(!$menu)
        $menu = $('#menu');
    const visible = $menu.css('display') === 'block';
    const hgt = $menu.height();
    if(close) {
        if(!visible)
            return;
        if(onEmpty && hgt > 5)
            return;
    }
    if($menu.height() > 5 || visible) {
        hideAllDialogs();
        $menu.toggle();
        menuVisible = !visible;
    }
}


export function isMenuVisible() {
    return menuVisible;
}


export function refreshMenu() {
    setTimeout(function() {
        dom.$header.children().each(function(ix: number, el: HTMLDivElement) {
            const $menuEl = $('#menu-' + el.id);
            $menuEl.toggle(el.offsetTop > 30);
        });
        toggleMenu(true, true);
    }, 1);
}

export let hitsPanelShown = false;

export function showSectionPanel() {
    toggleContent(1);
}

export function showHitsPanel() {
    toggleContent(2);
}

function toggleContent(mode: number) {
    hitsPanelShown = mode === 2;
    dom.$txt.toggle(!hitsPanelShown);
    dom.$hits.toggle(hitsPanelShown);
}


export function focusContent(scrollToTop?: boolean) {
    dom.$content.focus();
    if(scrollToTop)
        dom.$content.scrollTop(0);
}


export function toggleButtonBars(isHits: boolean, hitlist: boolean) {
    dom.$textBtns.toggle(!isHits);
    dom.$hitBtns.toggle(isHits);
    dom.$sectDown.toggle(page.current().downOrdinal() !== null);
    dom.$thisSect.toggle(page.isSearchResult() && !hitlist);
    dom.$prevPage.toggle(page.section.isBackAvailable());
}


/**
 * Shows server-side error and returns its presence.
 * @param json - result of any ajax
 * @return true if error happened
 */
export function javaError(json: any): boolean {
    loading(false);
    if(json.error) {
        if(json.error === 'expired')
            document.location.href = '/app';
        else if(json.error === 'maintenance')
            document.location.href = '/app/maintenance';
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
 * @param msg - error text
 * @param retryFn - callback for retrying operation that had failed
 */
export function ajaxError(/*xhr, status,*/ msg: string, retryFn: () => void) {
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
 * @param msg - message
 * @param needsCloser - need to add link to close?
 */
export function message(msg: string, needsCloser: boolean) {
    if(!$msg)
        $msg = $('#message');
    if(needsCloser)
        msg += '&nbsp;<a href="#" id="cancelMsg">Bezár</a>';
    $msg.html(msg);
    $('#cancelMsg', $msg).click(function() {
        $msg.hide();
    });
    const $win = $(window);
    $msg.css({
        top: Math.floor(($win.height() - $msg.height() - 20) / 2) + 'px',
        left: Math.floor(($win.width() - $msg.width() - 20) / 2) + 'px',
        display: 'block'
    });
}


function throttle(init: boolean, delay: number, cb: () => void) {
    let timer: number;

    if(init)
        cb.call(undefined);
    return function() {
        clearTimeout(timer);
        timer = setTimeout(cb, delay);
    };
}

let $dialogs: JQuery[];

/**
 * Shows/hides dialogs.
 * @param index - of dialog in ids array
 * @param toggle - or show
 * @return true if dialog is now visible
 */
export function dialog(index: number, toggle: boolean): boolean {
    if(!$dialogs)
        $dialogs = [$('#searchPop'), $('#sectionPop'), $('#viewPop'), $('#typesPop')];
    let ret = false;
    dialog.shown = dialog.id.none;
    const ix = ''+index;
    for(const i in $dialogs)
        if(i === ix) {
            if(toggle)
                $dialogs[i].toggle();
            else
                $dialogs[i].show();
            ret = $dialogs[i].is(':visible');
            if(ret)
                dialog.shown = index;
        } else {
            $dialogs[i].hide();
        }
    return ret;
}

export namespace dialog {
    export const enum id {
        all = -1, none = -1, search, section, view, types
    }
    export let shown: id;
}

export function hideAllDialogs() {
    dialog(dialog.id.all, false);
    if(hitsPanelShown)
        $('a', dom.$hits).filter(':first').focus();
    else
        focusContent();
}


export function loading(state: boolean) {
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
    const alreadyVisibleOrAboutToShow = reentrantLoading++;
    if(alreadyVisibleOrAboutToShow)
        return;
    loadingHandle = setTimeout(showIndicator, 500);

    function showIndicator() {
        dom.$loading.show();
        loadingHandle = 0;
    }
}


export function downtime(text: string) {
    downtimeText = text;
    if(!text)
        text = '';
    $('#info-icon').css('display', text ? 'inline-block' : 'none').attr('title', text);
}


export function downtimeMsg() {
    message(downtimeText + '<br/>', true);
}


export function menuModifier(e: JQueryKeyEventObject): boolean {
    if(!e.altKey)
        return false;
    if(client.system.mac && !e.ctrlKey)
        return false;
    return true;
}


export function resizeEvent() {
    window.onresize = throttle(true, 100, function() {
        resizeContent();
        refreshMenu();
    });
}


export function resizeContent() {
    const $m = $('#measure');
    const winHgt = $m.height();
    const winWid = $m.width();
    const headHgt = $('#header').height();
    const arr = [dom.$content, dom.$formContent];
    for(const i in arr) {
        const $e = arr[i];
        $e[0].style.top = headHgt + 'px';
        $e.innerHeight(winHgt - headHgt);
        $e.innerWidth(winWid);
    }
}

export function findButtonClass(e: Element, cls: string) {
    const $group = $(e).closest('.has-button');
    return $group ? $('.'+cls+'-button:visible', $group) : null;
}

export function bookOrdinalOnTop() {
    if(page.isSearchResult())
        return -page.hits.tocId;

    const top = dom.$content.scrollTop();
    const $ch = dom.$txt.children();
    let $prev;
    for(const i in $ch) {
        const $e = $($ch[i]);
        const pos = $e.position().top;
        if(pos === top) {
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


export function binarySearch<T>(arr: T[], comparator: (item: T) => number): number {
    let first = 0;
    let last = arr.length - 1;
    while(first <= last) {
        const mid = Math.floor((last + first) / 2);
        const cmp = comparator(arr[mid]);
        if(cmp > 0)
            first = mid + 1;
        else if(cmp < 0)
            last = mid - 1;
        else
            return mid;
    }
    return -first - 1;
}

export function findStartIndexBefore<T>(arr: T[], ix: number, comparator: (item: T) => number) {
    while(ix > 0) {
        if(comparator(arr[ix-1]) !== 0)
            break;
        ix--;
    }
    return ix;
}

export function compare<T>(a: T, b: T) {
    return a < b ? -1 : a > b ? 1 : 0;
}
