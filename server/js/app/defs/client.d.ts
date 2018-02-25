
declare var pg: {
    toc: TocTreeItem,
    maxTocId: number,
    justRegistered: boolean,
    downtime: string,
    serviceTag: string,
    ref: (tocId: number) => void,
    afterLogin: boolean
};

interface BookMap {
    pkg: number;
    abbr: string;
    id: number;
    $span: JQuery;      // ctnr of book CB
    $cb: JQuery;        // book CB
    state: boolean;     // of the book CB
}

declare var client: {
    browser: {
        ie: number
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

interface Window {
    md5(input: string): string;
}

declare function ok(test: boolean): void;
declare function test(name: string, fn: Function): void;
