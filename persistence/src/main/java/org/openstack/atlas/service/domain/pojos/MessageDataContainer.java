package org.openstack.atlas.service.domain.pojos;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadbalancerMeta;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.usage.BitTag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessageDataContainer implements Serializable {
    private final static long serialVersionUID = 532512316L;

    private String userName;
    private Integer accountId;
    private Integer loadBalancerId;
    private Integer virtualIpId;
    private String errorFileContents;
    private Integer nodeId;
    private Integer clusterId;
    private List<Integer> newVipIds;
    private List<Integer> newNodeIds;
    private ZeusSslTermination zeusSslTermination;
    private Collection<LoadbalancerMeta> metadata;
    private LoadBalancer loadBalancer;
    //for batch deletes
    private List<Integer> ids;
    private SslTermination previousSslTermination;

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

    public String getErrorFileContents() {
        return errorFileContents;
    }

    public void setErrorFileContents(String errorFileContents) {
        this.errorFileContents = errorFileContents;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public ZeusSslTermination getZeusSslTermination() {
        return zeusSslTermination;
    }

    public void setZeusSslTermination(ZeusSslTermination zeusSslTermination) {
        this.zeusSslTermination = zeusSslTermination;
    }

    public Collection<LoadbalancerMeta> getMetadata() {
        if(metadata == null) {
            metadata = new ArrayList<LoadbalancerMeta>();
        }
        return metadata;
    }

    public void setMetadata(Collection<LoadbalancerMeta> metadata) {
        this.metadata = metadata;
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public SslTermination getPreviousSslTermination() {
        return previousSslTermination;
    }

    public void setPreviousSslTermination(SslTermination previousSslTerminationTags) {
        this.previousSslTermination = previousSslTerminationTags;
    }
}
