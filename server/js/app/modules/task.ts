
import * as util from './util';

export function searchDialog() {
    if(util.dialog(util.dialog.id.search, true)) {
        const el = $('#searchInput')[0] as HTMLInputElement;
        el.focus();
        el.select();
    }
}

export function logout() {
    $.ajax({
        url: '/app/auth/logout',
        dataType: 'json',

        success(json) {
            if(util.javaError(json))
                return;
            if(!json.ok)
                failed();
            else
                document.location.href = '/app';
        },

        error(/*xhr, status*/) {
            failed();
        }
    });
    util.loading(true);

    function failed() {
        util.ajaxError('Sikertelen művelet.', logout);
    }
}
