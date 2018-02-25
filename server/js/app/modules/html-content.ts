
import dom from './dom';
import util from './util';


export interface ContentPageData {
    skipRender?: boolean;
}

export interface ContentPageResult<T extends ContentPageData> {
    html?: string;
    data?: T;
}

interface ContentPageCallback<T extends ContentPageData> {
    // noinspection JSUnusedLocalSymbols
    (data: T, html: string): void;
}

/**
 * Sends ajax request to get html content page.
 */
function load<T extends ContentPageData>(url: string, data?: any, cb?: ContentPageCallback<T>)
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


function init<T extends ContentPageData>(res: ContentPageResult<T>, cb?: ContentPageCallback<T>) {
    if(!res.data || !res.data.skipRender)
        render(res.html);
    if(cb)
        cb.call(null, res.data, res.html);
}

function render(html: string) {
    dom.$formContent.html(html).show().scrollTop(0);
    util.resizeContent();
}


export default {
    load: load,
    render: render
};
