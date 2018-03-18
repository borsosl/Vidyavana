
import * as page from './page';
import * as load from './load';

let mobile: boolean;

export function init() {
    const cs = client.system;
    mobile = cs.android || cs.ios || cs.iphone || cs.ipad || cs.winMobile;
    if(mobile) {
        $(window).on('swipeleft', function() {
            if(page.isSearchResult())
                load.nextHit();
            else if(page.section.bookId)
                load.nextSection();

        }).on('swiperight', function() {
            if(page.isSearchResult())
                load.prevHit();
            else if(page.section.bookId)
                load.prevSection();
        });
    }
}

export function isMobile() {
    return mobile;
}
