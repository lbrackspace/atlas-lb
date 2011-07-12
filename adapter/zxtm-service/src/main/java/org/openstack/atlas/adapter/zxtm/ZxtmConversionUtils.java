package org.openstack.atlas.adapter.zxtm;

import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import com.zxtm.service.client.PoolLoadBalancingAlgorithm;
import com.zxtm.service.client.VirtualServerProtocol;

import java.io.Serializable;
import java.util.HashMap;

public class ZxtmConversionUtils {

	public static VirtualServerProtocol mapProtocol(LoadBalancerProtocol Protocol) {
		final HashMap<Enum<LoadBalancerProtocol>, Serializable> mapper = new HashMap<Enum<LoadBalancerProtocol>, Serializable>();

		mapper.put(LoadBalancerProtocol.HTTP, VirtualServerProtocol.http);
		mapper.put(LoadBalancerProtocol.HTTPS, VirtualServerProtocol.https);
		mapper.put(LoadBalancerProtocol.FTP, VirtualServerProtocol.ftp);
		mapper.put(LoadBalancerProtocol.IMAPv4, VirtualServerProtocol.imapv4);
        mapper.put(LoadBalancerProtocol.TCP, VirtualServerProtocol.server_first);
        mapper.put(LoadBalancerProtocol.IMAPv2, VirtualServerProtocol.imapv2);
        mapper.put(LoadBalancerProtocol.IMAPv3, VirtualServerProtocol.imapv3);
		mapper.put(LoadBalancerProtocol.IMAPS, VirtualServerProtocol.imaps);
		mapper.put(LoadBalancerProtocol.LDAP, VirtualServerProtocol.ldap);
		mapper.put(LoadBalancerProtocol.LDAPS, VirtualServerProtocol.ldaps);
		mapper.put(LoadBalancerProtocol.POP3, VirtualServerProtocol.pop3);
		mapper.put(LoadBalancerProtocol.POP3S, VirtualServerProtocol.pop3s);
		mapper.put(LoadBalancerProtocol.SMTP, VirtualServerProtocol.smtp);

		return (VirtualServerProtocol) mapper.get(Protocol);
	}
	
	public static PoolLoadBalancingAlgorithm mapAlgorithm(LoadBalancerAlgorithm algorithm) {
		final HashMap<Enum<LoadBalancerAlgorithm>, Serializable> mapper = new HashMap<Enum<LoadBalancerAlgorithm>, Serializable>();
		
		mapper.put(LoadBalancerAlgorithm.LEAST_CONNECTIONS, PoolLoadBalancingAlgorithm.connections);
		mapper.put(LoadBalancerAlgorithm.RANDOM, PoolLoadBalancingAlgorithm.random);
        mapper.put(LoadBalancerAlgorithm.ROUND_ROBIN, PoolLoadBalancingAlgorithm.roundrobin);
        mapper.put(LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS, PoolLoadBalancingAlgorithm.wconnections);
        mapper.put(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN, PoolLoadBalancingAlgorithm.wroundrobin);
		
		return (PoolLoadBalancingAlgorithm) mapper.get(algorithm);	
	}

}
