package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum LoadBalancerProtocol implements Serializable {
    HTTP,
    FTP,
    TCP,
    TCP_CLIENT_FIRST,
    IMAPv2,
    IMAPv3,
    IMAPv4,
    POP3,
    SMTP,
    LDAP,
    HTTPS,
    IMAPS,
    POP3S,
    LDAPS,
    DNS_UDP,
    DNS_TCP,
    UDP_STREAM,
    UDP,
    MYSQL,
    SFTP;

    private final static long serialVersionUID = 532512316L;
}
