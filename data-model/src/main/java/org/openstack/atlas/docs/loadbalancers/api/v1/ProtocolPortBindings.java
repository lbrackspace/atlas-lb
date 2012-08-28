package org.openstack.atlas.docs.loadbalancers.api.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProtocolPortBindings {
    private static final Map <String,Integer> protocolPortMap;
    private static final List<String> keys;
    private static final Protocols protocols;
    private static final String[] keysAsArray;
    static {
        int i;
        protocolPortMap = new HashMap<String,Integer>();
        protocolPortMap.put("HTTP",80);
        protocolPortMap.put("HTTPS",443);
        protocolPortMap.put("FTP",21);
        protocolPortMap.put("IMAPv4",143);
        protocolPortMap.put("IMAPv2",143);
        protocolPortMap.put("IMAPv3",220);
        protocolPortMap.put("IMAPS",993);
        protocolPortMap.put("POP3",110);
        protocolPortMap.put("POP3S",995);
        protocolPortMap.put("SMTP",25);
        protocolPortMap.put("LDAP",389);
        protocolPortMap.put("HTTP",80);
        protocolPortMap.put("TCP",0);
        protocolPortMap.put("TCP_CLIENT_FIRST",0);
        protocolPortMap.put("LDAPS",636);
        protocolPortMap.put("DNS_TCP",53);
        protocolPortMap.put("DNS_UDP",53);
        protocolPortMap.put("UDP",0);
        protocolPortMap.put("UDP_STREAM",0);
        protocolPortMap.put("MYSQL",3306);
        protocolPortMap.put("SFTP",22);

        keys = new ArrayList<String>();
        protocols = new Protocols();

        for(Map.Entry<String,Integer> entry: protocolPortMap.entrySet()) {
            Protocol protocol = new Protocol();
            String key = entry.getKey();
            Integer port = entry.getValue();            
            keys.add(entry.getKey());
            protocol.setName(key);
            protocol.setPort(port);
            protocols.getProtocols().add(protocol);            
        }

        keysAsArray = new String[keys.size()];
        for(i=0;i<keys.size();i++) {
            keysAsArray[i] = keys.get(i);
        }
    }

    public static Protocols getDefaultPortBindings() {
        return protocols;
    }

    public static List<String> getKeys() {
        return keys;
    }

    public static String[] getKeysAsArray() {
        return keysAsArray;
    }

    public static int getPortByKey(String key) {
        for(Map.Entry<String,Integer> entry: protocolPortMap.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }

        //Cant find protocol....
        return 0;
    }

}
