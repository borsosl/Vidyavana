
import page from './page';
import util from './util';
import task from './task';
import load from './load';
import bookmark from './bookmark';

export function init() {
    $(window).keydown(function(e: JQueryEventObject) {
        const c = e.keyCode;
        if(c === 39)     		    // right
        {
            if(!page.isSearchResult())
                load.continuation();
        }
        else if(c === 13)		    // enter
        {
            let ae = document.activeElement as HTMLElement;
            if(!ae || ae.tagName.toLowerCase() === 'div') {
                load.contextSwitch();
                e.preventDefault();
            }
            else if(ae) {
                if(ae.onclick)
                    ae.onclick(undefined);
                else {
                    const $btn = util.findClickableButton(ae);
                    if($btn)
                        $btn.click();
                }
                e.preventDefault();
            }
        }
        else if(c === 8)	    	// backspace
        {
            e.preventDefault();
        }
        else if(c === 75)           // k
        {
            task.searchDialog();
            e.preventDefault();
        }
        else if(c === 83)           // s
        {
            util.dialog(1, true);
            //noinspection JSUnresolvedFunction
            $('#sect1')[0].focus();
            e.preventDefault();
        }
        else if(c === 69)           // e
        {
            util.dialog(-1, false);
            bookmark.load();
            e.preventDefault();
        }
        else if(c === 27) {		    // esc
            if(util.isMenuVisible())
                util.toggleMenu(true);
            else
                util.dialog(-1, false);
            util.focusContent();
        }
        else if(c === 188 || c === 109)		    // , or -
            load.contextPrev();
        else if(c === 190 || c === 107)		    // . or +
            load.contextNext();
        else if(c === 88 && util.menuModifier(e))           // alt-x
            util.toggleMenu();
        else if(c === 80 && util.menuModifier(e))           // alt-p
            task.logout();
    });
}
