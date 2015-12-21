
var main = require('./main');
var dom = require('./modules/dom');
var util = require('./modules/util');
var html = require('./modules/html-content');

$(function() {
    main.init();
    $('#users-link').click(function() {
        util.toggleMenu(true);
        html.load('/app/admin/list-users', null, initListUsers);
    });
});


function initListUsers(data) {
    var $row = $('.admin-row');
    for(var i=0, len=data.length; i<len; ++i) {
        /** @type {User} */
        var drow = data[i];
        $row[0].id = drow.id;
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
}
