package org.openstack.atlas.rax.datamodel;

import org.openstack.atlas.datamodel.CoreProtocolType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Primary
@Component
@Scope("singleton")
public class RaxProtocolType extends CoreProtocolType {
    public static final String FTP = "FTP";
    public static final String IMAPv2 = "IMAPv2";
    public static final String IMAPv3 = "IMAPv3";
    public static final String IMAPv4 = "IMAPv4";
    public static final String IMAPS = "IMAPS";
    public static final String LDAP = "LDAP";
    public static final String LDAPS = "LDAPS";
    public static final String POP3 = "POP3";
    public static final String POP3S = "POP3S";
    public static final String SMTP = "SMTP";

    static {
        add(FTP, 21);
        add(IMAPv2, 143);
        add(IMAPv3, 220);
        add(IMAPv4, 143);
        add(IMAPS, 993);
        add(LDAP, 389);
        add(LDAPS, 636);
        add(POP3, 110);
        add(POP3S, 995);
        add(SMTP, 25);
    }

}
