
import * as page from './page';
import * as util from './util';
import * as task from './task';
import * as load from './load';
import * as bookmark from './bookmark';
import * as toc from './toc';

export function init() {
    $(window).keydown(function(e: JQueryEventObject) {
        const c = e.keyCode;
        const menuModifier = util.menuModifier(e);
        const ae = document.activeElement as HTMLElement;
        const tagname = ae ? ae.tagName.toLowerCase() : null;
        if(!menuModifier && ae && tagname !== 'div' && tagname !== 'a' && c !== 13 && c !== 27)
            return;
        if(c === 39) {                  // right
            if(!page.isSearchResult())
                load.continuation();
        } else if(c === 13) {           // enter
            if(!ae || tagname === 'div') {
                load.contextSwitch();
                e.preventDefault();     // must have
            } else if(ae) {
                if(ae.onclick)
                    ae.onclick(undefined);
                else {
                    const ev = ($ as any)._data(ae, 'events');
                    if(ev && ev.click && ae.tagName === 'button')
                        $(ae).click();
                    else {
                        const $btn = util.findButtonClass(ae, 'ok');
                        if($btn)
                            $btn.click();
                    }
                }
                e.preventDefault();
            }
        } else if(c === 8) {            // backspace
            e.preventDefault();
        } else if(c === 75) {           // k
            task.searchDialog();
            e.preventDefault();
        } else if(c === 83) {           // s
            toc.openForJumpToSection();
            $('#sect1')[0].focus();
            e.preventDefault();
        } else if(c === 69) {           // e
            util.hideAllDialogs();
            bookmark.loadPage();
            e.preventDefault();
        } else if(c === 27) {		    // esc
            const $btn = ae ? util.findButtonClass(ae, 'cancel') : null;
            if($btn && $btn.length)
                $btn.click();
            else if(util.isMenuVisible())
                util.toggleMenu(true);
            else if(util.dialog.shown > util.dialog.id.none)
                util.hideAllDialogs();
            else if(util.hitsPanelShown) {
                util.showSectionPanel();
                page.current(page.section);
            }
            util.focusContent();
        } else if(c === 188 || c === 109)		    // , or -
            load.contextPrev();
        else if(c === 190 || c === 107)		        // . or +
            load.contextNext();
        else if(c === 88 && menuModifier)           // alt-x
            util.toggleMenu();
        else if(c === 80 && menuModifier)           // alt-p
            task.logout();
    });
}
