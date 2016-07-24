
/** @type {Page} - section display */
var section = new Page();
/** @type {Page} - hits display */
var hits = new Page();
/** @type {Page} - current display = section|hits */
var current = section;

function Page()
{
    /** @type {number} - book id (segment # (eg. canto/lila) is << 16 bits) */
    var bookSegmentId;
    /** @type {number} - current TOC id */
    var tocId;
    /** @type {number} - 1-based index of the next, unloaded paragraph in current section.
     *      0=fully loaded section. -1=search render. */
    var last;
    /** @type {number} - saved scroll position for repositioning */
    var scrollPos;
    /** @type {HTMLElement} - saved active link of hitlist */
    var activeElement;


    /**
     * Sets fields for current page content.
     * @param {DisplayBlock} json - loaded section info
     * @returns {boolean} - was reset
     */
    function init(json)
    {
        //noinspection JSValidateTypes
        current = this;
        last = json.last;
        scrollPos = -1;
        activeElement = null;

        if(!json.tocId)
            return false;
        bookSegmentId = json.bookSegmentId;
        tocId = json.tocId;
        return true;
    }


    /**
     * Sets last request data.
     * @param {DisplayBlock} json - loaded chunk and book info
     */
    function down(json)
    {
        last = json.last;
    }


    /**
     * Gets next para ordinal, if section has more to load.
     * @return {?number} - next para or null
     */
    function next()
    {
        return last ? last : null;
    }

    /**
     * Sets or gets scroll position of the content panel
     * @param {number?} position
     * @return {?number}
     */
    function scrollPosFn(position) {
        if(position !== undefined)
            scrollPos = position;
        return scrollPos;
    }

    /**
     * Sets or gets scroll position of the content panel
     * @param {HTMLElement} element
     * @return {?HTMLElement}
     */
    function activeElementFn(element) {
        if(element)
            activeElement = element;
        return activeElement;
    }

    $.extend(this, {
        init: init,
        down: down,
        next: next,
        scrollPos: scrollPosFn,
        activeElement: activeElementFn,
        bookId: function(){return bookSegmentId;},
        tocId: function(){return tocId;}
    });
}

$.extend(exports, {
    section: function() { return section; },
    hits: function() { return hits; },
    current: function(o) { if(o) current = o; return current; },
    isSearchResult: function(){return current === hits;}
});
