
var page = require('./page').instance;
var load = require('./load');

var cs = client.system;
if(cs.android || cs.ios || cs.iphone || cs.ipad || cs.winMobile)
{
    $(window).on('swipeleft', function()
    {
        if(page.isSearchResult())
            load.text(load.mode.nextHit);
        else if(page.bookId())
            load.text(load.mode.next);

    }).on('swiperight', function()
    {
        if(page.isSearchResult())
            load.text(load.mode.prevHit);
        else if(page.bookId())
            load.text(load.mode.prev);
    });
}
