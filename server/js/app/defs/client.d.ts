
declare var pg: {
    toc: TocTreeItem,
    maxTocId: number,
    userId: number,
    justRegistered: boolean,
    downtime: string,
    serviceTag: string,
    ref: (tocId: number) => void,
    afterLogin: boolean
};

interface StringEnum {
    [key: string]: string;
}

interface BookMap {
    pkg: number;
    abbr: string;
    id: number;
    $span: JQuery;      // ctnr of book CB
    $cb: JQuery;        // book CB
    state: boolean;     // of the book CB
}

interface SearchSectionNode {
    op: string;
    abbrev: string;
    tocId: number;
    nextSiblingTocId: number;
}

interface SearchSection {
    nodeFilter: string;
    displayText: string;
    base: string;
    nodes: SearchSectionNode[];
    changed: boolean;
}

declare var client: {
    browser: {
        ie: number,
        firefox: number
    },
    system: {
        mac: boolean,
        android: boolean,
        ios: boolean,
        iphone: boolean,
        ipad: boolean,
        winMobile: boolean
    }
};

// noinspection JSUnusedGlobalSymbols
interface Window {
    // noinspection JSUnusedLocalSymbols
    md5(input: string): string;
}
