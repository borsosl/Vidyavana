/**
 * @typedef {Object} DisplayBlock
 * @property {number} bookId
 * @property {number} tocId?
 * @property {number} last
 * @property {string} text
 */

/**
 * @typedef {Object} TocTreeItem
 * @property {number} id - Ordinal in the whole TOC for each TOC node.
 * @property {?boolean} parentStart
 * @property {string} title
 * @property {?number} ordinal
 * @property {?TocTreeItem} parent
 * @property {?Array.<TocTreeItem>} children
 * @property {?boolean} partial
 */
