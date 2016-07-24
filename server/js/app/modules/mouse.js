
var dom = require('./dom');
var util = require('./util');
var task = require('./task');
var html = require('./html-content');
var load = require('./load');

$('#pandit-icon').click(function()
{
    util.toggleMenu();
});

$('#search-link, #menu-search-link').click(function()
{
    util.toggleMenu(true);
    task.searchDialog();
});

$('#section-link, #menu-section-link').click(function()
{
    util.toggleMenu(true);
    util.dialog(1, true);
    //noinspection JSUnresolvedFunction
    $('#sect1')[0].focus();
});

$('#logout-link, #menu-logout-link').click(function()
{
    util.toggleMenu(true);
    util.dialog(-1, false);
    task.logout();
});

$('#help-link, #menu-help-link').click(function()
{
    util.toggleMenu(true);
    util.dialog(-1, false);
    html.load('/app/dialog/html/help');
});

$('.prev-page').click(load.contextPrev);

$('.next-page').click(load.contextNext);

$('.switch-view').click(load.contextSwitch);

dom.$sectDown.click(load.continuation);

$('#info-icon').click(function()
{
    util.downtimeMsg();
});

dom.$thisSect.click(load.currentHitSection);
