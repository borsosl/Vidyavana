var dom = require('./modules/dom');
var util = require('./modules/util');
var task = require('./modules/task');
var toc = require('./modules/toc');
var search = require('./modules/search');

var initd = false;

function init()
{
    if(initd)
        return;
    dom.init();
    if(pg.justRegistered)
        util.message('A regisztráció véglegesítéséhez<br/>kiküldtünk egy ellenőrző e-mailt.<br/>', true);
    if(pg.downtime)
        util.downtime(pg.downtime);
    search.init();
    toc.initSectionSelect();
    require('./modules/keyboard');
    require('./modules/mouse');
    require('./modules/touch');
    util.resizeEvent();
    util.refreshMenu();
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

exports.init = init;
