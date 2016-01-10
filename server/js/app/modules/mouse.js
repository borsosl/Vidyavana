
var page = require('./page').instance;
var dom = require('./dom');
var util = require('./util');
var task = require('./task');
var html = require('./html-content');
var load = require('./load');
var search = require('./search');

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

$('.prev-sect').click(function()
{
    if(page.bookId())
        load.text(load.mode.prev);
});

$('.next-sect').click(load.thisNextSection);

dom.$sectDown.click(function()
{
    load.text(load.mode.down);
});

$('.prev-hit').click(function()
{
    if(search.get())
        load.text(load.mode.prevHit);
});

$('.next-hit').click(function()
{
    if(search.get())
        load.text(load.mode.nextHit);
});

$('#info-icon').click(function()
{
    util.downtimeMsg();
});

dom.$thisSect.click(load.thisNextSection);
