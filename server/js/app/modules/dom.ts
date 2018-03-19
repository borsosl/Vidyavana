
class Dom {
    nowarn: number;
    $header: JQuery;
    $content: JQuery;
    $formContent: JQuery;
    $txt: JQuery;
    $hits: JQuery;
    $textBtns: JQuery;
    $hitBtns: JQuery;
    $sectDown: JQuery;
    $thisSect: JQuery;
    $shortRef: JQuery;
    $bookTitle: JQuery;
    $menuShortRef: JQuery;
    $loading: JQuery;

    constructor() {
        this.nowarn = 0;
    }

    init() {
        /** top action bar */
        this.$header = $('#header');
        /** container for text and buttons */
        this.$content = $('#content');
        /** container for forms */
        this.$formContent = $('#form-content');
        /** container for text content */
        this.$txt = $('#text');
        /** container for search result content */
        this.$hits = $('#hits');
        /** button row */
        this.$textBtns = $('#text-buttons');
        /** button row */
        this.$hitBtns = $('#hit-buttons');
        /** buttons to load more of section or go to full section */
        this.$sectDown = $('#sect-down');

        this.$thisSect = $('#this-sect');
        this.$shortRef = $('#short-ref');
        this.$bookTitle = $('#book-title');
        this.$menuShortRef = $('#menu-short-ref');
        this.$loading = $('#loading');
    }
}

export default new Dom();
