package org.openstack.atlas.api.mgmt.resources.providers;

import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.repository.*;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRepository;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.api.mgmt.helpers.LDAPTools.MossoAuthConfig;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.api.resources.providers.RequestStateContainer;
import org.openstack.atlas.util.ip.IPv6;
import org.dozer.DozerBeanMapper;
import org.openstack.atlas.cfg.Configuration;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ManagementDependencyProvider {

    private boolean mockitoAuth = false;
    protected final static String VFAIL = "Validation Failure";
    private MossoAuthConfig mossoAuthConfig;
    private RequestStateContainer requestStateContainer;
    protected ManagementAsyncService managementAsyncService;
    protected AsyncService esbService;
    protected DozerBeanMapper dozerMapper;
    protected ClusterRepository clusterRepository;
    protected HostRepository hostRepository;
    protected LoadBalancerRepository loadBalancerRepository;
    protected AlertRepository alertRepository;
    protected VirtualIpRepository vipRepository;
    protected GroupRepository groupRepository;
    protected LoadBalancerEventRepository eventRepository;
    protected AccountLimitRepository accountLimitRepository;
    protected AccountUsageRepository accountUsageRepository;
    protected UsageRepository usageRepository;
    protected HostUsageRepository hostUsageRepository;
    protected BlacklistRepository blacklistRepository;
    protected AllowedDomainsRepository allowedDomainsRepository;
    protected LoadBalancerStatusHistoryRepository loadBalancerStatusHistoryRepository;

    protected TicketService ticketService;
    protected AccountLimitService accountLimitService;
    protected LoadBalancerService loadBalancerService;
    protected VirtualIpService virtualIpService;
    protected HostService hostService;
    protected GroupService groupService;
    protected AlertService alertService;
    protected RateLimitingService rateLimitingService;
    protected CallbackService callbackService;
    protected NotificationService notificationService;
    protected BlackListService blackListService;
    protected ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
    protected SuspensionService suspensionService;
    protected ClusterService clusterService;
    protected JobStateService jobStateService;
    protected AllowedDomainsService allowedDomainsService;
    protected LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    protected Configuration configuration;

    public static String getStackTraceMessage(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Exception: %s:%s\n", e.getMessage(), e.getClass().getName()));
        for (StackTraceElement se : e.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }

    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }

    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    public void setVirtualIpService(VirtualIpService virtualIpService) {
        this.virtualIpService = virtualIpService;
    }

    public ManagementAsyncService getManagementAsyncService() {
        return managementAsyncService;
    }

    public void setEsbService(AsyncService esbService) {
        this.esbService = esbService;
    }

    public AsyncService getEsbService() {
        return esbService;
    }

    public void setAccountLimitService(AccountLimitService loadBalancerLimitGroupService) {
        this.accountLimitService = loadBalancerLimitGroupService;
    }

    public BlackListService getBlackListService() {
        return blackListService;
    }

    public void setBlackListService(BlackListService blackListService) {
        this.blackListService = blackListService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setHostService(HostService hostService) {
        this.hostService = hostService;
    }

    public void setRateLimitingService(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public void setJobStateService(JobStateService jobStateService) {
        this.jobStateService = jobStateService;
    }

    public void setSuspensionService(SuspensionService suspensionService) {
        this.suspensionService = suspensionService;
    }

    public void setTicketService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    public void setReverseProxyLoadBalancerService(ReverseProxyLoadBalancerService reverseProxyLoadBalancerService) {
        this.reverseProxyLoadBalancerService = reverseProxyLoadBalancerService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setManagementAsyncService(ManagementAsyncService managementAsyncService) {
        this.managementAsyncService = managementAsyncService;
    }

    public void setCallbackService(CallbackService callbackService) {
            this.callbackService = callbackService;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ClusterRepository getClusterRepository() {
        return clusterRepository;
    }

    public void setClusterRepository(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    public HostRepository getHostRepository() {
        return hostRepository;
    }

    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    public LoadBalancerRepository getLoadBalancerRepository() {
        return loadBalancerRepository;
    }

    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    public AlertRepository getAlertRepository() {
        return alertRepository;
    }

    public void setAlertRepository(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public DozerBeanMapper getDozerMapper() {
        return dozerMapper;
    }

    public void setDozerMapper(DozerBeanMapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    public GroupRepository getGroupRepository() {
        return groupRepository;
    }

    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public LoadBalancerEventRepository getEventRepository() {
        return eventRepository;
    }

    public void setEventRepository(LoadBalancerEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public AccountLimitRepository getAccountLimitRepository() {
        return accountLimitRepository;
    }

    public void setAccountLimitRepository(AccountLimitRepository accountLimitRepository) {
        this.accountLimitRepository = accountLimitRepository;
    }

    public AccountUsageRepository getAccountUsageRepository() {
        return accountUsageRepository;
    }

    public void setAccountUsageRepository(AccountUsageRepository accountUsageRepository) {
        this.accountUsageRepository = accountUsageRepository;
    }

    public UsageRepository getUsageRepository() {
        return usageRepository;
    }

    public void setUsageRepository(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    public HostUsageRepository getHostUsageRepository() {
        return hostUsageRepository;
    }

    public void setHostUsageRepository(HostUsageRepository hostUsageRepository) {
        this.hostUsageRepository = hostUsageRepository;
    }

    public VirtualIpRepository getVipRepository() {
        return vipRepository;
    }

    public void setVipRepository(VirtualIpRepository vipRepository) {
        this.vipRepository = vipRepository;
    }

    public RequestStateContainer getRequestStateContainer() {
        return requestStateContainer;
    }

    public void setRequestStateContainer(RequestStateContainer requestStateContainer) {
        this.requestStateContainer = requestStateContainer;
    }

    public BlacklistRepository getBlacklistRepository() {
        return blacklistRepository;
    }

    public void setBlacklistRepository(BlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    public void setAllowedDomainsService(AllowedDomainsService allowedDomainsService) {
        this.allowedDomainsService = allowedDomainsService;
    }

    public void setAllowedDomainsRepository(AllowedDomainsRepository allowedDomainsRepository) {
        this.allowedDomainsRepository = allowedDomainsRepository;
    }

    public void setLoadBalancerStatusHistoryRepository(LoadBalancerStatusHistoryRepository loadBalancerStatusHistoryRepository) {
        this.loadBalancerStatusHistoryRepository = loadBalancerStatusHistoryRepository;
    }

    public void setLoadBalancerStatusHistoryService(LoadBalancerStatusHistoryService loadBalancerStatusHistoryService) {
        this.loadBalancerStatusHistoryService = loadBalancerStatusHistoryService;
    }

    public Set<String> getLDAPGroups() {
        Set<String> groupSet = new HashSet<String>();
        List<String> groupList;
        try {
            groupList = this.requestStateContainer.getHttpHeaders().getRequestHeader("LDAPGroups");
            if (groupList == null) {
                return groupSet;
            } else {
                groupSet = new HashSet<String>(groupList);
                return groupSet;
            }
        } catch (NullPointerException ex) {
            return groupSet;
        }
    }

    public String getLDAPUser() {
        String out = null;
        try {
            out = this.requestStateContainer.getHttpHeaders().getRequestHeaders().getFirst("LDAPUser");
            return out;
        } catch (NullPointerException ex) {
            return out;
        }
    }

    public Set<String> userRoles() {
        Set<String> out = new HashSet<String>();
        Map<String, HashSet<String>> roleMap = getMossoAuthConfig().getRoles();
        Set<String> roleNames = roleMap.keySet();
        for (String roleName : roleNames) {
            Set<String> groupsInRole = roleMap.get(roleName);
            Set<String> ldapGroups = getLDAPGroups();
            if (intersection(ldapGroups, groupsInRole).size() > 0) {
                out.add(roleName);
            }
        }
        return out;
    }

    private boolean isBypassAuth() {
        if (isMockitoAuth()) {
            return true;
        }
        try {
            List<String> bypassauth = getRequestStateContainer().getHttpHeaders().getRequestHeader("BYPASS-AUTH");
            if (bypassauth != null && !bypassauth.isEmpty() && bypassauth.get(0).toLowerCase().equals("true")) {
                return true;
            }
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("No auth data founf\n", ex);
        }
        return false;
    }

    public boolean isUserInRole(String roleStr) {
        int i;
        boolean out = false;

        if (isBypassAuth()) {
            return true; // Bypass auth was set to true so consider this request valid.
        }

        String[] roleSplit = roleStr.split(",");
        for (i = 0; i < roleSplit.length; i++) {
            String roleName = roleSplit[i];
            if (!getMossoAuthConfig().getRoles().containsKey(roleName)) {
                String fileName = getMossoAuthConfig().getFileName();
                String format = "Role \"%s\" was not found in %s\n";
                String msg = String.format(format, roleName, fileName);
                throw new IllegalArgumentException(msg);
            }
            Set<String> roleSet = getMossoAuthConfig().getRoles().get(roleName);
            if (intersection(getLDAPGroups(), roleSet).size() > 0) {
                out = true;
                return out;
            }
        }
        return out;
    }

    public Set<String> intersection(Set<String> a, Set<String> b) {
        Set<String> out = new HashSet<String>(a);
        a.retainAll(b);
        return a;
    }

    public MossoAuthConfig getMossoAuthConfig() {
        return mossoAuthConfig;
    }

    public void setMossoAuthConfig(MossoAuthConfig mossoAuthConfig) {
        this.mossoAuthConfig = mossoAuthConfig;
    }

    public void nop() {
    }

    public boolean isMockitoAuth() {
        return mockitoAuth;
    }

    // setMokitoAuth(true) when your running mock tests on this class
    // otherwise your tests will fail while trying to fetch auth data from non existent headers
    public void setMockitoAuth(boolean mockitoAuth) {
        this.mockitoAuth = mockitoAuth;
    }

    public String expandipv6(String ip) {
        String expanded;
        if (ip == null) {
            return null;
        }
        try {
            expanded = (new IPv6(ip)).expand();
        } catch (org.openstack.atlas.util.ip.exception.IPStringConversionException ex) {
            return null;
        }
        return expanded;
    }

    public Response getValidationFaultResponse(String errorStr){
        List<String> errorStrs = new ArrayList<String>();
        errorStrs.add(errorStr);
        return getValidationFaultResponse(errorStrs);
    }

     public Response getValidationFaultResponse(List<String> errorStrs) {
        BadRequest badreq;
        int status = 400;
        badreq = HttpResponseBuilder.buildBadRequestResponse(VFAIL, errorStrs);
        Response resp = Response.status(status).entity(badreq).build();
        return resp;
    }

    // Got tired of always import StringUtils.getExtendedStackTrace so I'm aliasing it
    public String getExtendedStackTrace(Throwable ti){
        String out;
        out = org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace(ti);
        return out;
    }

    public void expandIpv6InHost(Host host) {
        String ipv6Public = expandipv6(host.getIpv6Public());
        String ip8Servicenet = expandipv6(host.getIpv6Servicenet());
        host.setIpv6Public(ipv6Public);
        host.setIpv6Servicenet(ip8Servicenet);
    }
}
