
import page from './page';
import dom from './dom';
import util from './util';
import highlight from './highlight';
import load from './load';
import search from './search';

/**
 * On successful load, add text into DOM.
 * @param json - loaded text details
 * @param mode - one of {@link load.mode}
 */
function text(json: DisplayBlock | SearchResponse, mode: number) {
    const initPage = mode != load.mode.down;
    const resp = json as SearchResponse;
    const isSearch = resp.hitCount !== undefined;
    let isHitlist = false;
    const display: DisplayBlock = isSearch ? resp.display : json as DisplayBlock;

    // set page values and select panel
    if(isSearch)
    {
        if(display && display.downtime)
            util.downtime(display.downtime);
        if(resp.hitCount)
        {
            if(mode == load.mode.search)
            {
                search.accept();
                util.dialog(-1, false);
            }
            search.inst().last = resp;
        }
        else
            return search.message('Nincs találat.');
        util.showHitsPanel();
        page.hits.init(display);
    } else {
        util.showSectionPanel();
        if(initPage)
            page.section.init(display);
        else
            page.section.down(display);
    }

    let h = display.text;
    if(!h)
        return;

    // set content
    if(initPage)
    {
        dom.$formContent.hide();
        dom.$content.scrollTop(0);
        if(isSearch) {
            isHitlist = resp.endHit > -1;
            const ref = '<div class="long-ref">' + (resp.startHit + 1) +
                (isHitlist ? '-' + resp.endHit : '') +
                ' / ' + resp.hitCount + ' : ' +
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
    page.current().shortRef = display.shortRef;
    dom.$shortRef.text(display.shortRef);
    dom.$menuShortRef.text(display.shortRef);
    util.refreshMenu();

    const hl = highlight.inst();
    if(hl)
        hl.run(h, isSearch ? dom.$hits : dom.$txt);

    if(isHitlist) {
        dom.$content.scrollTop(0);
        $('a', dom.$hits).filter(':first').focus();
        page.hits.activeElement = document.activeElement as HTMLElement;
    }
    else
        util.focusContent(initPage);
}

function hitlistMarkup(h: string) {
    h = h.replace(/<td><a (\d+)>/g, '<td><a href="#" onclick="pg.ref($1);">');
    h = h.replace(/<\/td><td (\d+)>/g, '</td><td><p data-ix="$1">');
    h = h.replace(/<\/td><\/tr>/g, '</p></td></tr>');
    return h;
}


export default {
    text
};
