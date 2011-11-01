package org.openstack.atlas.adapter.zxtm.helper;

import com.zxtm.service.client.PoolLoadBalancingAlgorithm;
import com.zxtm.service.client.VirtualServerProtocol;
import org.openstack.atlas.datamodel.CoreAlgorithmType;
import org.openstack.atlas.datamodel.CoreProtocolType;

import java.io.Serializable;
import java.util.HashMap;

public class ZxtmConversionUtils {

    public static VirtualServerProtocol mapProtocol(String protocol) {
        final HashMap<String, Serializable> mapper = new HashMap<String, Serializable>();

        mapper.put(CoreProtocolType.HTTP, VirtualServerProtocol.http);
        mapper.put(CoreProtocolType.HTTPS, VirtualServerProtocol.https);
        mapper.put(CoreProtocolType.TCP, VirtualServerProtocol.server_first);

        return (VirtualServerProtocol) mapper.get(protocol);
    }

    public static PoolLoadBalancingAlgorithm mapAlgorithm(String algorithm) {
        final HashMap<String, Serializable> mapper = new HashMap<String, Serializable>();

        mapper.put(CoreAlgorithmType.ROUND_ROBIN, PoolLoadBalancingAlgorithm.roundrobin);
        mapper.put(CoreAlgorithmType.LEAST_CONNECTIONS, PoolLoadBalancingAlgorithm.connections);

        return (PoolLoadBalancingAlgorithm) mapper.get(algorithm);
    }

}
