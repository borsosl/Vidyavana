var dom = require('./modules/dom');
var util = require('./modules/util');
var task = require('./modules/task');
var toc = require('./modules/toc');
var search = require('./modules/search');

$(function()
{
    dom.init();
    search.init();
    toc.initSectionSelect();
    require('./modules/keyboard');
    require('./modules/mouse');
    require('./modules/touch');
    util.resizeEvent();
    task.searchDialog();
});
