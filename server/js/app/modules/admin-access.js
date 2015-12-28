
var util = require('./util');

/** @type {JQuery} */
var $dialog;
/** @type {boolean} */
var checkboxesDrawn;
/** @type {BookPackageMap} */
var gBooks;
/** @type {string} */
var gEmail;
/** @type {string} */
var gAccess;
/** @const */
var packageNames = ['Sraddha', 'SadhuSanga', 'BhajanaKriya', 'Ruci'];
/** @type {Array.<JQuery>} */
var packageCb;
/** @type {Array.<boolean>} */
var packageState;


/**
 * Fetch user book access, then fill dialog
 * @param {string} email
 * @param {BookPackageMap} books
 */
function open(email, books) {
    gEmail = email;
    $('input[type=checkbox]', $dialog).prop('checked', false);
    $dialog.show();
    $.ajax({
        url: '/app/admin/init-access',
        method: 'post',
        data: {
            email: email
        },

        success: function(json)
        {
            if(util.javaError(json))
                return;
            initCheckboxes(books, json);
        },

        error: function(/*xhr, status*/)
        {
            util.ajaxError(/*xhr, status,*/ 'Hálózati hiba.', open.bind(null, email));
        }
    });
}


function reset() {
    checkboxesDrawn = false;
    packageCb = [];
    packageState = [];
    $dialog = $('#admin-access');
}


/**
 * Fill checkboxes with user access.
 * @param {BookPackageMap} books
 * @param {string} access
 */
function initCheckboxes(books, access) {
    drawCheckboxes(books);
    markAllPackages(false, false);
    gAccess = access;
    var parts = access.split('|');
    for(var p in parts) {
        var pt = parts[p];
        var pkg = parseInt(pt);
        if(!isNaN(pkg))
            markPackage(pkg, false, true);
        else if(pt.charAt(0) === '-')
            markBook(pt.substr(1), false);
        else
            markBook(pt, true);
    }
    markAllPackages(true);
}


/**
 * Init data and DOM.
 * @param {BookPackageMap} books
 */
function drawCheckboxes(books) {
    if(checkboxesDrawn)
        return;
    var $proto = $('.access-book', $dialog);
    for(var p in packageNames) {
        var pn = packageNames[p];
        packageCb.push($('#'+pn+'-cb'));
        packageState.push(false);
        /** @type {JQuery} */
        var $block = $('#'+pn+'-cb-row');
        var parts = books[pn].split('|');
        books[pn] = [];
        for(var i=0, len=parts.length; i<len; i+=2)
        {
            var abbr = parts[i];
            if(!abbr)
                continue;
            var id = parseInt(parts[i+1]);
            var $span = $proto.clone();
            $span.appendTo($block).data('id', id);
            $('.access-abbrev', $span).text(abbr);
            books[pn].push({
                pkg: parseInt(p) + 1,
                abbr: abbr,
                id: id,
                $span: $span,
                $cb: $('input', $span),
                state: false
            });
        }
    }
    packageCb.push($('#full-access'));
    $proto.remove();
    gBooks = books;
    $('input[type=checkbox]', $dialog).change(cbChange);
    $('#access-ok').click(ok);
    $('#access-cancel').click(cancel);
    checkboxesDrawn = true;
}


function cbChange() {
    var state = this.checked;
    if(this.id) {
        if(this.id === 'full-access')
            markFull(false, state);
        else {
            var pkg = this.id.substr(0, this.id.length-3);
            var pkgIx = packageNames.indexOf(pkg);
            packageState[pkgIx] = state;
            markPackageBooks(pkgIx+1, false, state);
            markFull(true);
        }
    } else {
        var bookId = $(this).parent().data('id');
        var bkMap = getBookMap(null, bookId);
        bkMap.state = state;
        markPackage(bkMap.pkg, true);
        markFull(true);
    }
}


function markFull(determine, state) {
    if(determine) {
        packageCb[4][0].checked =
            packageState[0] && packageState[1] && packageState[2] && packageState[3];
    }
    else {
        markAllPackages(false, state);
    }
}


function markAllPackages(determine, state) {
    for(var i=1; i<=4; ++i)
        markPackage(i, determine, state);
    if(determine)
        markFull(true);
}


function markPackage(pkg, determine, state) {
    if(determine) {
        packageCb[pkg-1][0].checked =
            packageState[pkg-1] = markPackageBooks(pkg, true);
    }
    else {
        packageCb[pkg-1][0].checked =
            packageState[pkg-1] = state;
        markPackageBooks(pkg, false, state);
    }
}


function markPackageBooks(pkg, determine, state) {
    var pkgBookMaps = gBooks[packageNames[pkg-1]];
    for(var i in pkgBookMaps) {
        /** @type {BookMap} */
        var bkMap = pkgBookMaps[i];
        if(determine) {
            if(!bkMap.state)
                return false;
        }
        else {
            bkMap.$cb[0].checked =
                bkMap.state = state;
        }
    }
    return !!pkgBookMaps.length;
}


function markBook(abbr, state) {
    var bkMap = getBookMap(abbr);
    if(bkMap) {
        bkMap.$cb[0].checked =
            bkMap.state = state;
    }
}


/**
 * @param {?string} abbr
 * @param {number?} id
 * @return {BookMap}
 */
function getBookMap(abbr, id) {
    for(var p in packageNames) {
        var pkgBk = gBooks[packageNames[p]];
        for(var i in pkgBk) {
            var bkMap = pkgBk[i];
            if(abbr && bkMap.abbr === abbr || bkMap.id === id)
                return bkMap;
        }
    }
}


function getAccessStr() {
    var num = '', abbr = '';
    for(var p=0; p<4; ++p) {
        if(packageState[p])
            num += (p+1) + '|';
        else {
            var pkgBk = gBooks[packageNames[p]];
            var pkgInc = '', pkgExc = '';
            for(var i in pkgBk) {
                var bkMap = pkgBk[i];
                if(bkMap.state)
                    pkgInc += bkMap.abbr + '|';
                else
                    pkgExc += '-' + bkMap.abbr + '|';
            }
            if(pkgInc) {
                if(pkgInc.length <= pkgExc.length)
                    abbr += pkgInc;
                else {
                    num += (p+1) + '|';
                    abbr += pkgExc;
                }
            }
        }
    }
    num += abbr;
    if(num)
        num = num.substring(0, num.length-1);
    return num;
}


function ok() {
    var access = getAccessStr();
    if(access === gAccess) {
        cancel();
        return;
    }
    $.ajax({
        url: '/app/admin/save-access',
        method: 'post',
        data: {
            email: gEmail,
            access: access
        },

        success: function(json)
        {
            if(util.javaError(json))
                return;
            cancel();
        },

        error: function(/*xhr, status*/)
        {
            util.ajaxError(/*xhr, status,*/ 'Hálózati hiba.', ok);
        }
    });
}


function cancel() {
    $dialog.hide();
}

$.extend(exports, {
    open: open,
    reset: reset
});
