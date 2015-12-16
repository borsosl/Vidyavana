
var page = require('./page').instance;
var dom = require('./dom');
var util = require('./util');
var task = require('./task');
var load = require('./load');
var search = require('./search');

$('#searchLnk').click(function()
{
    task.searchDialog();
});

$('#sectionLnk').click(function()
{
    util.dialog(1, true);
    //noinspection JSUnresolvedFunction
    $('#sect1')[0].focus();
});

$('#logoutLnk').click(function()
{
    task.logout();
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

dom.$thisSect.click(load.thisNextSection);
