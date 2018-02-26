
type WordsItemIndexes = [string, number, number];
type WordsItemRex = [string, boolean, RegExp];
type WordsItem = WordsItemIndexes | WordsItemRex;
type IndexPair = [number, number];

let highlight: Highlight;

const LOWER = "áéíóöőúüűāīūḍḥḷḹṁṅṇñṛṝṣśṭ";
const UPPER = "ÁÉÍÓÖŐÚÜŰĀĪŪḌḤḶḸṀṄṆÑṚṜṢŚṬ";
const TRANS = "āīūḍḥḷḹṁṅṇñṛṝṣśṭ";
const PLAIN = "aiudhllmnnnrrsst";

interface CharMap {
    [key: string]: string
}

const lowerMap: CharMap = {};
for(let i=0; i<LOWER.length; ++i)
{
    const low = LOWER.charAt(i);
    lowerMap[low] = low;
    lowerMap[UPPER.charAt(i)] = low;
}

const plainMap: CharMap = {};
for(let i=0; i<TRANS.length; ++i)
    plainMap[TRANS.charAt(i)] = PLAIN.charAt(i);


class Highlight {
    run: (text: string, $rootElement: JQuery) => void;
    wordArr: (saa: WordsItemRex[]) => void;
    lowercase: (s: string) => string;
    highlightIndexes: (pContent: string) => any[];
    sought: (wordArr: WordsItemIndexes) => boolean;

