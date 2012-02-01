package org.openstack.atlas.adapter.helpers;

import com.zxtm.service.client.PoolPriorityValueDefinition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeCondition;
import org.openstack.atlas.service.domain.entities.NodeType;
import org.openstack.atlas.util.converters.StringConverter;

public class ZeusNodePriorityContainer {

    public static final int INVALID = 0;
    public static final int NOACTION = 1;
    public static final int DISABLE = 2;
    public static final int ENABLE = 3;
    private List<PoolPriorityValueDefinition> priorityValues;
    private boolean constainsSecondary;
    private boolean constainsPrimary;

    public static int getAction(ZeusNodePriorityContainer oldPri,ZeusNodePriorityContainer newPri) {
        if (!newPri.hasPrimary()) {
            return INVALID;
        } else if (oldPri.hasSecondary() == newPri.hasSecondary()) {
            return NOACTION;
        } else if (newPri.hasSecondary()) {
            return ENABLE;
        } else {
            return DISABLE;
        }
    }

    public ZeusNodePriorityContainer(Collection<Node> nodesIn) {
        Node[] nodes = (Node[]) nodesIn.toArray(new Node[1]);
        priorityValues = new ArrayList<PoolPriorityValueDefinition>();
        constainsSecondary = false;
        constainsPrimary = true;
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if(node.getCondition()!= NodeCondition.ENABLED){
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
            pVal.setNode(nodeIpStr);
            pVal.setPriority(zPri);
            priorityValues.add(pVal);
        }
    }

    public PoolPriorityValueDefinition[][] getPriorityValues() {
        PoolPriorityValueDefinition[][] out = new PoolPriorityValueDefinition[1][];
        out[0] = priorityValues.toArray(new PoolPriorityValueDefinition[1]);
        return out;
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
        sb.append(String.format("{ primaryNodeCount: %d,\n", priCount));
        sb.append(String.format(" secondaryNodeCount: %d,\n", secCount));
        sb.append(String.format(" nodes: [%s]\n}", nodeStr));
        return sb.toString();
    }
}
