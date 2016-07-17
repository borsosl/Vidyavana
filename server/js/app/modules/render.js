
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
function text(json, mode) {
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
    var hitlist;
    if(h)
    {
        if(initPage)
        {
            dom.$formContent.hide();
            dom.$content.scrollTop(0);
            if(isSearch) {
                var ref = '<div class="long-ref">'+(json.startHit+1);
                if(json.endHit > -1) {
                    hitlist = true;
                    ref += '-' + json.endHit;
                }
                ref += ' / '+json.hitCount+' : ';
                if(json.endHit == -1)
                    ref += display.longRef;
                ref += '</div>';
                if(json.endHit > -1)
                    h = hitlistMarkup(h);
                dom.$txt.html(ref+h);
            }
            else
                dom.$txt.html(h);
            dom.$textBtns.toggle(!isSearch);
            dom.$hitBtns.toggle(isSearch);
        }
        else
            dom.$txt.append(h);
        dom.$sectDown.toggle(page.next() !== null);
        dom.$thisSect.toggle(page.isSearchResult() && json.endHit == -1);

        if(!display.shortRef)
            display.shortRef = '';
        dom.$shortRef.text(display.shortRef);
        dom.$menuShortRef.text(display.shortRef);
        util.refreshMenu();

        var hl = highlight.get();
        if(hl)
            hl.run(h);

        if(hitlist)
            $('a', dom.$txt).filter(':first').focus();
        else
            util.focusText(initPage);
    }
    if(isSearch && display.downtime)
        util.downtime(display.downtime);
}

function hitlistMarkup(h) {
    h = h.replace(/<td><a (\d+)>/g, '<td><a href="#" onclick="pg.ref($1);">');
    h = h.replace(/<\/td><td (\d+)>/g, '</td><td><p data-ix="$1">');
    h = h.replace(/<\/td><\/tr>/g, '</p></td></tr>');
    return h;
}


exports.text = text;
