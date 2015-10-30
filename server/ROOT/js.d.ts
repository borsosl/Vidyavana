/// <reference path="ts/lib/jquery.d.ts" />
interface TocTreeItem {
    id: any;
    title: any;
    partial: any;
    children: any;
    parentStart: any;
}
interface JqMouseWheelEvent extends JQueryEventObject {
    deltaX: any;
    deltaY: any;
}
declare var pg: {
    toc: TocTreeItem;
};
declare var book: any, measure: any, loadMode: {
    section: number;
    next: number;
    down: number;
    up: number;
}, $msg: any, nodeToUpdate: any, selSection: any, txtViewHgt: any, loadedHgt: any, scrollTop: any, textBoundary: any, $txt: any, emptyEndBlock: any;
declare var reduceAt: any;
declare function loadText(mode: any, px?: any): void;
declare function renderText(json: any, mode: any): void;
declare function down(px: any): void;
declare function up(px: any): void;
declare function scrollTextBy(ofs: number): void;
declare function Book(): void;
declare function Measure(): void;
declare function updateTocNode(): void;
declare function findTocNodeById(parent: any, id: any): any;
declare function getTocChildren(id: any, retryFn: any, cb: any): void;
declare function initSectionSelect(): void;
declare function updateSectionSelects(parent: any, level: any): void;
declare function gotoSection(): void;
declare function javaError(json: any): boolean;
declare function ajaxError(xhr: any, status: any, msg: any, retryFn: any): void;
declare function message(msg: any): void;
declare function throttle(init: any, delay: any, cb: any): () => void;
