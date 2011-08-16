package org.openstack.atlas.service.domain.pojo;

import org.openstack.atlas.service.domain.entity.LoadBalancer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageDataContainer implements Serializable {
    private final static long serialVersionUID = 532512316L;

    private String userName;
    private Integer accountId;
    private Integer loadBalancerId;
    private Integer virtualIpId;
    private Integer nodeId;
    private List<Integer> newVipIds;
    private List<Integer> newNodeIds;
    //for batch deletes
    private List<Integer> ids;
    private LoadBalancer loadBalancer;

    public List<Integer> getIds() {
        if(ids == null){
            ids = new ArrayList<Integer>();
        }
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public Integer getVirtualIpId() {
        return virtualIpId;
    }

    public void setVirtualIpId(Integer virtualIpId) {
        this.virtualIpId = virtualIpId;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public List<Integer> getNewVipIds() {
        if (newVipIds == null) {
            newVipIds = new ArrayList<Integer>();
        }
        return newVipIds;
    }

    public void setNewVipIds(List<Integer> newVipIds) {
        this.newVipIds = newVipIds;
    }

    public List<Integer> getNewNodeIds() {
        if (newNodeIds == null) {
            newNodeIds = new ArrayList<Integer>();
        }
        return newNodeIds;
    }

    public void setNewNodeIds(List<Integer> newNodeIds) {
        this.newNodeIds = newNodeIds;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }
}
