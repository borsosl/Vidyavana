
var page = require('./page');
var load = require('./load');

var cs = client.system;
//noinspection JSUnresolvedVariable
if(cs.android || cs.ios || cs.iphone || cs.ipad || cs.winMobile)
{
    $(window).on('swipeleft', function()
    {
        if(page.isSearchResult())
            load.nextHit();
        else if(page.section().bookId())
            load.nextSection();

    }).on('swiperight', function()
    {
        if(page.isSearchResult())
            load.prevHit();
        else if(page.section().bookId())
            load.prevSection();
    });
}
