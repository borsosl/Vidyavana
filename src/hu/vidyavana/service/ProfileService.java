package hu.vidyavana.service;

import com.google.gson.GsonBuilder;
import hu.vidyavana.db.dao.EmailChangeDao;
import hu.vidyavana.db.dao.UserDao;
import hu.vidyavana.db.model.BookAccess;
import hu.vidyavana.db.model.EmailChange;
import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Encrypt;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.MailTask;
import hu.vidyavana.web.RequestInfo;

import java.util.Date;

public class ProfileService {

    public static class ProfileResult {
        public String email;
        public String name;
        public String access;
    }

    public static class SaveProfileRequest {
        public String email;
        public String oldPassword;
        public String newPassword;
        public String name;
    }

    public static class SaveProfileResponse {
        public boolean newEmailExists;
        public boolean emailChangeStarted;
        public boolean oldPasswordInvalid;
        public boolean passwordChanged;
        public boolean nameChanged;
    }

    private RequestInfo ri;

    public ProfileService(RequestInfo ri) {
        this.ri = ri;
    }

    public void page()
    {
        try
        {
            String html = ri.getTemplate("/dialog/profile.html");
            ProfileResult data = initPage();
            ri.renderAjaxTemplateString(html, data);
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private ProfileResult initPage() {
        ProfileResult res = new ProfileResult();
        res.email = ri.user.email;
        res.name = ri.user.name;
        res.access = BookAccess.displayedAccessString(ri.user.accessStr);
        return res;
    }

    public void save() {
        String json = ri.req.getParameter("profile");
        SaveProfileRequest req = new GsonBuilder()
                .create()
                .fromJson(json, SaveProfileRequest.class);
        SaveProfileResponse resp = new SaveProfileResponse();

        if(!req.email.equalsIgnoreCase(ri.user.email)) {
            User newMailUser = UserDao.findUserByEmail(req.email);
            if(newMailUser != null)
                resp.newEmailExists = true;
            else {
                startEmailChange(req.email);
                resp.emailChangeStarted = true;
            }
        }
        if(req.newPassword != null) {
            if(!req.oldPassword.equals(ri.user.password)) {
                resp.oldPasswordInvalid = true;
            } else {
                ri.user.password = req.newPassword;
                resp.passwordChanged = true;
            }
        }
        if(!req.name.equals(ri.user.name)) {
            ri.user.name = req.name;
            resp.nameChanged = true;
        }
        if(resp.passwordChanged || resp.nameChanged) {
            UserDao.updateUser(ri.user);
        }
        ri.ajaxResult = resp;
    }

    private void startEmailChange(String email) {
        EmailChangeDao.delete(ri.user.id);
        EmailChange ec = new EmailChange();
        ec.userId = ri.user.id;
        ec.oldEmail = ri.user.email;
        ec.newEmail = email;
        ec.token = Encrypt.md5(""+(new Date().getTime()));
        EmailChangeDao.insert(ec);

        if(Globals.serverEnv)
            Globals.mailExecutor.submit(new MailTask("change-email", ri.user.email, ec.token));
    }

    public void verifyEmailChange(RequestInfo ri) throws Exception {
        EmailChange ec = deleteEmailChangeRecord(ri);
        User user = UserDao.findUserByEmail(ec.oldEmail);
        user.email = ec.newEmail;
        user.regToken = Encrypt.md5(""+(new Date().getTime()));
        UserDao.updateUser(user);
        if(Globals.serverEnv)
            Globals.mailExecutor.submit(new MailTask("register", user.email, user.regToken));
        ri.resp.getWriter().write("Az e-mail cím változtatását megerősítetted,<br />" +
                "az új e-mail címre kiküldtük a regisztrációs levelet!<br />" +
                "Annak megerősítése előtt nem tudsz belépni.");
    }

    public void declineEmailChange(RequestInfo ri) throws Exception {
        deleteEmailChangeRecord(ri);
        ri.resp.getWriter().write("Az e-mail cím változtatását érvénytelenítettük.");
    }

    private EmailChange deleteEmailChangeRecord(RequestInfo ri) {
        String token = ri.req.getParameter("token").trim();
        EmailChange ec = EmailChangeDao.findByToken(token);
        EmailChangeDao.delete(ec.userId);
        return ec;
    }

}
