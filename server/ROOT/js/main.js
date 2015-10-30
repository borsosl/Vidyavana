/// <reference path="lib/jquery.d.ts" />
var book, measure, loadMode = { section: 1, next: 2, down: 3, up: 4 }, $msg, nodeToUpdate, selSection, txtViewHgt, loadedHgt, scrollTop, textBoundary, $txt, emptyEndBlock;
var reduceAt;
function loadText(mode, px) {
    if (px === void 0) { px = null; }
    var m = loadMode;
    function loadModeUrl() {
        switch (mode) {
            case m.section:
                return '/app/txt/section/' + selSection;
            case m.next:
            case m.down:
                var last = book.last();
                if (last > book.paraNum())
                    return null;
                return '/app/txt/next/' + book.id() + '/' + last;
            case m.up:
                return '/app/txt/prev/' + book.id() + '/' + book.first();
        }
    }
    var url = loadModeUrl();
    if (!url)
        return;
    $.ajax({
        url: url,
        dataType: 'json',
        success: function (json) {
            if (!javaError(json)) {
                renderText(json, mode);
                // scroll
                if (mode == m.down)
                    down(px);
                else if (mode == m.up)
                    up(px);
            }
        },
        error: function (xhr, status) {
            ajaxError(xhr, status, 'Hiba a szöveg letöltésekor.', function () {
                loadText(mode);
            });
        }
    });
    if (mode === m.up) {
        var $first = $txt.children(':first');
        $first.data('pos', $first.position().top);
    }
}
function renderText(json, mode) {
    var m = loadMode;
    var init = mode == m.section;
    init = init || book.init(json, init);
    book.load(json);
    var h = json.text;
    var down = mode == m.down, up = mode == m.up;
    if (h) {
        if (init) {
            $txt.html(h);
            scrollTop = 0;
            emptyEndBlock = false;
        }
        else if (up) {
            $txt.prepend(h);
            reduceAt = 'bottom';
        }
        else {
            $txt.append(h);
            reduceAt = 'top';
        }
        measure.eachPara(up);
        // fill screen without scrolling
        if (init && loadedHgt < txtViewHgt)
            loadText(m.next);
    }
    else if (down || up) {
        textBoundary = true;
        if (down && !emptyEndBlock) {
            $('<div style="height: ' + txtViewHgt + 'px;" />').appendTo($txt);
            emptyEndBlock = true;
        }
    }
}
function down(px) {
    //console.log(''+loadedHgt+','+scrollTop+','+txtViewHgt+','+px);
    if (loadedHgt > scrollTop + txtViewHgt + px) {
        scrollTextBy(px);
        measure.reduce();
    }
    else if (textBoundary) {
        if (loadedHgt > scrollTop + px)
            scrollTextBy(px);
        textBoundary = false;
    }
    else {
        loadText(loadMode.down, px);
    }
}
function up(px) {
    //console.log(''+scrollTop+','+px);
    if (textBoundary) {
        scrollTop = 0;
        scrollTextBy(0);
        textBoundary = false;
    }
    else if (scrollTop >= px) {
        scrollTextBy(-px);
        measure.reduce();
    }
    else {
        loadText(loadMode.up, px);
    }
}
function scrollTextBy(ofs) {
    scrollTop += ofs;
    $txt.scrollTop(scrollTop);
}
function Book() {
    var id, first, last, show, paraNum;
    function init(json, force) {
        if (json.book == id && !force)
            return false;
        id = json.book;
        first = 50000;
        last = -1;
        if (json.paraNum)
            paraNum = json.paraNum;
        if (measure)
            measure.init();
        return true;
    }
    function load(json) {
        if (json.first < first)
            first = json.first;
        if (json.last > last)
            last = json.last;
        show = json.show;
    }
    $.extend(this, {
        init: init,
        load: load,
        id: function () {
            return id;
        },
        first: function (f) {
            return f === undefined ? first : (first = f);
        },
        last: function (l) {
            return l === undefined ? last : (last = l);
        },
        paraNum: function () {
            return paraNum;
        },
        show: function () {
            return show;
        }
    });
}
function Measure() {
    var $shadow = $('#shadowText'), lnHgt, charPerRow, hgtMap, $paras;
    init();
    function init() {
        $shadow.html('M');
        lnHgt = $shadow.height();
        $shadow.html('M<br/>M');
        lnHgt = $shadow.height() - lnHgt;
        var wid = $txt.width();
        charPerRow = Math.ceil(wid / lnHgt / 0.35);
        resize();
    }
    function resize() {
        hgtMap = {};
    }
    function eachPara(up) {
        loadedHgt = $txt[0].scrollHeight;
        if (up) {
            $paras = $txt.children();
            for (var i = 0; i < $paras.length; ++i) {
                var $p = $($paras[i]);
                var prevPos = $p.data('pos');
                if (prevPos === undefined)
                    continue;
                var newPos = $p.position().top;
                scrollTop += newPos - prevPos;
                return;
            }
            alert('No old pos');
        }
    }
    function reduce() {
        var KEEP_HGT = txtViewHgt * 3;
        if (loadedHgt < KEEP_HGT)
            return;
        if (!reduceAt)
            return;
        var atTop = reduceAt === 'top';
        reduceAt = null;
        var $ch = $txt.children(), len = $ch.length;
        if (atTop) {
            var till = -KEEP_HGT;
            var $p;
            var ofs, topOfs;
            for (var i = 0; i < len; ++i) {
                $p = $($ch[i]);
                ofs = $p.position().top;
                if (!topOfs)
                    topOfs = ofs;
                if (ofs >= till)
                    break;
            }
            if (i < len && i > 0) {
                $ch.slice(0, i).remove();
                scrollTextBy(topOfs - ofs);
                book.first(book.first() + i);
            }
        }
        else {
            till = txtViewHgt + KEEP_HGT;
            for (i = len - 1; i >= 0; --i) {
                $p = $($ch[i]);
                ofs = $p.position().top;
                if (ofs <= till)
                    break;
            }
            if (i < len - 1 && i >= 0) {
                $ch.slice(i + 1).remove();
                book.last(book.last() - (len - i - 1));
            }
        }
    }
    $.extend(this, {
        init: init,
        resize: resize,
        eachPara: eachPara,
        reduce: reduce,
        lnHgt: function () {
            return lnHgt;
        }
    });
}
//********** TOC data **********
function updateTocNode() {
    var o = nodeToUpdate;
    if (o.node.partial) {
        getTocChildren(o.id, updateTocNode, function (fullNode) {
            o.node.children = fullNode.children;
            o.node.partial = false;
            updateTocNode();
        });
        return;
    }
    nodeToUpdate = null;
    updateSectionSelects(o.node, o.level + 1);
}
function findTocNodeById(parent, id) {
    var ch = parent.children;
    if (ch == null)
        return parent;
    var len = ch.length;
    for (var i = 0; i < len; ++i) {
        var ti = ch[i];
        if (id > ti.id)
            continue;
        if (id < ti.id && i > 0)
            return findTocNodeById(ch[i - 1], id);
        return ti;
    }
    return findTocNodeById(ch[len - 1], id);
}
function getTocChildren(id, retryFn, cb) {
    $.ajax({
        url: '/app/toc/get/' + id,
        dataType: 'json',
        success: function (json) {
            if (!javaError(json))
                cb.call(this, json);
        },
        error: function (xhr, status) {
            ajaxError(xhr, status, 'Hiba a tartalomjegyzék ág letöltésekor.', retryFn);
        }
    });
}
// ********** Section select **********
function initSectionSelect() {
    updateSectionSelects(pg.toc, 1);
    // event handlers
    $('.sectionSelect').change(function () {
        var level = parseInt(this.id.substr(4));
        var id = parseInt(this.value);
        var node = findTocNodeById(pg.toc, id);
        nodeToUpdate = {
            sel: this,
            level: level,
            id: id,
            node: node
        };
        updateTocNode();
    });
    $('#sectionGo').click(gotoSection);
}
function updateSectionSelects(parent, level) {
    while (parent) {
        var ch = parent.children;
        selSection = parent.id;
        if (parent.parentStart)
            --selSection;
        if (!ch)
            break;
        var $e = $('#sect' + (level++));
        var opt = [];
        for (var i in ch) {
            if (!ch.hasOwnProperty(i))
                continue;
            var it = ch[i];
            opt.push('<option value="', it.id, '">', it.title, '</option>');
        }
        $e.html(opt.join(''));
        $e.css('visibility', 'visible');
        parent = ch[0];
    }
    while (level <= 9) {
        $('#sect' + (level++))[0].style.visibility = 'hidden';
    }
}
function gotoSection() {
    $('#sectionPop').hide();
    loadText(loadMode.section);
}
//********** Util **********
function javaError(json) {
    if (json.error)
        message(json.error);
    return !!json.error;
}
function ajaxError(xhr, status, msg, retryFn) {
    message(msg + '...<br><a href="#" id="retry">Ismétlés</a>&nbsp;&nbsp;<a href="#" id="cancelMsg">Mégse</a>');
    $('#retry', $msg).click(function () {
        retryFn.call(this);
    });
    $('#cancelMsg', $msg).click(function () {
        $msg.hide();
    });
}
function message(msg) {
    if (!$msg)
        $msg = $('#message');
    $msg.html(msg);
    var $win = $(window);
    $msg.css({
        'top': Math.floor(($win.height() - $msg.height() - 20) / 2) + 'px',
        'left': Math.floor(($win.width() - $msg.width() - 20) / 2) + 'px',
        'display': 'block'
    });
}
function throttle(init, delay, cb) {
    var timer;
    if (init)
        cb.call();
    return function () {
        clearTimeout(timer);
        timer = setTimeout(cb, delay);
    };
}
$(function () {
    $txt = $('#text');
    book = new Book();
    measure = new Measure();
    initSectionSelect();
    $('#sectionPop').show();
    $('#sectionLnk').click(function () {
        $('#sectionPop').toggle();
    });
    $(window).keydown(function (e) {
        if (e.keyCode == 40)
            down(measure.lnHgt());
        else if (e.keyCode == 38)
            up(measure.lnHgt());
        else if (e.keyCode == 34)
            down(txtViewHgt);
        else if (e.keyCode == 33)
            up(txtViewHgt);
    });
    $(window).on('mousewheel', function (e) {
        if (e.deltaY > 0)
            up(measure.lnHgt());
        else if (e.deltaY < 0)
            down(measure.lnHgt());
    });
    window.onresize = throttle(true, 100, function () {
        var $m = $('#measure');
        var winHgt = $m.height();
        var winWid = $m.width();
        var headHgt = $('#header').height();
        $txt[0].style.top = headHgt + 'px';
        $txt.innerHeight(winHgt - headHgt);
        $txt.innerWidth(winWid);
        txtViewHgt = $txt.height();
        measure.resize();
        measure.eachPara();
    });
});
