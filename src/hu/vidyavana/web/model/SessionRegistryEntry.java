package hu.vidyavana.web.model;

import java.io.Serializable;
import java.util.Date;

public class SessionRegistryEntry implements Serializable {
    public String sid;
    public Date accessed;

    public SessionRegistryEntry(String sid) {
        this.sid = sid;
        accessed = new Date();
    }
}
