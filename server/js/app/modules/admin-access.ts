
import util from './util';

type BookSpanArray = BookMap[];
type Package = string|BookSpanArray;

let $dialog: JQuery;
let checkboxesDrawn: boolean;
let gBooks: BookPackageMap;
let gEmail: string;
let gAccess: string;
const packageNames = ['Sraddha', 'SadhuSanga', 'BhajanaKriya', 'Ruci'];
let packageCb: JQuery[];
let packageState: boolean[];


/**
 * Properties are strings abbr|id|abbr|id|... from server,
 * converted to BookSpanArray on client.
 */
export interface BookPackageMap {
    Sraddha: Package;
    SadhuSanga: Package;
    BhajanaKriya: Package;
    Ruci: Package;
    [key: string]: Package;
}


/**
 * Fetch user book access, then fill dialog
 */
function open(email: string, books: BookPackageMap) {
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
    util.loading(true);
}


function reset() {
    checkboxesDrawn = false;
    packageCb = [];
    packageState = [];
    $dialog = $('#admin-access');
}


/**
 * Fill checkboxes with user access.
 */
function initCheckboxes(books: BookPackageMap, access: string) {
    drawCheckboxes(books);
    markAllPackages(false, false);
    gAccess = access;
    const parts = access.split('|');
    for(let p in parts) {
        const pt = parts[p];
        const pkg = parseInt(pt);
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
 */
function drawCheckboxes(books: BookPackageMap) {
    if(checkboxesDrawn)
        return;
    const $proto = $('.access-book', $dialog);
    for(let p in packageNames) {
        const pn = packageNames[p];
        packageCb.push($('#'+pn+'-cb'));
        packageState.push(false);
        const $block = $('#' + pn + '-cb-row');
        const parts = (books[pn] as string).split('|');
        books[pn] = [] as BookSpanArray;
        let i = 0;
        const len = parts.length;
        for(; i<len; i+=2)
        {
            let abbr = parts[i];
            if(!abbr)
                continue;
            const id = parseInt(parts[i + 1]);
            const $span = $proto.clone();
            $span.appendTo($block).data('id', id);
            $('.access-abbrev', $span).text(abbr);
            (books[pn] as BookSpanArray).push({
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


function cbChange(this: HTMLInputElement) {
    const state = this.checked;
    if(this.id) {
        if(this.id === 'full-access')
            markFull(false, state);
        else {
            const pkg = this.id.substr(0, this.id.length - 3);
            const pkgIx = packageNames.indexOf(pkg);
            packageState[pkgIx] = state;
            markPackageBooks(pkgIx+1, false, state);
            markFull(true);
        }
    } else {
        const bookId = $(this).parent().data('id');
        const bkMap = getBookMap(null, bookId);
        bkMap.state = state;
        markPackage(bkMap.pkg, true);
        markFull(true);
    }
}


function markFull(determine: boolean, state?: boolean) {
    if(determine) {
        (packageCb[4][0] as HTMLInputElement).checked =
            packageState[0] && packageState[1] && packageState[2] && packageState[3];
    }
    else {
        markAllPackages(false, state);
    }
}


function markAllPackages(determine: boolean, state?: boolean) {
    for(let i=1; i<=4; ++i)
        markPackage(i, determine, state);
    if(determine)
        markFull(true);
}


function markPackage(pkg: number, determine: boolean, state?: boolean) {
    if(determine) {
        (packageCb[pkg - 1][0] as HTMLInputElement).checked =
            packageState[pkg-1] = markPackageBooks(pkg, true);
    }
    else {
        (packageCb[pkg - 1][0] as HTMLInputElement).checked =
            packageState[pkg-1] = state;
        markPackageBooks(pkg, false, state);
    }
}


function markPackageBooks(pkg: number, determine: boolean, state?: boolean) {
    const pkgBookMaps = gBooks[packageNames[pkg - 1]] as BookSpanArray;
    for(let i in pkgBookMaps) {
        const bkMap = pkgBookMaps[i] as BookMap;
        if(determine) {
            if(!bkMap.state)
                return false;
        }
        else {
            (bkMap.$cb[0] as HTMLInputElement).checked =
                bkMap.state = state;
        }
    }
    return !!pkgBookMaps.length;
}


function markBook(abbr: string, state: boolean) {
    const bkMap = getBookMap(abbr);
    if(bkMap) {
        (bkMap.$cb[0] as HTMLInputElement).checked =
            bkMap.state = state;
    }
}


function getBookMap(abbr: string, id?: number): BookMap {
    for(let p in packageNames) {
        const pkgBk = gBooks[packageNames[p]] as BookSpanArray;
        for(let i in pkgBk) {
            const bkMap = pkgBk[i] as BookMap;
            if(abbr && bkMap.abbr === abbr || bkMap.id === id)
                return bkMap;
        }
    }
}


function getAccessStr() {
    let num = '', abbr = '';
    for(let p=0; p<4; ++p) {
        if(packageState[p])
            num += (p+1) + '|';
        else {
            const pkgBk = gBooks[packageNames[p]] as BookSpanArray;
            let pkgInc = '', pkgExc = '';
            for(let i in pkgBk) {
                const bkMap = pkgBk[i] as BookMap;
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
    const access = getAccessStr();
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
    util.loading(true);
}


function cancel() {
    $dialog.hide();
}

export default {
    open: open,
    reset: reset
};
