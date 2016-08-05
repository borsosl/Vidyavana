
var dom = require('./dom');
var util = require('./util');


/**
 * @typedef {Object} ContentPageData
 * @property {?boolean} skipRender
 */

/**
 * @typedef {Object} ContentPageResult
 * @property {string} html
 * @property {ContentPageData} data
 * @property {string} [html]
 */

/**
 * @callback ContentPageCallback
 * @property {ContentPageData} data
 */

/**
 * Sends ajax request to get html content page.
 *
 * @param {string} url
 * @param {Object} [data]
 * @param {ContentPageCallback} [cb]
 */
function load(url, data, cb)
{
    $.ajax({
        url: url,
        dataType: 'json',
        data: data,

        success: function(json)
        {
            if(util.javaError(json))
                return;
            if(json.html || json.data)
                init(json, cb);
        },

        error: function(/*xhr, status*/)
        {
            util.ajaxError(/*xhr, status,*/ 'Hiba az oldal letöltésekor.', load.bind(null, url, data, cb));
        }
    });
    util.loading(true);
}


/**
 * @param {ContentPageResult} res
 * @param {ContentPageCallback} cb
 */
function init(res, cb) {
    if(!res.data || !res.data.skipRender)
        render(res.html);
    if(cb)
        cb.call(null, res.data, res.html);
}

/**
 * @param {string} html
 */
function render(html) {
    dom.$formContent.html(html).show().scrollTop(0);
    util.resizeContent();
}


$.extend(exports, {
    load: load,
    render: render
});
