
var dom = require('./dom');
var util = require('./util');

/**
 * Sends ajax request to get html content page.
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
            if(json.html)
                render(json, cb);
        },

        error: function(/*xhr, status*/)
        {
            util.ajaxError(/*xhr, status,*/ 'Hiba az oldal letöltésekor.', load.bind(null, url, data, cb));
        }
    });
    util.loading(true);
}


function render(json, cb) {
    $('#form-content').html(json.html).show().scrollTop(0);
    util.resizeContent();
    if(cb)
        cb.call(null, json.data);
}


$.extend(exports, {
    load: load
});
