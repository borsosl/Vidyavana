
var main = require('./main');
var dom = require('./modules/dom');
var util = require('./modules/util');
var html = require('./modules/html-content');
var access = require('./modules/admin-access');

var books;

/**
 * @typedef {Object} User
 * @property {number} id - registration datestamp.
 * @property {string} adminLevel - 'None'|'Full'|'BookRights'
 * @property {string} email
 * @property {string} name
 * @property {string} regToken
 */

/**
 * @typedef {Object} UserListResponse
 * @property {Array.<User>} users
 * @property {BookPackageMap} books
 */


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


/**
 * Create user list admin form.
 * @param {UserListResponse} data
 */
function initListUsers(data) {
    /** @type {JQuery} */
    var $row = $('.admin-row');
    var users = data.users;
    books = data.books;
    var notConfirmed = 0;
    for(var i=0, len=users.length; i<len; ++i) {
        /** @type {User} */
        var drow = users[i];
        if(drow.regToken)
            ++notConfirmed;
        $row.data('email', drow.email);
        // fill row html
        $row.children().each(function(ix, el) {
            switch(ix) {
                case 0: $(el).text(drow.email); break;
                case 1: $(el).text(drow.regToken ? '!' : ''); break;
                case 2: $('input', el).val(drow.name); break;
                case 3: $('select', el).val(drow.adminLevel); break;
            }
        });
        // clone $row
        if(i < len-1) {
            $row = $row.clone().insertAfter($row);
        }
    }
    // count
    $('#user-count').text(''+len);
    $('#unconf-count').text(''+notConfirmed);
    // event listeners
    $('button', '.admin-form').click(listUsersButton);
    access.reset();
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
    access.open($row.data('email'), books);
}


function resendReg($row) {
    alert('Nincs kész.');
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
            if(json.fail) {
                util.message('Nincs jogosultság, vagy egyéb hiba.', true);
                return;
            }
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
    util.loading(true);
}
