
import * as main from './main';
import * as util from './modules/util';
import * as html from './modules/html-content';
import * as access from './modules/admin-access';

let books: BookPackageMap;

interface User {
 id: number;            // registration datestamp.
 adminLevel: string;    // 'None'|'Full'|'BookRights'
 email: string;
 name: string;
 regToken: string;
}

interface UserListResponse extends ContentPageData {
 users: User[];
 books: BookPackageMap;
}


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
 */
function initListUsers(data: UserListResponse) {
    let $row = $('.admin-row');
    const users = data.users;
    books = data.books;
    let notConfirmed = 0;
    const len = users.length;
    for(let i=0; i<len; ++i) {
        const drow = users[i];
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
    const $e = $(this);
    const btnId = $e.data('id');
    const $row = $e.parents('.admin-row');
    switch(btnId) {
        case 1: modifyUser($row); break;
        case 2: bookRights($row); break;
        case 3: resendReg($row); break;
        case 4: deleteUser($row); break;
    }
}


function modifyUser($row: JQuery) {
    ajax('/app/admin/modify-user', 1, {
        email: $row.data('email'),
        name: $('.ur-name>input', $row).val(),
        admin: $('.ur-admin>select', $row).val()
    });
}


function bookRights($row: JQuery) {
    access.open($row.data('email'), books);
}


function resendReg($row: JQuery) {
    ajax('/app/admin/resend-reg', 2, {
        email: $row.data('email')
    });
}


function deleteUser($row: JQuery) {
    if(!confirm('Tuti?'))
        return;
    ajax('/app/admin/delete-user', 4, {
        email: $row.data('email')
    });
}


function ajax(url: string, type: number, data: any) {
    $.ajax({
        url,
        method: type===1 ? 'post' : 'get',
        dataType: 'json',
        data,

        success(json) {
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

        error(/*xhr, status*/) {
            util.ajaxError(/*xhr, status,*/ 'Hálózati hiba.', ajax.bind(null, url, data, type));
        }
    });
    util.loading(true);
}
