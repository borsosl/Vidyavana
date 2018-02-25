
import dom from './dom';
import util from './util';
import task from './task';
import html from './html-content';
import load from './load';
import bookmark from './bookmark';

export function init() {
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
        $('#sect1')[0].focus();
    });

    $('#bookmark-link, #menu-bookmark-link').click(function()
    {
        util.toggleMenu(true);
        util.dialog(-1, false);
        bookmark.load();
    });

    $('#logout-link, #menu-logout-link').click(function()
    {
        util.toggleMenu(true);
        util.dialog(-1, false);
        task.logout();
    });

    $('#view-link, #menu-view-link').click(function()
    {
        util.toggleMenu(true);
        task.viewDialog();
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

    $('#viewGo').click(function()
    {
        task.applyView();
    });

    $('#info-icon').click(function()
    {
        util.downtimeMsg();
    });

    dom.$thisSect.click(load.currentHitSection);
}
