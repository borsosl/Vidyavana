
import dom from './modules/dom';
import * as util from './modules/util';
import * as task from './modules/task';
import * as toc from './modules/toc';
import * as search from './modules/search';
import * as load from './modules/load';
import * as keyboard from './modules/keyboard';
import * as mouse from './modules/mouse';
import * as touch from './modules/touch';

let initd = false;

export function init()
{
    if(pg.afterLogin) {
        window.location.href = '/app';
        return;
    }
    if(initd)
        return;
    dom.init();
    pg.ref = load.hitlistClick;
    if(pg.justRegistered)
        util.message('A regisztráció véglegesítéséhez<br/>kiküldtünk egy ellenőrző e-mailt.<br/>', true);
    if(pg.downtime)
        util.downtime(pg.downtime);
    search.init();
    toc.initSectionSelect();
    keyboard.init();
    mouse.init();
    touch.init();
    util.resizeEvent();
    util.refreshMenu();
    task.initView();
    task.searchDialog();
    dom.$loading.hide();
    // to preload image, it is hidden only by negative left in css, position now
    dom.$loading[0].style.left = '50%';
    initd = true;
}

$(function()
{
    init();
});
