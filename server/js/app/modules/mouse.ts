
import dom from './dom';
import * as util from './util';
import * as task from './task';
import * as html from './html-content';
import * as load from './load';
import * as bookmark from './bookmark';
import * as profile from './profile';
import * as render from './render';
import * as toc from './toc';
import * as view from './view';

export function init() {
    $('#pandit-icon').click(function() {
        util.toggleMenu();
    });

    $('#search-link, #menu-search-link').click(function() {
        util.toggleMenu(true);
        task.searchDialog();
    });

    $('#section-link, #menu-section-link').click(function() {
        util.toggleMenu(true);
        toc.openForJumpToSection();
    });

    $('#bookmark-link, #menu-bookmark-link').click(function() {
        util.toggleMenu(true);
        util.hideAllDialogs();
        bookmark.loadPage();
    });

    $('#logout-link, #menu-logout-link').click(function() {
        util.toggleMenu(true);
        util.hideAllDialogs();
        task.logout();
    });

    $('#view-link, #menu-view-link').click(function() {
        util.toggleMenu(true);
        view.showDialog();
    });

    $('#profile-link, #menu-profile-link').click(function() {
        util.toggleMenu(true);
        util.hideAllDialogs();
        profile.loadPage();
    });

    $('#help-link, #menu-help-link').click(function() {
        util.toggleMenu(true);
        util.hideAllDialogs();
        html.load('/app/dialog/html/help');
    });

    $('.prev-page').click(load.contextPrev);
    $('.next-page').click(load.contextNext);
    $('.switch-view').click(load.contextSwitch);
    dom.$shortRef.click(render.displayBookTitle);
    dom.$menuShortRef.click(render.displayBookTitle);

    dom.$sectDown.click(load.continuation);

    $('#info-icon').click(function() {
        util.downtimeMsg();
    });

    dom.$thisSect.click(load.currentHitSection);
}
