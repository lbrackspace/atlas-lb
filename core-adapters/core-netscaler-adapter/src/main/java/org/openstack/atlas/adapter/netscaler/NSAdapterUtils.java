package org.openstack.atlas.adapter.netscaler;

import java.util.*;
import java.io.*;
import java.net.*;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openstack.atlas.adapter.exception.*;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;


public class NSAdapterUtils
{
    public static Log LOG = LogFactory.getLog(NSAdapterUtils.class.getName());

    static String getLBURLStr(String serviceUrl, Integer accountId, String resourceType)
    {
    	String resourceUrl = serviceUrl + "/" + accountId + "/" + resourceType;

        return resourceUrl;
    }


    static String getLBURLStr(String serviceUrl, Integer accountId, String resourceType, Integer resourceId)
    {
    	String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType) + "/" + resourceId;

        return resourceUrl;
    }


    static String getLBURLStr(String serviceUrl, Integer accountId, String resourceType, Integer resourceId, String childResourceType)
    {
    	String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId) + "/" + childResourceType;

        return resourceUrl;
    }


    static String getLBURLStr(String serviceUrl, Integer accountId, String resourceType, Integer resourceId, String childResourceType, Integer childResourceId)
    {
    	String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType) + "/" + childResourceId;

        return resourceUrl;
    }

    
    static String performRequest(String method, String urlStr, String requestBody) 
           throws AdapterException
    {
        LOG.debug(String.format("Service URL string: '%s'...", urlStr));
		
        LOG.debug(String.format("Load balancer request " + new Throwable().getStackTrace()[1].getMethodName() + ": '%s'...", requestBody));
	
        Map<String, String> headers = new HashMap<String, String>();
		
        headers.put("Content-Type", "application/xml");
        headers.put("Accept", "application/xml");
        headers.put("X-Auth-Token", "tk82848ebd-f079-4959-bbd3-6c7e27ea4d9a");
		
        try 
        {
            return NSRequest.perform_request(method, urlStr, headers, requestBody);
        } 
        catch (Exception e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            
            LOG.debug(e.getMessage());
            LOG.debug(stacktrace);
            throw new AdapterException("Exception occurred: " + e.getMessage(), new Error());    		
        }
    }


    static void performRequest(String method, String urlStr) 
           throws AdapterException
    {
        NSAdapterUtils.performRequest(method, urlStr, null);
    }


    static Object getResponseObject(String response) 
           throws AdapterException
    {
        String requestBody;
	 	
        try 
        {
			JAXBContext ctxt = JAXBContext.newInstance("com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1");
			Unmarshaller  u = ctxt.createUnmarshaller() ; 
			return u.unmarshal( new StreamSource( new StringReader( response) ) );
		} 
        catch (JAXBException e) 
        {
			System.out.println("error: " + e.toString());
			e.printStackTrace();
			throw new AdapterException("Failed to transform a XML Payload to JAXB object", new Error());   
		}
	}


    static String getRequestBody(Object marshalObject) 
           throws AdapterException
    {
        String requestBody;
	 	
        try 
        {
            JAXBContext ctxt = JAXBContext.newInstance("com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1");
            Marshaller m = ctxt.createMarshaller() ; 
            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            Writer writer = new StringWriter();
			
            m.marshal(marshalObject, writer);
	        requestBody = writer.toString();
	
	        LOG.debug("request body: " + requestBody);
	    } 
        catch (JAXBException e) 
        {
	        System.out.println("error: " + e.toString());
	        e.printStackTrace();
	        throw new AdapterException("Failed to transform a JAXB object to XML payload...", new Error());   
	    }

    	return requestBody;
    }



    static void populateNSLoadBalancer(LoadBalancer lb, com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer nsLB) 
           throws BadRequestException
    {
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.VirtualIp nsVIP = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.VirtualIp();
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor nsMon = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor();
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence nsPersistence = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence();
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.ConnectionThrottle nsThrottle = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.ConnectionThrottle();


        Integer id = lb.getId();
    	String name = lb.getName();
    	String alg = lb.getAlgorithm().toString();
    	String prot = lb.getProtocol().toString();
    	Integer port = lb.getPort();
        Set<LoadBalancerJoinVip> vips = lb.getLoadBalancerJoinVipSet();
        Set<LoadBalancerJoinVip6> vips6 = lb.getLoadBalancerJoinVip6Set();        
    	Set<Node> nodes = lb.getNodes();
    	HealthMonitor monitor = lb.getHealthMonitor();
		SessionPersistence sp = lb.getSessionPersistence();
		ConnectionThrottle throttle = lb.getConnectionThrottle();

        // If we find an IPv6 address we use that one. If not we use an IPv4 address
        
        NSAdapterUtils.populateNSVIP6(vips6, nsVIP);
        
        if (nsVIP.getId() <= 0)
        {
            NSAdapterUtils.populateNSVIP(vips, nsVIP);
        }
        
        if (nsVIP.getId() <= 0)
        {
            throw new BadRequestException("VirtualIP element of loadbalancer missing....", new Error());
        }     
        
        NSAdapterUtils.populateNSNodes(nodes, nsLB.getNodes());
        
		if(sp != null)
		{
			NSAdapterUtils.populateSessionPersistence(lb.getSessionPersistence(), nsPersistence);
		}
		else
		{
			nsPersistence = null;
		}

    	if (monitor != null) 
    	{
            NSAdapterUtils.populateNSHealthMonitor(monitor, nsMon);
        } else {
            nsMon = null; 
        }

    	if (throttle != null) 
    	{
            NSAdapterUtils.populateConnectionThrottle(throttle, nsThrottle);
        } else {
            nsThrottle = null; 
        }

        nsLB.setId(id);
    	nsLB.setName(name);
        nsLB.setPort(port);
        nsLB.setProtocol(prot);
        nsLB.setAlgorithm(alg);
        nsLB.setVirtualIp(nsVIP);

        if ((nsLB.getNodes() == null) || (nsLB.getNodes().size() == 0))
            nsLB.setNodes(null);

        nsLB.setSessionPersistence(nsPersistence);   
        nsLB.setHealthMonitor(nsMon);   
		nsLB.setConnectionThrottle(nsThrottle);
    }

    static void populateNSVIP(Set<LoadBalancerJoinVip> vips, com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.VirtualIp nsVIP) 
           throws BadRequestException
                 
    {
        if (vips == null)
        {
            nsVIP.setId(-1);
            return;
        }
            
        if (vips.size() > 1)
        {
            throw new BadRequestException("Core adapters can support only one VIP per loadbalancer", new Error());
        }
        
        for (LoadBalancerJoinVip lbjoinVip : vips)
        {
            VirtualIp vip = lbjoinVip.getVirtualIp();

            Integer vipId = vip.getId(); 
            String vipAddress = vip.getAddress();
            IpVersion vipVersion = IpVersion.IPV4;
            VirtualIpType vipType = vip.getVipType();

            nsVIP.setId(vipId);

            if (vipAddress == null)
            {
                throw new BadRequestException("Missing attributes [ipAddress] from virtualIp....", new Error());
            }

    	    nsVIP.setAddress(vipAddress);

            nsVIP.setIpVersion(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.IpVersion.IPV4);

            if (vipType != null)
            {   
                        switch(vipType)
                        { 
                            case PUBLIC:  
		                        nsVIP.setType(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.VirtualIpType.PUBLIC);
                                break;

                            case PRIVATE:  
		                        nsVIP.setType(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.VirtualIpType.PRIVATE);
                                break;

                            default:
                                throw new BadRequestException("Value for attribute [type] of virtualIp not valid....", new Error());                      
                        }
            }
             

            break; // process only the first Virtual IP of a loadbalancer
        }
    }


    static void populateNSVIP6(Set<LoadBalancerJoinVip6> vips6, com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.VirtualIp nsVIP) 
           throws BadRequestException
                 
    {

        if (vips6 == null)
        {
            nsVIP.setId(-1);
            return;
        }   
        
        if (vips6.size() > 1)
        {
            throw new BadRequestException("Core adapters can support only one VIP per loadbalancer", new Error());
        }
            

        for (LoadBalancerJoinVip6 lbjoinVip : vips6)
        {
            VirtualIpv6 vip = lbjoinVip.getVirtualIp();

            Integer vipId = vip.getId(); 
            IpVersion vipVersion = IpVersion.IPV6;
            nsVIP.setId(vipId);



		    nsVIP.setIpVersion(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.IpVersion.IPV6);
		    nsVIP.setType(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.VirtualIpType.PUBLIC);

            try {
                String vipAddress = vip.getDerivedIpString();
                if (vipAddress == null)
                {
                    throw new BadRequestException("Missing attributes [ipAddress] from virtualIp....", new Error());
                }

                nsVIP.setAddress(vipAddress);
            } catch (Exception e) {
                throw new BadRequestException("Cannot convert IPv6 address into a string....", new Error());            
            }        
            break; // process only the first Virtual IP of a loadbalancer.
        }
    }
    
    
    static com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Node translateNode(Node node, boolean forUpdate) 
           throws BadRequestException
    {

	   com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Node nsNode = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Node();

	   Integer nodeid = node.getId();
	   String address = node.getAddress();
	   Integer port = node.getPort();
	   Boolean enabled = node.isEnabled();
	   Integer weight = node.getWeight();

	   if ((address == null) || (port == null))
	   {
		   throw new BadRequestException("Missing attributes [ipAddress, port] from node element....", new Error());
	   }

	   LOG.debug(String.format("node %d: address:%s, port:%d ", nodeid, address, port));

	   if(!forUpdate)
	   {
		   nsNode.setId(nodeid);
		   nsNode.setAddress(address);
		   nsNode.setPort(port);
	   }
	   nsNode.setWeight(weight);

       if (enabled)
       {
			nsNode.setCondition(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.NodeCondition.ENABLED);
       } else {
			nsNode.setCondition(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.NodeCondition.DISABLED);
		}

		return nsNode;	
	}


    static void populateNSNodes(Collection<Node> nodes, List<com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Node> nsNodes) 
           throws BadRequestException
    {

    	if ((nodes != null) && (nodes.size() > 0))
        {
            LOG.debug(String.format("This loadBalancer has got %d nodes", nodes.size()));
			boolean forUpdate = false;
            for (Node node : nodes)
    	    {
               com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Node nsNode = translateNode(node, forUpdate);
               nsNodes.add(nsNode);
            }
    	}
    }

    static void populateNSHealthMonitor(HealthMonitor monitor, com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor nsMon) 
           throws BadRequestException 
    {

        String monType = monitor.getType();
        Integer monDelay = monitor.getDelay();
        Integer monTimeout = monitor.getTimeout();
        Integer monRetries = monitor.getAttemptsBeforeDeactivation();

        if (monType == null)
        {
            throw  new BadRequestException("Missing attributes [type] of healthMonitor....", new Error());
        }

        nsMon.setDelay(monDelay);
        nsMon.setTimeout(monTimeout);
        nsMon.setAttemptsBeforeDeactivation(monRetries);
        

        if (monType == "CONNECT")
        { 
			nsMon.setType(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitorType.CONNECT);
			return; 
        }

        if (monType == "HTTP")
        { 
			nsMon.setType(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitorType.HTTP);
            return;
        }
        
        if (monType == "HTTPS")
        {         
			nsMon.setType(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitorType.HTTPS);
            return;
        }

        throw new BadRequestException("Value for attribute [IpVersion] not valid....", new Error());                      

    }


	static com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer
			getLB(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId) 
           throws AdapterException 
	{
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
		String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId);

        String nsLB = NSAdapterUtils.performRequest("GET", resourceUrl, "");
		return (com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer) NSAdapterUtils.getResponseObject(nsLB);
	}

	static List<com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Node> 
	getAllNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId) 
           throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "nodes";
		String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = NSAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        String nodesAsString = NSAdapterUtils.performRequest("GET", resourceUrl, "");
		com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Nodes nsNodes = (com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Nodes) 
																						NSAdapterUtils.getResponseObject(nodesAsString);
		return nsNodes.getNodes();
	
	}
    static void populateSessionPersistence(SessionPersistence sp, com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence nsSP) 
           throws BadRequestException
    {
		String pt = sp.getPersistenceType();
		
        if (pt == "HTTP_COOKIE")
        { 
			nsSP.setPersistenceType(com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.PersistenceType.HTTP_COOKIE);
            return;
        }
        
        throw new BadRequestException("Value for attribute [PersistenceType] of SessionPersistence is not valid....", new Error());                      
	}


    static void populateConnectionThrottle(ConnectionThrottle throttle, com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.ConnectionThrottle nsThrottle) 
           throws BadRequestException 
    {
		nsThrottle.setRateInterval(throttle.getRateInterval());
		nsThrottle.setMaxRequestRate(throttle.getMaxRequestRate());
    }
}
