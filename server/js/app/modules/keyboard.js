
var page = require('./page').instance;
var util = require('./util');
var task = require('./task');
var load = require('./load');
var search = require('./search');

$(window).keydown(function(e) {
    var c = e.keyCode;
    if(c === 39)     		    // right
    {
        if(!page.isSearchResult())
            load.text(load.mode.down);
    }
    else if(c === 13)		    // enter
    {
        var ae = document.activeElement;
        if(ae && ae.onclick)
            ae.onclick();
        else {
            var sr = search.get();
            if(!sr || !sr.last() || sr.last().endHit == -1)
                load.currentOrNextSection();
        }
    }
    else if(c === 8)	    	// backspace
    {
        if(page.bookId())
            load.text(load.mode.prev);
        //noinspection JSUnresolvedFunction
        e.preventDefault();
    }
    else if(c === 75)           // k
    {
        task.searchDialog();
        //noinspection JSUnresolvedFunction
        e.preventDefault();
    }
    else if(c === 83)           // s
    {
        util.dialog(1, true);
        //noinspection JSUnresolvedFunction
        $('#sect1')[0].focus();
        //noinspection JSUnresolvedFunction
        e.preventDefault();
    }
    else if(c === 27) {		    // esc
        if(util.isMenuVisible())
            util.toggleMenu(true);
        else
            util.dialog(-1, false);
    }
    else if(c === 188 || c === 109)		    // , or -
    {
        if(search.get())
            load.text(load.mode.prevHit);
    }
    else if(c === 190 || c === 107)		    // . or +
    {
        if(search.get())
            load.text(load.mode.nextHit);
    }
    else if(c === 88 && util.menuModifier(e))           // alt-x
        util.toggleMenu();
    else if(c === 80 && util.menuModifier(e))           // alt-p
        task.logout();
});
