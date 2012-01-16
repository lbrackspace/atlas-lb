package org.openstack.atlas.service.domain.pojo;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageDataContainer implements Serializable {
    private final static long serialVersionUID = 532512316L;

    private Integer accountId;
    private Integer loadBalancerId;
    private Integer resourceId;
    private List<Integer> resourceIds;
    private Object resource;
    private String userName;

    public MessageDataContainer() {
    }

    public MessageDataContainer(Integer accountId, Integer loadBalancerId) {
        this.accountId = accountId;
        this.loadBalancerId = loadBalancerId;
    }

    public List<Integer> getIds() {
        if(resourceIds == null){
            resourceIds = new ArrayList<Integer>();
        }
        return resourceIds;
    }

    public void setIds(List<Integer> ids) {
        this.resourceIds = ids;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public Integer getVirtualIpId() {
        return resourceId;
    }

    public void setVirtualIpId(Integer virtualIpId) {
        this.resourceId = virtualIpId;
    }

    public Integer getNodeId() {
        return resourceId;
    }

    public void setNodeId(Integer nodeId) {
        this.resourceId = nodeId;
    }

    public List<Integer> getNewVipIds() {
        if (resourceIds == null) {
            resourceIds = new ArrayList<Integer>();
        }
        return resourceIds;
    }

    public void setNewVipIds(List<Integer> newVipIds) {
        this.resourceIds = newVipIds;
    }

    public List<Integer> getNewNodeIds() {
        if (resourceIds == null) {
            resourceIds = new ArrayList<Integer>();
        }
        return resourceIds;
    }

    public void setNewNodeIds(List<Integer> newNodeIds) {
        this.resourceIds = newNodeIds;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.resource = loadBalancer;
    }

    public LoadBalancer getLoadBalancer() {
        return (LoadBalancer) resource;
    }

    public void setNode(Node node) {
        this.resource = node;
    }

    public Node getNode() {
        return (Node) resource;
    }

    public void setVirtualIp(VirtualIp virtualIp) {
        this.resource = virtualIp;
    }

    public VirtualIp getVirtualIp() {
        return (VirtualIp) resource;
    }

    public void setVirtualIpv6(VirtualIpv6 virtualIp) {
        this.resource = virtualIp;
    }

    public VirtualIpv6 getVirtualIpv6() {
        return (VirtualIpv6) resource;
    }

    public void setLoadBalancers(List<LoadBalancer> loadBalancers) {
        this.resource =  loadBalancers;
    }

    public List<LoadBalancer> getLoadBalancers() {
        return  (List<LoadBalancer>) this.resource;
    }

    public void setNodes(List<Node> nodes) {
        this.resource = nodes;
    }

    public List<Node> getNodes() {
        return  (List<Node>) this.resource;
    }

    public void setVirtualIps(List<VirtualIp> virtualIps) {
        this.resource = virtualIps;
    }

    public List<VirtualIp> getVirtualIps() {
        return (List<VirtualIp>) this.resource;
    }

    public Object getResource() {
        return resource;
    }

    public void setResource(Object resource) {
        this.resource = resource;
    }
}
