package org.openstack.atlas.api.mgmt.resources;

import javax.ws.rs.GET;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.Ciphers;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

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
    private JobsResource jobsResource;
    private AuditResource auditResource;
    private BlackListResource blackListResource;
    private HealthCheckResource healthCheckResource;
    private AllowedDomainsResource allowedDomainsResource;
    private SslCipherProfileResource sslCipherProfileResource;

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
    public AlertsResource retrieveAlertsResource() {
        return alertsResource;
    }

    @Path("groups")
    public GroupsResource retrieveGroupsResource() {
        return groupsResource;
    }

    @Path("blacklist")
    public BlackListResource retrieveBlackListResource() {
        return blackListResource;
    }

    @Path("zeusevent")
    public CallbackResource retrieveCallbackResource() {
        return callbackResource;
    }

    @Path("event")
    public EventResource retrieveEventResource() {
        return eventResource;
    }

    @Path("jobs")
    public JobsResource retrieveJobsResource() {
        return jobsResource;
    }

    @Path("healthcheck")
    public HealthCheckResource retrieveHealthCheckResource() {
        return healthCheckResource;
    }

    @Path("alloweddomains")
    public AllowedDomainsResource retrieveAllowedDomainsResource() {
        return allowedDomainsResource;
    }

    @Path("cipherprofile")
    public SslCipherProfileResource retrieveSslCipherProfileResource() { return sslCipherProfileResource; }

    @GET
    @Path("ciphers")
    public Response getGlobalCiphers() {       
        String cipherList;
        try {
                cipherList = reverseProxyLoadBalancerVTMService.getSsl3Ciphers();

        } catch (EntityNotFoundException ex) {
            return ResponseFactory.getErrorResponse(ex, "No soap end points available", "");
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, "", "");
        }
        Ciphers ciphers = dozerMapper.map(cipherList, Ciphers.class);
        Response wtf = Response.status(200).entity(ciphers).build();
        return wtf;
    }

    public void setHealthCheckResource(HealthCheckResource healthCheckResource) {
        this.healthCheckResource = healthCheckResource;
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

    public void setJobsResource(JobsResource jobsResource) {
        this.jobsResource = jobsResource;
    }

    public void setAllowedDomainsResource(AllowedDomainsResource allowedDomainsResource) {
        this.allowedDomainsResource = allowedDomainsResource;
    }

    public SslCipherProfileResource getSslCipherProfileResource() {
        return sslCipherProfileResource;
    }

    public void setSslCipherProfileResource(SslCipherProfileResource sslCipherProfileResource) {
        this.sslCipherProfileResource = sslCipherProfileResource;
    }
}
