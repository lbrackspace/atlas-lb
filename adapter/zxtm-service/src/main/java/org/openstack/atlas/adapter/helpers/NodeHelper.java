package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.util.converters.StringConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
            ipAddressArray[0][i] = IpHelper.createZeusIpString(node.getIpAddress(), node.getPort());
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
}
