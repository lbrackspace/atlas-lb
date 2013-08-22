package org.openstack.atlas.adapter.helpers;

import com.zxtm.service.client.PoolPriorityValueDefinition;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeCondition;
import org.openstack.atlas.service.domain.entities.NodeType;
import org.openstack.atlas.util.converters.StringConverter;

import java.util.*;

public class ZeusNodePriorityContainer {
    private List<PoolPriorityValueDefinition> priorityValues;
    private Map<String, Integer> priorityValuesMap;
    private Set<String> priorityValuesSet;
    private boolean constainsSecondary;
    private boolean constainsPrimary;

    public ZeusNodePriorityContainer(Collection<Node> nodesIn) {
        Map<String, Integer> pMap = new HashMap<String, Integer>();
        priorityValuesSet = new HashSet<String>();
        Node[] nodes = (Node[]) nodesIn.toArray(new Node[nodesIn.size()]);
        priorityValues = new ArrayList<PoolPriorityValueDefinition>();
        constainsSecondary = false;
        constainsPrimary = true;
        for (Node node : nodes) {
            if (node.getCondition() == NodeCondition.DISABLED) {
                continue; // This node is not enabled so it doesn't count as either primary or secondary.
            }
            PoolPriorityValueDefinition pVal = new PoolPriorityValueDefinition();
            int zPri;
            String nodeIpStr = IpHelper.createZeusIpString(node.getIpAddress(), node.getPort());
            if (node.getType() == NodeType.SECONDARY) {
                zPri = 1;
                constainsSecondary = true;
            } else {
                zPri = 2;
                constainsPrimary = true;
            }

            pMap.put(nodeIpStr, zPri);

            pVal.setNode(nodeIpStr);
            pVal.setPriority(zPri);
            priorityValues.add(pVal);
            priorityValuesSet.add(String.format("%s:%d", nodeIpStr, zPri));
        }
        priorityValuesMap = pMap;
    }

    public PoolPriorityValueDefinition[][] getPriorityValues() {
        PoolPriorityValueDefinition[][] out = new PoolPriorityValueDefinition[1][];
        out[0] = priorityValues.toArray(new PoolPriorityValueDefinition[1]);
        return out;
    }

    public Map<String, Integer> getPriorityValuesMap() {
       return priorityValuesMap;
    }

    public Set<String> getPriorityValuesSet() {
       return priorityValuesSet;
    }

    public boolean hasSecondary() {
        return constainsSecondary;
    }

    public boolean hasPrimary() {
        return constainsPrimary;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        List<String> nodeStrList = new ArrayList<String>();
        String nodeStr;
        int i;
        int priCount = 0;
        int secCount = 0;
        String nodeName;
        int zPri;

        for (PoolPriorityValueDefinition pv : priorityValues) {
            nodeName = pv.getNode();
            zPri = pv.getPriority();
            if (zPri >= 2) {
                priCount++;
            } else {
                secCount++;
            }
            nodeStr = String.format("{addr:\"%s\",priority: %d}", nodeName, zPri);
            nodeStrList.add(nodeStr);
        }
        nodeStr = StringConverter.commaSeperatedStringList(nodeStrList);
        sb.append(String.format("{ primaryNodeCount: %d,", priCount));
        sb.append(String.format(" secondaryNodeCount: %d,", secCount));
        sb.append(String.format(" nodes: [%s]}", nodeStr));
        return sb.toString();
    }
}
