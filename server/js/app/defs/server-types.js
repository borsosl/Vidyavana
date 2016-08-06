/**
 * @typedef {Object} DisplayBlock
 * @property {number} bookSegmentId
 * @property {number} tocId?
 * @property {number} last
 * @property {string} text
 * @property {string} shortRef?
 * @property {string} longRef?
 * @property {string} downtime?
 */

/**
 * @typedef {Object} HitResponse
 * @property {string} shortRef
 */

/**
 * @typedef {Object} SearchResponse
 * @property {number} id
 * @property {*} hitCount
 * @property {number} startHit
 * @property {number} endHit
 * @property {number} ordinal
 * @property {DisplayBlock} display
 * @property {Array.<HitResponse>} hits?
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
