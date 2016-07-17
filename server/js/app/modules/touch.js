
var page = require('./page').instance;
var load = require('./load');

var cs = client.system;
if(cs.android || cs.ios || cs.iphone || cs.ipad || cs.winMobile)
{
    $(window).on('swipeleft', function()
    {
        if(page.isSearchResult())
            load.nextHit();
        else if(page.bookId())
            load.nextSection();

    }).on('swiperight', function()
    {
        if(page.isSearchResult())
            load.prevHit();
        else if(page.bookId())
            load.prevSection();
    });
}
