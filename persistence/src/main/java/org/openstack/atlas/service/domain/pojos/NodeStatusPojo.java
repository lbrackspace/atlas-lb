package org.openstack.atlas.service.domain.pojos;
import org.openstack.atlas.service.domain.entities.NodeStatus;

public class NodeStatusPojo {

    private int loadbalancerId;
    private String ipAddress;
    private int port;
    private int nodeId;
    private NodeStatus status;

    public NodeStatusPojo() {
    }

    public NodeStatusPojo(int lid, String ip, int port, int nid, NodeStatus status){
        this.loadbalancerId = lid;
        this.ipAddress = ip;
        this.port = port;
        this.nodeId = nid;
        this.status = status;
    }

    @Override
    public String toString() {
        return "NodeStatusPojo{" + "loadbalancerId=" + loadbalancerId + 
                ", ipAddress=" + ipAddress + ", port=" + port +
                ", nodeId=" + nodeId + ", nodeStatus=" + status + '}';
    }

    public int getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(int loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }
}