    /**
     * @param queryStr - current search term
     */
    constructor(queryStr: string)
    {
        const paraRex = /<p.*?>([^]*?)<\/p>/gm;
        const htmlRex = /(.*?)(<.*?>|&.*;|$)/gm;
        const whiteRex = /(.*?)( |$)/gm;
        let $root: JQuery;

        const q = lowercase(queryStr);
        let soughtArrArr: WordsItem[] = words(q, true);
        for(const i in soughtArrArr)
        {
            const soughtArr = soughtArrArr[i] as WordsItemRex;
            let word = soughtArr[0];
            // if entered word has translit, take it literally
            soughtArr[1] = plain(word) !== word;
            if(hasWildcard(word))
            {
                word = word.replace(/\?/g, '.');
                word = word.replace(/\*/g, '.*');
                soughtArr[2] = new RegExp(word);
            }
            else
                soughtArr[2] = null;
        }


        function lowercase(s: string): string
        {
            let t = '';
            for(let i=0; i<s.length; ++i)
                t += lowerChar(s.charAt(i));
            return t;
        }


        function lowerChar(c: string): string
        {
            if(c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
            }
            else
            {
                const ch = lowerMap[c];
                if(ch)
                    c = ch;
                else
                    c = c.toLowerCase();
            }
            return c;
        }


        function plain(s: string): string
        {
            let t = '';
            for(let i=0; i<s.length; ++i)
            {
                let c = s.charAt(i);
                let ch = plainMap[c];
                if(ch)
                    c = ch;
                t += c;
            }
            return t;
        }


        function hasWildcard(s: string): boolean
        {
            const ixAst = s.indexOf('*');
            const ixQm = s.indexOf('?');
            return (ixAst > 1 && (ixQm == -1 || ixQm > 1)) || ixQm > 1 && ixAst == -1;
        }


        /**
         * @param s - lowercased string of possibly multiple words
         * @return array of words from string as [word, start, end], where indexes are relative to q
         * @param isQuery - tokenizing for the queried words
         */
        function words(s: string, isQuery: boolean): WordsItemIndexes[]
        {
            const res: WordsItemIndexes[] = [];
            let inWord = false;
            let word = '';
            let start = 0;
            let i, len;
            for(i = 0, len = s.length; i < len; ++i)
            {
                const c = s.charAt(i);
                const wc = isLowerWordChar(c) || isQuery && (c == '*' || c == '?');
                if(!inWord && wc)
                {
                    inWord = true;
                    start = i;
                }
                else if(inWord && !wc)
                {
                    inWord = false;
                    if(word)
                    {
                        res.push([word, start, i]);
                        word = '';
                    }
                }
                if(inWord)
                    word += c;
            }
            if(inWord)
                res.push([word, start, len]);
            return res;
        }


        function isLowerWordChar(c: string): boolean
        {
            return c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || !!lowerMap[c];
        }


        /**
         * Split html into paragraphs and invoke highlighting in each.
         * @param text - html chunk
         * @param $rootElement - container of modified para's
         */
        function run(text: string, $rootElement: JQuery): void
        {
            $root = $rootElement;
            paraRex.lastIndex = 0;
            nextPara(text);
        }


        function nextPara(text: string): void
        {
            const res = paraRex.exec(text);
            if(!res)
                return;
            setTimeout(paraText.bind(this, text, res[0], res[1]), 1);
        }


        function paraText(text: string, pText: string, pContent: string): void
        {
            const highlightIxPairArr = highlightIndexes(pContent);
            if(highlightIxPairArr.length > 0)
            {
                const res = /data-ix="(\d+)"/.exec(pText);
                if(res)
                {
                    const paraId = res[1];
                    const chunks = [];
                    let pos = 0;
                    for(const i in highlightIxPairArr)
                    {
                        const ixPair = highlightIxPairArr[i];
                        if(pos < ixPair[0])
                            chunks.push(pContent.substring(pos, ixPair[0]));
                        chunks.push('<span class="hilite">', pContent.substring(ixPair[0], ixPair[1]), '</span>');
                        pos = ixPair[1];
                    }
                    if(pos < pContent.length)
                        chunks.push(pContent.substring(pos));
                    $('p[data-ix="'+paraId+'"]', $root).html(chunks.join(''));
                }
            }
            nextPara(text);
        }


        function highlightIndexes(pContent: string): IndexPair[]
        {
            const ret: IndexPair[] = [];
            htmlRex.lastIndex = 0;
            while(true)
            {
                const htmlStart = htmlRex.lastIndex;
                let res = htmlRex.exec(pContent);
                if(!res || !res[0] && htmlRex.lastIndex >= pContent.length)
                    break;
                if(!res[0])
                    ++htmlRex.lastIndex;
                if(!res[1])
                    continue;
                const toNextTag = res[1];
                whiteRex.lastIndex = 0;
                while(true)
                {
                    const whiteStart = htmlStart + whiteRex.lastIndex;
                    res = whiteRex.exec(toNextTag);
                    if(!res || !res[0])
                        break;
                    if(!res[1])
                        continue;
                    const toNextWhite = res[1];
                    const q = lowercase(toNextWhite);
                    const whiteWordArrArr = words(q, false);
                    for(const i in whiteWordArrArr)
                    {
                        const whiteWordArr: WordsItemIndexes = whiteWordArrArr[i];
                        if(sought(whiteWordArr))
                            ret.push([whiteStart+whiteWordArr[1], whiteStart+whiteWordArr[2]]);
                    }
                }
            }
            return ret;
        }


        function sought(wordArr: WordsItemIndexes): boolean
        {
            for(const i in soughtArrArr)
            {
                const soughtArr = soughtArrArr[i] as WordsItemRex;
                const word = soughtArr[0];
                const hasTrans = soughtArr[1];
                const re = soughtArr[2];
                if(re)
                {
                    let paraWord = wordArr[0];
                    if(!hasTrans)
                        paraWord = plain(paraWord);
                    if(re.test(paraWord))
                        return true;
                }
                else if(hasTrans)
                {
                    if(word === wordArr[0])
                        return true;
                }
                else if(word === plain(wordArr[0]))
                    return true;
            }
            return false;
        }


        this.run = run;
        // for tests
        this.wordArr = function(saa: WordsItem[]) {soughtArrArr = saa;};
        this.lowercase = lowercase;
        // this.words = words;
        this.highlightIndexes = highlightIndexes;
        this.sought = sought;
    }
}


export function getInstance(): Highlight {
    return highlight;
}


export function init(queryStr: string): Highlight {
    return highlight = new Highlight(queryStr);
}
