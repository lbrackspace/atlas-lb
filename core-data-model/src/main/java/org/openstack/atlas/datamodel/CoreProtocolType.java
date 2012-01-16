package org.openstack.atlas.datamodel;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("singleton")
public class CoreProtocolType implements ProtocolType {
    public static final String HTTP = "HTTP";
    public static final String HTTPS = "HTTPS";
    public static final String TCP = "TCP";
    private static final Map<String, Integer> protocolPortMap;

    static {
        protocolPortMap = new HashMap<String, Integer>();
        protocolPortMap.put(HTTP, 80);
        protocolPortMap.put(HTTPS, 443);
        protocolPortMap.put(TCP, 0);
    }

    public static Map<String, Integer> getProtocolPortMappings() {
        return protocolPortMap;
    }

    @Override
    public String[] toList() {
        return protocolPortMap.keySet().toArray(new String[protocolPortMap.keySet().size()]);
    }

    protected static void add(String protocol, Integer defaultPort) {
        protocolPortMap.put(protocol, defaultPort);
    }

}
