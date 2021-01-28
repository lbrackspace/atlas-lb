package org.openstack.atlas.api.resources.providers;

import org.dozer.Mapper;
import org.openstack.atlas.api.atom.AtomFeedAdapter;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.ServiceUnavailableException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.util.ip.DnsUtil;
import org.openstack.atlas.util.ip.IPUtils;

import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CommonDependencyProvider {

    private RequestStateContainer requestStateContainer;

    protected final static String NOBODY = "Undefined User";
    protected final static String USERHEADERNAME = "X-PP-User";
    protected final static String VFAIL = "Validation Failure";

    protected RestApiConfiguration restApiConfiguration;
    protected AsyncService asyncService;
    protected LoadBalancerRepository lbRepository;
    protected Mapper dozerMapper;
    protected DozerMapperBuilderBean dozerMapperBuilderBean;
    protected AtomFeedAdapter atomFeedAdapter;
    protected LoadBalancerService loadBalancerService;
    protected HealthMonitorService healthMonitorService;
    protected ConnectionLoggingService connectionLoggingService;
    protected ContentCachingService contentCachingService;
    protected ConnectionThrottleService connectionThrottleService;
    protected VirtualIpService virtualIpService;
    protected LoadbalancerMetadataService loadbalancerMetadataService;
    protected NodeMetadataService nodeMetadataService;
    protected NodeService nodeService;
    protected SessionPersistenceService sessionPersistenceService;
    protected AccountLimitService accountLimitService;
    protected AccessListService accessListService;
    protected AlgorithmsService algorithmsService;
    protected UsageService usageService;
    protected ProtocolsService protocolsService;
    protected SslTerminationService sslTerminationService;
    protected SslCipherProfileService sslCipherProfileService;
    protected CertificateMappingService certificateMappingService;
    protected AllowedDomainsService allowedDomainsService;
    protected LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    protected LoadBalancerEventRepository loadBalancerEventRepository;
    protected ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
    protected HostService hostService;
    protected ClusterService clusterService;

    public void init() throws Exception{
        dozerMapper = dozerMapperBuilderBean.getDozerMapperObject();
    }

    public void setReverseProxyLoadBalancerVTMService(ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService) {
        this.reverseProxyLoadBalancerVTMService = reverseProxyLoadBalancerVTMService;
    }

    public void setProtocolsService(ProtocolsService protocolsService) {
        this.protocolsService = protocolsService;
    }

    public void setAlgorithmsService(AlgorithmsService algorithmsService) {
        this.algorithmsService = algorithmsService;
    }

    public void setSessionPersistenceService(SessionPersistenceService sessionPersistenceService) {
        this.sessionPersistenceService = sessionPersistenceService;
    }

    public void setConnectionThrottleService(ConnectionThrottleService connectionThrottleService) {
        this.connectionThrottleService = connectionThrottleService;
    }

    public void setAccessListService(AccessListService accessListService) {
        this.accessListService = accessListService;
    }

    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    public void setConnectionLoggingService(ConnectionLoggingService connectionLoggingService) {
        this.connectionLoggingService = connectionLoggingService;
    }

    public void setContentCachingService(ContentCachingService contentCachingService) {
        this.contentCachingService = contentCachingService;
    }

    public void setHealthMonitorService(HealthMonitorService healthMonitorService) {
        this.healthMonitorService = healthMonitorService;
    }

    public void setVirtualIpService(VirtualIpService virtualIpService) {
        this.virtualIpService = virtualIpService;
    }

    public void setLoadbalancerMetadataService(LoadbalancerMetadataService loadbalancerMetadataService) {
        this.loadbalancerMetadataService = loadbalancerMetadataService;
    }

    public void setNodeMetadataService(NodeMetadataService nodeMetadataService) {
        this.nodeMetadataService = nodeMetadataService;
    }

    public void setLoadBalancerEventRepository(LoadBalancerEventRepository loadBalancerEventRepository) {
        this.loadBalancerEventRepository = loadBalancerEventRepository;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setUsageService(UsageService usageService) {
        this.usageService = usageService;
    }

    public void setAsyncService(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    public void setLbRepository(LoadBalancerRepository lbRepository) {
        this.lbRepository = lbRepository;
    }

    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    public void setAtomFeedAdapter(AtomFeedAdapter atomFeedAdapter) {
        this.atomFeedAdapter = atomFeedAdapter;
    }

    public void setAccountLimitService(AccountLimitService accountLimitService) {
        this.accountLimitService = accountLimitService;
    }

    public void setSslTerminationService(SslTerminationService sslTerminationService) {
        this.sslTerminationService = sslTerminationService;
    }

    public void setSslCipherProfileService(SslCipherProfileService sslCipherProfileService) {
        this.sslCipherProfileService = sslCipherProfileService;
    }

    public void setCertificateMappingService(CertificateMappingService certificateMappingService) {
        this.certificateMappingService = certificateMappingService;
    }

    public void setAllowedDomainsService(AllowedDomainsService allowedDomainsService) {
        this.allowedDomainsService = allowedDomainsService;
    }

    public void setLoadBalancerStatusHistoryService(LoadBalancerStatusHistoryService loadBalancerStatusHistoryService) {
        this.loadBalancerStatusHistoryService = loadBalancerStatusHistoryService;
    }

    public void setHostService(HostService hostService) {
        this.hostService = hostService;
    }

    public DozerMapperBuilderBean getDozerMapperBuilderBean() {
        return dozerMapperBuilderBean;
    }

    public void setDozerMapperBuilderBean(DozerMapperBuilderBean dozerMapperBuilderBean) {
        this.dozerMapperBuilderBean = dozerMapperBuilderBean;
    }

    public String getUserName(HttpHeaders headers) {
        if (headers == null || headers.getRequestHeader(USERHEADERNAME).size() < 1) {
            return NOBODY;
        }
        String userName = headers.getRequestHeader(USERHEADERNAME).get(0);
        if (userName == null) {
            return NOBODY;
        }
        return userName;
    }

    public Response getValidationFaultResponse(ValidatorResult result) {
        List<String> vmessages = result.getValidationErrorMessages();
        int status = 400;
        BadRequest badreq = HttpResponseBuilder.buildBadRequestResponse(VFAIL, vmessages);
        Response vresp = Response.status(status).entity(badreq).build();
        return vresp;
    }

    public Response getValidationFaultResponse(String errorStr) {
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

    public RequestStateContainer getRequestStateContainer() {
        return requestStateContainer;
    }

    public void setRequestStateContainer(RequestStateContainer requestStateContainer) {
        this.requestStateContainer = requestStateContainer;
    }

    public void setRestApiConfiguration(RestApiConfiguration restApiConfiguration) {
        this.restApiConfiguration = restApiConfiguration;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public List<String> verifyNodeDomains(Collection<Node> nodes) throws BadRequestException {
        String fmt;
        String msg;
        List<String> foundIps;
        List<String> errors = new ArrayList<String>();
        for (Node node : nodes) {
            String address = node.getAddress();
            if (IPUtils.isValidIpv4String(address)) {
                continue;// If this was an IPv4 Address don't try to validate it as a domain.
            } else if (IPUtils.isValidIpv6String(address)) {
                continue; // If this was an IPv6 address don't try to validate it as a domain
            } else if (address.matches(".*[a-zA-Z]+.*")) {
                if (!allowedDomainsService.hasHost(node.getAddress())) {
                    fmt = "The address %s is not a valid IPv4, IPv6 address or an FQDN that is in an"
                            + " authorized Domain";
                    msg = String.format(fmt, address);
                    errors.add(msg);
                }
                try {
                    foundIps = DnsUtil.lookup(address, "A", "AAAA");
                } catch (InvalidNameException inv) {
                     fmt = "Lables cannot exceed 63 octets, cannot complete request at this time..";
                    msg = String.format(fmt, address);
                    throw new BadRequestException(msg, inv);
                } catch (NamingException ne) {
                    fmt = "Unable to resolve host %s could not add node at this time";
                    msg = String.format(fmt, address);
                    throw new ServiceUnavailableException(msg, ne);
                }
                if (foundIps.isEmpty()) {
                    fmt = "domain %s had no A or AAAA records. Can not add node. domain must have only 1 A or AAAA record";
                    msg = String.format(fmt, address);
                    errors.add(msg);
                } else if (foundIps.size() > 1) {
                    fmt = "domain %s has %d A or AAAA records";
                    msg = String.format(fmt, address, foundIps.size());
                    errors.add(msg);
                }
            }

        }
        return errors;
    }
}
