
import dom from './dom';
import * as util from './util';
import * as html from './html-content';
import * as validate from './validate';

interface ProfileResult extends ContentPageData {
    email: string;
    name: string;
    access: string;
}

interface SaveProfileRequest {
    email: string;
    oldPassword: string;
    newPassword: string;
    name: string;
}

interface SaveProfileResponse {
    newEmailExists: boolean;
    emailChangeStarted: boolean;
    oldPasswordInvalid: boolean;
    passwordChanged: boolean;
    nameChanged: boolean;
}

let profile: ProfileResult;

export function loadPage() {
    html.load('/app/profile/page', null, initPage);
}

export function initPage(data: ProfileResult) {
    profile = data;
    $('#pf-email').val(data.email);
    $('#pf-name').val(data.name);
    $('#pf-access').text(data.access);

    $('#pf-save').click(saveClick);
    $('#pf-close').click(closeClick);
    if(client.browser.firefox)
        setTimeout(() => ($('#pf-password-old')[0] as HTMLInputElement).value = '', 200);
}

function saveClick() {
    message();
    let email = $('#pf-email').val().trim();
    let pwdOld = $('#pf-password-old').val();
    let pwd1 = $('#pf-password1').val();
    const pwd2 = $('#pf-password2').val();
    const name = $('#pf-name').val().trim();

    let isChanged = false;
    let pwdChanged = false;

    try {
        if(email !== profile.email) {
            isChanged = true;
            email = validate.email(email);
        }
        pwd1 = validate.password(pwd1, pwd2, true);
        if(pwd1.length) {
            pwdOld = validate.password(pwdOld);
            if(pwd1 !== pwdOld)
                isChanged = pwdChanged = true;
        }
        if(name !== profile.name)
            isChanged = true;
    } catch(e) {
        message((validate.errorCode as StringEnum)[e.message]);
        return;
    }

    if(!isChanged) {
        message('Nincs változás.');
        return;
    }

    const data: SaveProfileRequest = {
        email,
        oldPassword: null,
        newPassword: null,
        name
    };
    if(pwdChanged) {
        data.oldPassword = window.md5(pwdOld.trim());
        data.newPassword = window.md5(pwd1.trim());
    }
    const req = {
        profile: JSON.stringify(data)
    };

    ajax<SaveProfileResponse>('/app/profile/save', req, saveClick, profileSaved);
}

function profileSaved(resp: SaveProfileResponse) {
    const msgArr: string[] = [];
    if(resp.emailChangeStarted)
        msgArr.push('Az email változtatáshoz kiküldtük a megerősítő emailt a régi címre.');
    else if(resp.newEmailExists)
        msgArr.push('Az új email cím már használatban van.');
    if(resp.oldPasswordInvalid) {
        if(msgArr.length) msgArr.push('<br />');
        msgArr.push('A régi jelszó helytelen.');
    }
    const p = resp.passwordChanged, n = resp.nameChanged;
    if(p || n) {
        if(msgArr.length) msgArr.push('<br />');
        msgArr.push(`A ${p ? 'jelszó ' : ''}${p && n ? 'és a ' : ''}${n ? 'név' : ''} változása elmentve.`);
    }
    message(msgArr.join(''));
    if(p)
        emptyPasswordFields();
}

function emptyPasswordFields() {
    $('#pf-password-old').val('');
    $('#pf-password1').val('');
    $('#pf-password2').val('');
}

function closeClick() {
    emptyPasswordFields();
    dom.$formContent.hide();
}

function message(text?: string) {
    const $msg = $('#pf-msg');
    if(text)
        $msg.html(text).show();
    else
        $msg.hide();
}

function ajax<T>(url: string, data: any, retryFn: () => void, cb?: AjaxResultCallback<T>) {
    $.ajax({
        url,
        dataType: 'json',
        data,

        success(json: T) {
            if (!util.javaError(json) && cb)
                cb.call(null, json);
        },

        error(/*xhr, status*/) {
            util.ajaxError(/*xhr, status,*/ 'Hálózati hiba.', retryFn);
        }
    });
}
