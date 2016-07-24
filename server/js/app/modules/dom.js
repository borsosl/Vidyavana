
function init() {
    /** @type {JQuery} - top action bar */
    this.$header = $('#header');
    /** @type {JQuery} - container for text and buttons */
    this.$content = $('#content');
    /** @type {JQuery} - container for forms */
    this.$formContent = $('#form-content');
    /** @type {JQuery} - container for text content */
    this.$txt = $('#text');
    /** @type {JQuery} - container for search result content */
    this.$hits = $('#hits');
    /** @type {JQuery} - button row */
    this.$textBtns = $('#text-buttons');
    /** @type {JQuery} - button row */
    this.$hitBtns = $('#hit-buttons');
    /** @type {JQuery} - buttons to load more of section or go to full section */
    this.$sectDown = $('#sect-down');
    /** @type {JQuery} */
    this.$thisSect = $('#this-sect');
    /** @type {JQuery} */
    this.$shortRef = $('#short-ref');
    /** @type {JQuery} */
    this.$menuShortRef = $('#menu-short-ref');
    /** @type {JQuery} */
    this.$loading = $('#loading');
}

/**
 * @type {{init, nowarn, $header, $content, $formContent, $txt, $hits, $textBtns, $hitBtns,
 *      $sectDown, $thisSect, $shortRef, $menuShortRef, $loading}}
 */
var dom = exports;
dom.init = init;
dom.nowarn = 0;
