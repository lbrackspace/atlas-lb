package org.openstack.atlas.adapter.zxtm.helper;

import org.openstack.atlas.adapter.exception.BadRequestException;
import org.openstack.atlas.common.converters.StringConverter;
import org.openstack.atlas.service.domain.entity.Node;

import java.util.*;

public class NodeHelper {

    public static String[][] buildNodeInfo(String ipAddress, Integer port) {
        String[][] node = new String[1][1];
        node[0][0] = IpHelper.createZeusIpString(ipAddress, port);
        return node;
    }

    public static String[][] getIpAddressesFromNodes(Collection<Node> nodes) {
        final String[][] ipAddressArray = new String[1][nodes.size()];
        int i = 0;
        for (Node node : nodes) {
            ipAddressArray[0][i] = IpHelper.createZeusIpString(node.getAddress(), node.getPort());
            i++;
        }
        return ipAddressArray;
    }

    public static List<Integer> getNodeIds(Collection<Node> nodes) {
        List<Integer> nodeIds = new ArrayList<Integer>();
        for (Node node : nodes) {
            nodeIds.add(node.getId());
        }
        Collections.sort(nodeIds);
        return nodeIds;
    }

    public static String getNodeIdsStr(Collection<Node> nodes) {
        List<Integer> ids = getNodeIds(nodes);
        return StringConverter.integersAsString(ids);
    }

    public static String[][] getMergedIpAddresses(Collection<Node> newNodes, String[] enabledNodes, String[] disabledNodes, String[] drainingNodes) throws BadRequestException {
        String[][] newIpAddressArray = getIpAddressesFromNodes(newNodes);
        final String[][] ipAddressArray = new String[1][newIpAddressArray[0].length + enabledNodes.length + disabledNodes.length + drainingNodes.length];
        Set<String> dummySet = new HashSet<String>();
        int i = 0;

        for (String ipAddress : newIpAddressArray[0]) {
            ipAddressArray[0][i] = ipAddress;
            if(!dummySet.add(ipAddress)) throw new BadRequestException("Duplicate nodes detected.");
            i++;
        }

        for (String ipAddress : enabledNodes) {
            ipAddressArray[0][i] = ipAddress;
            if(!dummySet.add(ipAddress)) throw new BadRequestException("Duplicate nodes detected.");
            i++;
        }

        for (String ipAddress : disabledNodes) {
            ipAddressArray[0][i] = ipAddress;
            if(!dummySet.add(ipAddress)) throw new BadRequestException("Duplicate nodes detected.");
            i++;
        }

        for (String ipAddress : drainingNodes) {
            ipAddressArray[0][i] = ipAddress;
            if(!dummySet.add(ipAddress)) throw new BadRequestException("Duplicate nodes detected.");
            i++;
        }

        return ipAddressArray;
    }
}
