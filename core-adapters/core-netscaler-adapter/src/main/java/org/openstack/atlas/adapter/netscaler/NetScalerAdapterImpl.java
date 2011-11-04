package org.openstack.atlas.adapter.netscaler;

import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.repository.NodeRepository;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.*;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.*;
import java.io.*;
import java.net.*;


@Primary
@Service
public class NetScalerAdapterImpl implements LoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(NetScalerAdapterImpl.class.getName());
    private static String SOURCE_IP = "SOURCE_IP";
    private static String HTTP_COOKIE = "HTTP_COOKIE";
	protected NodeRepository nodeRepository;

    public void setNodeRepository(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws AdapterException 
    {
        String resourceType = "loadbalancers";

        Integer accountId = lb.getAccountId(); 

        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer nsLB = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer();

        NSAdapterUtils.populateNSLoadBalancer(lb, nsLB);

        String requestBody = NSAdapterUtils.getRequestBody(nsLB); 
        String serviceUrl = lb.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType);

        NSAdapterUtils.performRequest("POST", resourceUrl, requestBody);
    }

    @Override
    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lb.getId();

        Integer accountId = lb.getAccountId(); 
        
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer nsLB = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer();
        NSAdapterUtils.populateNSLoadBalancer(lb, nsLB);
        
		String requestBody = NSAdapterUtils.getRequestBody(nsLB);
		String serviceUrl = config.getHost().getEndpoint();
		String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId);
		NSAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config,  LoadBalancer lb)
            throws AdapterException 
    {
        String resourceType = "loadbalancers";
        
        Integer accountId = lb.getAccountId(); 
        Integer lbId = lb.getId();   
 
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, lbId);

        NSAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void createNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) 
        throws AdapterException
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "nodes";

        if(nodes.size() > 0)
        {
            com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Nodes nsNodes = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Nodes();
            NSAdapterUtils.populateNSNodes(nodes, nsNodes.getNodes());
            String requestBody = NSAdapterUtils.getRequestBody(nsNodes);
            String serviceUrl = config.getHost().getEndpoint();
            String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);
			
            NSAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
		}
    }
    

    @Override
    public void deleteNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) 
        throws AdapterException
    {
    	for(Node node: nodes)
    	{
			this.removeNode(config, lbId, accountId, node.getId());
    	}    
    }
    

    @Override
    public void updateNode(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Node node) 
        throws AdapterException 
    {
        LOG.info("updateNodes");// NOP
    }

    
    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, ConnectionThrottle conThrottle) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "connectionthrottle";
		
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.ConnectionThrottle nsThrottle = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.ConnectionThrottle();
		NSAdapterUtils.populateConnectionThrottle(conThrottle, nsThrottle);
        String requestBody = NSAdapterUtils.getRequestBody(nsThrottle);
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        NSAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "connectionthrottle";

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        NSAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, HealthMonitor monitor) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "healthmonitor";

        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor nsMon;

        nsMon  = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor();  

        NSAdapterUtils.populateNSHealthMonitor(monitor, nsMon);

        String requestBody = NSAdapterUtils.getRequestBody(nsMon);
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        NSAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "healthmonitor";

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        NSAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, SessionPersistence sessionPersistence) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "sessionpersistence";
		
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence nsPersistence = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence();
		NSAdapterUtils.populateSessionPersistence(sessionPersistence, nsPersistence);
        String requestBody = NSAdapterUtils.getRequestBody(nsPersistence);
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        NSAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "sessionpersistence";

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        NSAdapterUtils.performRequest("DELETE", resourceUrl, "");
    }


    private void removeNode(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Integer nodeId)
            throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "nodes";
		String serviceUrl = config.getHost().getEndpoint();
		String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId,childResourceType) + "/" + nodeId;
		
		NSAdapterUtils.performRequest("DELETE", resourceUrl);
    }
}

