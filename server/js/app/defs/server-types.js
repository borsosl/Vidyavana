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

/**
 * @typedef {Object} User
 * @property {number} id - registration datestamp.
 * @property {string} adminLevel - 'None'|'Full'|'BookRights'
 * @property {string} email
 * @property {string} name
 */

/**
 * @typedef {Object} BookPackageMap
 * @property {string|BookSpanArray} Sraddha
 * @property {string|BookSpanArray} SadhuSanga
 * @property {string|BookSpanArray} BhajanaKriya
 * @property {string|BookSpanArray} Ruci
 *
 * Properties are strings abbr|id|abbr|id|... from server,
 * converted to BookSpanArray on client.
 */

/**
 * @typedef {Object} UserListResponse
 * @property {Array.<User>} users
 * @property {BookPackageMap} books
 */
