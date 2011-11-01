package org.openstack.atlas.adapter.zxtm.helper;

import org.openstack.atlas.adapter.exception.BadRequestException;
import org.openstack.atlas.common.converters.StringConverter;
import org.openstack.atlas.service.domain.entity.Node;

import java.util.*;

public class NodeHelper {

    public static String[][] buildNodeInfo(String ipAddress, Integer port) throws BadRequestException {
        String[][] node = new String[1][1];
        node[0][0] = IpHelper.createZeusIpString(ipAddress, port);
        return node;
    }

    public static List<String> getIpAddressesFromNodes(Collection<Node> nodes) throws BadRequestException {
        final List<String> ipAddressArray = new ArrayList<String>();
        for (Node node : nodes) {
            ipAddressArray.add(IpHelper.createZeusIpString(node.getAddress(), node.getPort()));
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

    public static String[] getMergedIpAddresses(String[] enabledNodes, String[] disabledNodes, String[] drainingNodes) throws BadRequestException {
        final String[] ipAddressArray = new String[enabledNodes.length + disabledNodes.length + drainingNodes.length];
        Set<String> dummySet = new HashSet<String>();
        int i = 0;

        for (String ipAddress : enabledNodes) {
            ipAddressArray[i] = ipAddress;
            if (!dummySet.add(ipAddress)) throw new BadRequestException("Duplicate nodes detected.");
            i++;
        }

        for (String ipAddress : disabledNodes) {
            ipAddressArray[i] = ipAddress;
            if (!dummySet.add(ipAddress)) throw new BadRequestException("Duplicate nodes detected.");
            i++;
        }

        for (String ipAddress : drainingNodes) {
            ipAddressArray[i] = ipAddress;
            if (!dummySet.add(ipAddress)) throw new BadRequestException("Duplicate nodes detected.");
            i++;
        }

        return ipAddressArray;
    }

    public static List<String> getMergedIpAddresses(Collection<Node> nodes, String[] enabledNodes, String[] disabledNodes, String[] drainingNodes) throws BadRequestException {
        List<String> newIpAddressArray = getIpAddressesFromNodes(nodes);
        String[] existingAddressArray = getMergedIpAddresses(enabledNodes, disabledNodes, drainingNodes);

        List<String> ipAddressArray = new ArrayList<String>();
        Set<String> dummySet = new HashSet<String>();

        for (String ipAddress : newIpAddressArray) {
            ipAddressArray.add(ipAddress);
            if (!dummySet.add(ipAddress)) throw new BadRequestException("Duplicate nodes detected.");
        }

        for (String ipAddress : existingAddressArray) {
            ipAddressArray.add(ipAddress);
            if (!dummySet.add(ipAddress)) throw new BadRequestException("Duplicate nodes detected.");
        }

        return ipAddressArray;
    }
}
