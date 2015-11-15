/**
 * @typedef {Object} DisplayBlock
 * @property {number} book
 * @property {number} paraNum?
 * @property {number} first
 * @property {number} last
 * @property {number} show
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
