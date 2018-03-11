
import dom from './dom';
import * as util from './util';


type ContentPageCallback<T extends ContentPageData> = (data: T, html: string) => void;

/**
 * Sends ajax request to get html content page.
 */
export function load<T extends ContentPageData>(url: string, data?: any, cb?: ContentPageCallback<T>) {
    $.ajax({
        url,
        dataType: 'json',
        data,

        success(json) {
            if(util.javaError(json))
                return;
            if(json.html || json.data)
                init(json, cb);
        },

        error(/*xhr, status*/) {
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

export function render(html: string) {
    dom.$formContent.html(html).show().scrollTop(0);
    util.resizeContent();
}
