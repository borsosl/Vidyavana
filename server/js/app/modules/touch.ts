
import * as page from './page';
import * as load from './load';

export function init() {
    const cs = client.system;
    if(cs.android || cs.ios || cs.iphone || cs.ipad || cs.winMobile)
    {
        $(window).on('swipeleft', function()
        {
            if(page.isSearchResult())
                load.nextHit();
            else if(page.section.bookId)
                load.nextSection();

        }).on('swiperight', function()
        {
            if(page.isSearchResult())
                load.prevHit();
            else if(page.section.bookId)
                load.prevSection();
        });
    }
}
