
function init() {
    /** @type {JQuery} - container for text and buttons */
    this.$content = $('#content');
    /** @type {JQuery} - container for text content */
    this.$txt = $('#text');
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
}


function initMenu() {
    var $head = $('#header');
    $head.children().each(function(ix, el) {
        var $menuEl = $('#menu-' + el.id);
        $menuEl.toggle(el.offsetTop > 30);
    });
}

/**
 * @type {{init, initMenu, $content, $txt, $textBtns, $hitBtns, $sectDown, $thisSect, $shortRef, $menuShortRef}}
 */
var dom = exports;
dom.init = init;
dom.initMenu = initMenu;
