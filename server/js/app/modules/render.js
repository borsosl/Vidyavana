
var page = require('./page').instance;
var dom = require('./dom');
var util = require('./util');
var highlight = require('./highlight');
var load = require('./load');
var search = require('./search');

/**
 * On successful load, add text into DOM.
 * @param {DisplayBlock|SearchResponse} json - loaded text details
 * @param {number} mode - one of {@link load.mode}
 */
function text(json, mode)
{
    var isSearch = json.hitCount !== undefined;
    if(isSearch)
    {
        if(json.hitCount)
        {
            if(mode == load.mode.search)
            {
                search.accept();
                util.dialog(-1, false);
            }
            search.get().last(json);
        }
        else
            return search.message('Nincs találat.');
    }
    var display = isSearch ? json.display : json;
    var initPage = mode != load.mode.down;
    if(initPage)
        page.init(display, false);
    page.load(display);
    var h = display.text;
    if(h)
    {
        if(initPage)
        {
            dom.$content.scrollTop(0);
            if(isSearch)
                dom.$txt.html('<div class="long-ref">'+(json.hit+1)+' / '+json.hitCount+' : '+display.longRef+'</div>'+h);
            else
                dom.$txt.html(h);
            dom.$textBtns.toggle(!isSearch);
            dom.$hitBtns.toggle(!!search.get());
        }
        else
            dom.$txt.append(h);
        dom.$sectDown.toggle(page.next() !== null);
        dom.$thisSect.toggle(page.isSearchResult());

        if(!display.shortRef)
            display.shortRef = '';
        dom.$shortRef.text(display.shortRef);
        dom.$menuShortRef.text(display.shortRef);

        if(highlight.get())
            highlight.get().run(h);
    }
}


exports.text = text;
