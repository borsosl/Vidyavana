
var util = require('./util');


function searchDialog() {
    util.dialog(0, true);
    var el = $('#searchInput')[0];
    el.focus();
    el.select();
}


function logout() {
    $.ajax({
        url: '/app/auth/logout',
        dataType: 'json',

        success: function(json) {
            if(util.javaError(json))
                return;
            if(!json.ok)
                failed();
            else
                document.location = '/app';
        },

        error: function(/*xhr, status*/) {
            failed();
        }
    });

    function failed() {
        util.ajaxError('Sikertelen művelet.', logout);
    }
}


$.extend(exports, {
    searchDialog: searchDialog,
    logout: logout
});
