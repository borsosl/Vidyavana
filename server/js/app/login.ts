
import * as validate from './modules/validate';

let activeTab = 0;
let activeForgottenLink = false;
let $msg: JQuery;

function setActiveTab() {
    $('.loginitem').toggle(activeTab == 0 && !activeForgottenLink);
    $('.regitem').toggle(activeTab == 1);
    $('.forgotitem').toggle(activeTab == 0 && activeForgottenLink);
    $('#pass-div').toggle(!activeForgottenLink);

    setTimeout(function() {
        const $e = $('#email');
        if(!$e.val() || activeForgottenLink)
            $e[0].focus();
        else
            $('#loginBtn')[0].focus();
    }, 100);
}


function switchTabs() {
    if(this.id.charAt(3) !== '' + activeTab) {
        $('.tab').toggleClass('active').toggleClass('inactive');
        activeTab = 1 - activeTab;
        setActiveTab();
    }
}


function login() {
    try {
        const email = validate.email($('#email').val());
        let pwd = validate.password($('#password').val());
        if(pwd.length === 33 && pwd.charAt(0) === '@')
            pwd = pwd.substring(1);
        else
            pwd = window.md5(pwd);

        $.ajax({
            url: '/app/auth/authenticate',
            method: 'POST',
            dataType: 'json',
            data: {
                email: email,
                password: pwd
            },

            success: function(json)
            {
                if(json.message)
                    message(json.message);
                else if(json.fail)
                    message('Sikertelen belépés');
                else if(json.error)
                    errorMsg(json.error);
                else
                    post(location.href, {
                        username: email,
                        password: '@' + pwd
                    });
            },

            error: function(/*xhr, status*/)
            {
                message('Hálózati hiba');
            }
        });
    } catch(e) {
        handleValidationError(e);
    }
}


function register() {
    try {
        const email = validate.email($('#email').val());
        let pwd = validate.password($('#password').val(), $('#password2').val());
        pwd = window.md5(pwd);

        $.ajax({
            url: '/app/auth/register',
            method: 'POST',
            dataType: 'json',
            data: {
                email: email,
                password: pwd,
                name: $('#name').val()
            },

            success: function(json)
            {
                if(json.message)
                    message(json.message);
                else if(json.error)
                    errorMsg(json.error);
                else
                    post('/app', {
                        username: email,
                        password: '@' + pwd
                    });
            },

            error: function(/*xhr, status*/)
            {
                message('Hálózati hiba');
            }
        });
    } catch(e) {
        handleValidationError(e);
    }
}

function forgottenPassword(e: JQueryEventObject) {
    activeForgottenLink = true;
    setActiveTab();
    e.preventDefault();
}

function forgotOk() {
    try {
        const email = validate.email($('#email').val());

        $.ajax({
            url: '/app/auth/forgotten',
            dataType: 'json',
            data: {
                email: email
            },

            success: function(json)
            {
                if(json.message)
                    message(json.message);
                else if(json.error)
                    errorMsg(json.error);
                else {
                    message('Az emailt kiküldtük.<br />(Spam mappát is figyeld.)');
                    forgotCancel();
                }
            },

            error: function(/*xhr, status*/)
            {
                message('Hálózati hiba');
            }
        });
    } catch(e) {
        handleValidationError(e);
    }
}

function forgotCancel() {
    activeForgottenLink = false;
    setActiveTab();
}

function handleValidationError(e: Error) {
    if(!e || !e.message)
        return;
    let msg = validate.errorCode[e.message];
    if(msg)
        message(msg);
}

function message(s: string) {
    $msg.html(s).show();
}


function errorMsg(code: number) {
    message('Hiba történt. <a href="mailto:dev@pandit.hu?subject=Hibajelentés ('+
        code+')">Beszámolok róla</a>');
}


function keydown(e: KeyboardEvent) {
    const c = e.keyCode;
    $msg.hide();
    const tag = document.activeElement.tagName.toLowerCase();
    if(c === 13 && tag === 'input') {
        if(activeTab === 0)
            login();
        else
            register();
    }
}


$(function() {
    setActiveTab();
    $('.tab').click(switchTabs);
    $('#loginBtn').click(login);
    $('#regBtn').click(register);
    $('#forgot-link').click(forgottenPassword);
    $('#forgot-ok').click(forgotOk);
    $('#forgot-cancel').click(forgotCancel);
    $(window).keydown(keydown);
    $msg = $('#loginMsg');
});


// http://stackoverflow.com/questions/2382329/how-can-i-get-browser-to-prompt-to-save-password
function post(path: string, params: any, method?: string)
{
    method = method || "post"; // Set method to post by default if not specified.

    // The rest of this code assumes you are not using a library.
    // It can be made less wordy if you use one.

    const form = document.createElement("form");
    form.id = "dynamicform" + Math.random();
    form.setAttribute("method", method);
    form.setAttribute("action", path);
    form.setAttribute("style", "display: none");
    // Internet Explorer needs this
    form.setAttribute("onsubmit", "window.external.AutoCompleteSaveForm(document.getElementById('" + form.id + "'))");

    for (const key in params)
    {
        if (params.hasOwnProperty(key))
        {
            const hiddenField = document.createElement("input");
            // Internet Explorer needs a "password"-field to show the store-password-dialog
            hiddenField.setAttribute("type", key == "password" ? "password" : "text");
            hiddenField.setAttribute("name", key);
            hiddenField.setAttribute("value", params[key]);

            form.appendChild(hiddenField);
        }
    }

    const submitButton = document.createElement("input");
    submitButton.setAttribute("type", "submit");

    form.appendChild(submitButton);

    document.body.appendChild(form);

    //form.submit(); does not work on Internet Explorer
    submitButton.click(); // "click" on submit-button needed for Internet Explorer
}
