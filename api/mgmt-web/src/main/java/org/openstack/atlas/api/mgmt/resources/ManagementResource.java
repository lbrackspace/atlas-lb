package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.Path;

public class ManagementResource extends ManagementDependencyProvider {

    private HostsResource hostsResource;
    private VirtualIpsResource virtualIpsResource;
    private ClustersResource clustersResource;
    private LoadBalancersResource loadBalancersResource;
    private LoadBalancerSuspensionResource suspensionResource;
    private AccountsResource accountsResource;
    private CallbackResource callbackResource;
    private AlertsResource alertsResource;
    private StubResource stubResource;
    private BounceResource bounceResource;
    private GroupsResource groupsResource;
    private EventResource eventResource;
    private AuditResource auditResource;
    private BlackListResource blackListResource;

    @Path("accounts")
    public AccountsResource retrieveAccountsResource() {
        return accountsResource;
    }

    @Path("audit")
    public AuditResource retrieveAuditResource() {
        return auditResource;
    }
    
    @Path("hosts")
    public HostsResource retrieveHostsResource() {
        return hostsResource;
    }

    @Path("stub")
    public StubResource retrieveStubResource() {
        return stubResource;
    }

    @Path("bounce")
    public BounceResource retrieveBounceResource() {
        return bounceResource;
    }

    @Path("virtualips")
    public VirtualIpsResource retreiveVirtualIpsResource() {
        return virtualIpsResource;
    }

    @Path("clusters")
    public ClustersResource retrieveClustersResource() {
        return clustersResource;
    }

    @Path("loadbalancers")
    public LoadBalancersResource retrieveLoadBalancersResource() {
        return loadBalancersResource;
    }

    @Path("alerts")
    public AlertsResource retrieveAlertsResource(){
        return alertsResource;
    }

    @Path("groups")
    public GroupsResource retrieveGroupsResource(){
        return groupsResource;
    }

    @Path("blacklist")
    public BlackListResource retrieveBlackListResource(){
        return blackListResource;
    }

    @Path("lbdeviceevent")
    public CallbackResource retrieveCallbackResource() {
        return callbackResource;
    }

    @Path("event")
    public EventResource retrieveEventResource() {
        return eventResource;
    }

    public void setBounceResource(BounceResource bounceResource) {
        this.bounceResource = bounceResource;
    }

    public void setStubResource(StubResource stubResource) {
        this.stubResource = stubResource;
    }

    public void setAccountsResource(AccountsResource accountsResource) {
        this.accountsResource = accountsResource;
    }

    public void setHostsResource(HostsResource hostsResource) {
        this.hostsResource = hostsResource;
    }

    public void setVirtualIpsResource(VirtualIpsResource virtualIpsResource) {
        this.virtualIpsResource = virtualIpsResource;
    }

    public void setClustersResource(ClustersResource clustersResource) {
        this.clustersResource = clustersResource;
    }

    public void setAuditResource(AuditResource auditResource) {
        this.auditResource = auditResource;
    }

    public void setLoadBalancersResource(LoadBalancersResource loadBalancersResource) {
        this.loadBalancersResource = loadBalancersResource;
    }

    public void setCallbackResource(CallbackResource callbackResource) {
        this.callbackResource = callbackResource;
    }

    public void setAlertsResource(AlertsResource alertsResource) {
        this.alertsResource = alertsResource;
    }

    public void setGroupsResource(GroupsResource groupsResource) {
        this.groupsResource = groupsResource;
    }

    public void setSuspensionResource(LoadBalancerSuspensionResource suspensionResource) {
        this.suspensionResource = suspensionResource;
    }

    public void setEventResource(EventResource eventResource) {
        this.eventResource = eventResource;
    }

    public void setBlackListResource(BlackListResource blackListResource) {
        this.blackListResource = blackListResource;
    }

    public BlackListResource getBlackListResource() {
        return blackListResource;
    }
}
