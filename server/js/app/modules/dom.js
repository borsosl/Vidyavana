/**
 * @type {{init, $content, $txt, $textBtns, $hitBtns, $sectDown, $thisSect}}
 */
var dom = {
    init: function() {
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
    }
};

module.exports = dom;
