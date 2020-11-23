package org.openstack.atlas.adapter.vtm;

import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;

import java.util.HashMap;


public class VTMConversionUtils {

	public static String mapProtocol(LoadBalancerProtocol Protocol) {
		final HashMap<Enum<LoadBalancerProtocol>, String> mapper = new HashMap<>();

		mapper.put(LoadBalancerProtocol.HTTP, "http");
		mapper.put(LoadBalancerProtocol.HTTPS, "https");
		mapper.put(LoadBalancerProtocol.FTP, "ftp");
		mapper.put(LoadBalancerProtocol.IMAPv4, "imapv4");
        mapper.put(LoadBalancerProtocol.TCP, "server_first");
        mapper.put(LoadBalancerProtocol.TCP_CLIENT_FIRST, "client_first");
        mapper.put(LoadBalancerProtocol.TCP_STREAM, "stream");
        mapper.put(LoadBalancerProtocol.IMAPv2, "imapv2");
        mapper.put(LoadBalancerProtocol.IMAPv3, "imapv3");
        mapper.put(LoadBalancerProtocol.IMAPS, "imaps");
        mapper.put(LoadBalancerProtocol.LDAP, "ldap");
        mapper.put(LoadBalancerProtocol.LDAPS, "ldaps");
        mapper.put(LoadBalancerProtocol.POP3, "pop3");
        mapper.put(LoadBalancerProtocol.POP3S, "pop3s");
        mapper.put(LoadBalancerProtocol.SMTP, "smtp");
        mapper.put(LoadBalancerProtocol.DNS_TCP, "dns_tcp");
        mapper.put(LoadBalancerProtocol.DNS_UDP, "dns");
        mapper.put(LoadBalancerProtocol.UDP, "udp");
        mapper.put(LoadBalancerProtocol.UDP_STREAM, "udpstreaming");
        mapper.put(LoadBalancerProtocol.MYSQL, "server_first");
        mapper.put(LoadBalancerProtocol.SFTP, "server_first");

		return mapper.get(Protocol);
	}

}
