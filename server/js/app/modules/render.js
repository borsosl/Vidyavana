
var page = require('./page');
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
    var initPage = mode != load.mode.down;
    var isSearch = json.hitCount !== undefined;
    var isHitlist = false;
    var display = isSearch ? json.display : json;

    // set page values and select panel
    if(isSearch)
    {
        if(display.downtime)
            util.downtime(display.downtime);
        if(json.hitCount)
        {
            if(mode == load.mode.search)
            {
                search.accept();
                util.dialog(-1, false);
            }
            search.inst().last(json);
        }
        else
            return search.message('Nincs találat.');
        util.showHitsPanel();
        page.hits().init(display);
    } else {
        util.showSectionPanel();
        if(initPage)
            page.section().init(display);
        else
            page.section().down(display);
    }

    var h = display.text;
    if(!h)
        return;

    // set content
    if(initPage)
    {
        dom.$formContent.hide();
        dom.$content.scrollTop(0);
        if(isSearch) {
            isHitlist = json.endHit > -1;
            var ref = '<div class="long-ref">'+(json.startHit+1) +
                (isHitlist ? '-' + json.endHit : '') +
                ' / '+json.hitCount+' : ' +
                (isHitlist ? '' : display.longRef) +
                '</div>';
            if(isHitlist)
                h = hitlistMarkup(h);
            dom.$hits.html(ref+h);
        }
        else
            dom.$txt.html(h);
    }
    else
        dom.$txt.append(h);
    util.toggleButtonBars(isSearch, isHitlist);

    if(!display.shortRef)
        display.shortRef = '';
    dom.$shortRef.text(display.shortRef);
    dom.$menuShortRef.text(display.shortRef);
    util.refreshMenu();

    var hl = highlight.inst();
    if(hl)
        hl.run(h, isSearch ? dom.$hits : dom.$txt);

    if(isHitlist) {
        dom.$content.scrollTop(0);
        $('a', dom.$hits).filter(':first').focus();
        page.hits().activeElement(document.activeElement);
    }
    else
        util.focusContent(initPage);
}

function hitlistMarkup(h) {
    h = h.replace(/<td><a (\d+)>/g, '<td><a href="#" onclick="pg.ref($1);">');
    h = h.replace(/<\/td><td (\d+)>/g, '</td><td><p data-ix="$1">');
    h = h.replace(/<\/td><\/tr>/g, '</p></td></tr>');
    return h;
}


exports.text = text;
