
var main = require('./main');
var dom = require('./modules/dom');
var util = require('./modules/util');
var html = require('./modules/html-content');

$(function() {
    main.init();
    $('#users-link').click(function() {
        util.toggleMenu(true);
        listUsers();
    });
});


function listUsers() {
    html.load('/app/admin/list-users', null, initListUsers);
}


function initListUsers(data) {
    /** @type {JQuery} */
    var $row = $('.admin-row');
    for(var i=0, len=data.length; i<len; ++i) {
        /** @type {User} */
        var drow = data[i];
        $row.data('email', drow.email);
        // fill row html
        $row.children().each(function(ix, el) {
            switch(ix) {
                case 0: $(el).text(drow.email); break;
                case 1: $('input', el).val(drow.name); break;
                case 2: $('select', el).val(drow.adminLevel); break;
            }
        });
        // clone $row
        if(i < len-1) {
            $row = $row.clone().insertAfter($row);
        }
    }
    // event listeners
    $('button', '.admin-form').click(listUsersButton);
}


function listUsersButton() {
    var $e = $(this);
    var btnId = $e.data('id');
    var $row = $e.parents('.admin-row');
    switch(btnId) {
        case 1: modifyUser($row); break;
        case 2: bookRights($row); break;
        case 3: resendReg($row); break;
        case 4: deleteUser($row); break;
    }
}


function modifyUser($row) {
    ajax('/app/admin/modify-user', 1, {
        email: $row.data('email'),
        name: $('.ur-name>input', $row).val(),
        admin: $('.ur-admin>select', $row).val()
    });
}


function bookRights($row) {

}


function resendReg($row) {

}


function deleteUser($row) {
    if(!confirm('Tuti?'))
        return;
    ajax('/app/admin/delete-user', 4, {
        email: $row.data('email')
    });
}


function ajax(url, type, data) {
    $.ajax({
        url: url,
        method: type==1 ? 'post' : 'get',
        dataType: 'json',
        data: data,

        success: function(json)
        {
            if(util.javaError(json))
                return;
            if(type === 4)
                listUsers();
            else if(json.ok)
                util.message('Siker', true);
        },

        error: function(/*xhr, status*/)
        {
            util.ajaxError(/*xhr, status,*/ 'Hálózati hiba.', ajax.bind(null, url, data, type));
        }
    });
}