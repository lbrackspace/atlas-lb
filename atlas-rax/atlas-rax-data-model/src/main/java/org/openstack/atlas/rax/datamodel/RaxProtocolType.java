package org.openstack.atlas.rax.datamodel;

import org.openstack.atlas.datamodel.CoreProtocolType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Primary
@Component
@Scope("request")
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
        protocolPortMap.put(FTP, 21);
        protocolPortMap.put(IMAPv2, 143);
        protocolPortMap.put(IMAPv3, 220);
        protocolPortMap.put(IMAPv4, 143);
        protocolPortMap.put(IMAPS, 993);
        protocolPortMap.put(LDAP, 389);
        protocolPortMap.put(LDAPS, 636);
        protocolPortMap.put(POP3, 110);
        protocolPortMap.put(POP3S, 995);
        protocolPortMap.put(SMTP, 25);
    }

}
