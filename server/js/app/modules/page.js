
function Page()
{
    /** @type {number} - book id (segment # (eg. canto/lila) is << 16 bits) */
    var bookSegmentId;
    /** @type {number} - current TOC id */
    var tocId;
    /** @type {number} - 1-based index of the next, unloaded paragraph. 0=fully loaded. -1=search render. */
    var last;


    /**
     * Sets fields for current page content.
     * @param {DisplayBlock} json - loaded section info
     * @param {boolean} force - force reload even if the same section was loaded
     * @returns {boolean} - was reset
     */
    function init(json, force)
    {
        if(!json.tocId && !force)
            return false;
        bookSegmentId = json.bookSegmentId;
        tocId = json.tocId;
        last = 0;
        return true;
    }


    /**
     * Sets last request data.
     * @param {DisplayBlock} json - loaded chunk and book info
     */
    function load(json)
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


    $.extend(this, {
        init: init,
        load: load,
        next: next,
        isSearchResult: function(){return last === -1;},
        bookId: function(){return bookSegmentId;},
        tocId: function(){return tocId;}
    });
}

exports.instance = new Page();
