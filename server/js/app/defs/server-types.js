/**
 * @typedef {Object} DisplayBlock
 * @property {number} bookId
 * @property {number} tocId?
 * @property {number} last
 * @property {string} text
 * @property {string} shortRef?
 * @property {string} longRef?
 */

/**
 * @typedef {Object} HitResponse
 * @property {string} shortRef
 */

/**
 * @typedef {Object} SearchResponse
 * @property {number} id
 * @property {*} hitCount
 * @property {number} hit
 * @property {number} ordinal
 * @property {DisplayBlock} display
 * @property {Array.<HitResponse>} hits?
 * @property {number} startHit
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
