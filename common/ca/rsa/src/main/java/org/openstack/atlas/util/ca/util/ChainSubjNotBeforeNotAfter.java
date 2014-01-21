package org.openstack.atlas.util.ca.util;

import java.util.Date;

public class ChainSubjNotBeforeNotAfter {

    private String subject;
    private Date notBeforeMillis;
    private Date notAfterMillis;

    public ChainSubjNotBeforeNotAfter() {
    }

    public ChainSubjNotBeforeNotAfter(String subject, Date notBeforeMillis, Date notAfterMillis) {
        this.subject = subject;
        this.notBeforeMillis = notBeforeMillis;
        this.notAfterMillis = notAfterMillis;
    }

    @Override
    public String toString() {
        return "{" + "subject=" + subject
                + ", notBefore=" + notBeforeMillis
                + ", notAfter=" + notAfterMillis
                + "}";

    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getNotBeforeMillis() {
        return notBeforeMillis;
    }

    public void setNotBeforeMillis(Date notBeforeMillis) {
        this.notBeforeMillis = notBeforeMillis;
    }

    public Date getNotAfterMillis() {
        return notAfterMillis;
    }

    public void setNotAfterMillis(Date notAfterMillis) {
        this.notAfterMillis = notAfterMillis;
    }
}
