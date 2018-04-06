
import dom from './dom';
import * as util from './util';

interface MruWord {
    word: string;
    tstamp: number;
}

const maxItems = 100;
const sep = /\s+|[-"!|()\/~^]/g;
const storageKey = 'mru_';

let mru: MruWord[];
let $mru: JQuery;
let $children: JQuery;

let prevInput: string;
let prevEnd: number;
let listShown: boolean;
let matches: string[];
let count: number;
let selectedIndex: number;


export function init() {
    const storedMru = localStorage.getItem(storageKey + pg.userId);
    mru = storedMru ? JSON.parse(storedMru) : [];
    $mru = $('#mru');
}

function store() {
    localStorage.setItem(storageKey + pg.userId, JSON.stringify(mru));
}

export function addNewSearchWords(q: string) {
    const words = q.split(sep).filter(val => val.length);
    if(!words.length)
        return;
    const now = Date.now();
    for(let word of words) {
        word = word.toLowerCase();
        const comparator = compareWordToMruItem.bind(null, word);
        const ix = util.binarySearch(mru, comparator);
        if(ix >= 0)
            mru[ix].tstamp = now;
        else
            mru.splice(-ix-1, 0, {
                word,
                tstamp: now
            });
    }
    if(mru.length > maxItems) {
        mru.sort((a, b) => b.tstamp - a.tstamp);
        mru.length = maxItems;
        mru.sort((a, b) => util.compare(a.word, b.word));
    }
    store();
}

export function inputKeydown(this: HTMLInputElement, e: JQueryEventObject) {
    if(listShown) {
        const c = e.keyCode;
        if(c === 40 || c === 38) {          // down|up
            move(c === 40);
            e.preventDefault();
        } else if(c === 13) {                 // enter
            accept();
            e.stopPropagation();
        } else if(c === 27) {               // esc
            show(false);
            e.stopPropagation();
        } else if(c === 46 && e.ctrlKey)    // ctrl-del
            del();
    }
    setTimeout(() => afterKeypress(this), 0);
}

function afterKeypress(inp: HTMLInputElement) {
    const text = inp.value;
    if(text === prevInput) {
        if(inp.selectionEnd !== prevEnd)
            show(false);
        return;
    }
    prevInput = text;
    prevEnd = inp.selectionEnd;
    list();
}

function list() {
    const [word] = currentWord(prevInput, prevEnd);
    if(word.length)
        fill(word);
    else
        count = 0;
    show(!!count);
}

function fill(word: string) {
    matches = [];
    const comparator = compareWordToMruItemStart.bind(null, word);
    let ix = util.binarySearch(mru, comparator);
    if(ix < 0) {
        count = 0;
        return;
    }
    ix = util.findStartIndexBefore(mru, ix, comparator);
    do {
        matches.push(mru[ix++].word);
    } while(ix < mru.length && !comparator(mru[ix]));
    count = matches.length;

    const divArr = [];
    ix = 0;
    for(const item of matches)
        divArr.push(`<div data-id="${ix++}">${item}</div>`);
    $mru.html(divArr.join(''));
    $children = $mru.children('div');
    $children.click(accept);
    selectedIndex = -1;
    select(0);
}

function select(ix: number) {
    if(selectedIndex > -1)
        $children.eq(selectedIndex).removeClass('sel');
    const $el = $children.eq(ix);
    $el.addClass('sel');
    ($el[0] as HTMLDivElement).scrollIntoView(false);
    selectedIndex = ix;
}

export function show(show = true) {
    if(!show && !listShown)
        return;
    $mru.toggle(show);
    listShown = show;
}

export function move(down: boolean) {
    if(!listShown)
        return;
    let ix = down ? selectedIndex+1 : selectedIndex-1;
    if(ix < 0)
        ix = matches.length - 1;
    else if(ix >= matches.length)
        ix = 0;
    select(ix);
}

function accept() {
    const word = this ? matches[$(this).data('id')] : matches[selectedIndex];
    const [, start, end] = currentWord(prevInput, prevEnd);
    const text = prevInput.substr(0, start) + word + prevInput.substring(end);
    dom.$searchInput.val(text);
    const el = dom.$searchInput[0] as HTMLInputElement;
    const pos = start+word.length;
    el.selectionEnd = el.selectionStart = pos;
    show(false);
    prevInput = text;
    prevEnd = pos;
}

export function del() {
    const word = matches[selectedIndex];
    const comparator = compareWordToMruItem.bind(null, word.toLowerCase());
    let ix = util.binarySearch(mru, comparator);
    if(ix < 0)
        return;
    mru.splice(ix, 1);
    store();
    ix = selectedIndex;
    list();
    if(count) {
        if(ix >= matches.length)
            ix = matches.length-1;
        select(ix);
    }
}

function currentWord(text: string, pos: number): [string, number, number] {
    const rev = text.substr(0, pos).split('').reverse().join('');
    let ix = rev.search(sep);
    const start = ix < 0 ? 0 : pos-ix;
    ix = text.substr(pos).search(sep);
    const end = ix < 0 ? text.length : pos+ix;
    return [text.substring(start, pos).toLowerCase(), start, end];
}

function compareWordToMruItem(word: string, item: MruWord) {
    return word < item.word ? -1 : word > item.word ? 1 : 0;
}

function compareWordToMruItemStart(word: string, item: MruWord) {
    const start = item.word.substring(0, word.length);
    return word < start ? -1 : word > start ? 1 : 0;
}
