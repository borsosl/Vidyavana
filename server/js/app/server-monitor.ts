
function poll()
{
    $.ajax({
        url: '/app/util/monitor/'+pg.serviceTag,
        dataType: 'json',

        success: function(json)
        {
            if(json.text)
                $('#content').append(json.text);
            if(!json.finished)
                poll();
            else
                $('#content').append('Finished.');
        },

        error: function()
        {
            poll();
        }
    });
}

$(function()
{
    poll();
});
