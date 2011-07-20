package org.openstack.atlas.api.resources;

import java.net.URI;
import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.SourceAddresses;
import java.util.Calendar;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import java.util.ArrayList;
import org.openstack.atlas.docs.loadbalancers.api.v1.Cluster;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeStatus;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.docs.loadbalancers.api.v1.Updated;

import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;

import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import java.util.List;


import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.ws.rs.core.Response;

public class StubResource extends CommonDependencyProvider {

    @GET
    @Path("loadbalancers")
    public Response stubLoadBalancers() {
        LoadBalancers loadbalancers = new LoadBalancers();
        loadbalancers.getLoadBalancers().add(newLoadBalancer(1, "LB1"));
        loadbalancers.getLoadBalancers().add(newLoadBalancer(2, "LB2"));
        return Response.status(200).entity(loadbalancers).build();
    }

    @GET
    @Path("loadbalancer")
    public Response stubLoadBalancer() {
        LoadBalancer lb = newLoadBalancer(1, "LB1");
        return Response.status(200).entity(lb).build();
    }

    @GET
    @Path("virtualip")
    public Response stubVirtualIp() {
        VirtualIp virtualIp = newVip(1, "127.0.0.1");
        return Response.status(200).entity(virtualIp).build();
    }

    @GET
    @Path("virtualips")
    public Response stubVirtualIps() {
        VirtualIps vips = new VirtualIps();
        vips.getVirtualIps().add(newVip(1, "127.0.0.1"));
        vips.getVirtualIps().add(newVip(2, "127.0.0.2"));
        return Response.status(200).entity(vips).build();
    }

    @GET
    @Path("connectionthrottle")
    public Response stubConnectionThrottle() {
        ConnectionThrottle ct;
        ct = new org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle();
        ct.setMaxConnectionRate(100);
        ct.setMaxConnections(200);
        ct.setMinConnections(300);
        ct.setRateInterval(60);
        return Response.status(200).entity(ct).build();
    }

    @GET
    @Path("node")
    public Response stubNode() {
        Node node;
        node = newNode(64, 80, "127.0.0.1");
        return Response.status(200).entity(node).build();
    }

    @GET
    @Path("uri")
    public Response uriInfo(){
        String uri = getRequestStateContainer().getUriInfo().getAbsolutePath().toString();
        SourceAddresses sa = new SourceAddresses();
        sa.setIpv4Public(uri);
        return Response.status(200).entity(sa).build();
    }

    @GET
    @Path("healthmonitor")
    public Response stubHealthMonitor() {
        HealthMonitor hm;
        hm = new HealthMonitor();
        hm.setAttemptsBeforeDeactivation(10);
        hm.setBodyRegex(".*");
        hm.setDelay(60);
        hm.setId(64);
        hm.setPath("/");
        hm.setStatusRegex(".*");
        hm.setTimeout(100);
        hm.setType(HealthMonitorType.HTTP);
        return Response.status(200).entity(hm).build();
    }

    @GET
    @Path("sessionpersistence")
    public Response stubSessionPersistence() {
        SessionPersistence sp;
        sp = new SessionPersistence();
        sp.setPersistenceType(PersistenceType.HTTP_COOKIE);
        return Response.status(200).entity(sp).build();
    }

    @GET
    @Path("connectionlogging")
    public Response stubConnectionLogging() {
        ConnectionLogging cl;
        cl = new ConnectionLogging();
        cl.setEnabled(Boolean.TRUE);
        return Response.status(200).entity(cl).build();
    }

    @GET
    @Path("nodes")
    public Response stubNodes() {
        List<Node> nodeList;
        Nodes nodes;
        Node node;
        nodes = new Nodes();
        nodeList = nodes.getNodes();
        nodeList.add(newNode(64, 80, "127.0.0.1"));
        nodeList.add(newNode(64, 443, "127.0.0.2"));
        return Response.status(200).entity(nodes).build();
    }

    @GET
    @Path("accesslist")
    public Response stubAccessList() {
        AccessList al = new AccessList();
        al.getNetworkItems().add(newNetworkItem(1, "10.0.0.0/8"));
        al.getNetworkItems().add(newNetworkItem(2, "192.168.0.0/24"));
        return Response.status(200).entity(al).build();
    }

    @GET
    @Path("updated")
    public Response stubUpdated() {
        Calendar now = Calendar.getInstance();
        Updated updated = new Updated();
        updated.setTime(now);
        return Response.status(200).entity(updated).build();
    }

    private Node newNode(Integer id, Integer port, String address) {
        Node node;
        node = new Node();
        node.setAddress(address);
        node.setCondition(NodeCondition.ENABLED);
        node.setId(id);
        node.setPort(port);
        node.setStatus(NodeStatus.ONLINE);
        node.setWeight(1);
        return node;
    }

    private VirtualIp newVip(Integer id, String address) {
        VirtualIp vip;
        vip = new VirtualIp();
        vip.setId(id);
        vip.setAddress(address);
        vip.setIpVersion(IpVersion.IPV4);
        vip.setType(VipType.PUBLIC);
        return vip;
    }

    private NetworkItem newNetworkItem(Integer id, String address) {
        NetworkItem n = new NetworkItem();
        n.setId(id);
        n.setAddress(address);
        n.setIpVersion(IpVersion.IPV4);
        n.setType(NetworkItemType.DENY);
        return n;
    }

    private LoadBalancer newLoadBalancer(Integer id, String name) {
        List<Node> nodes = new ArrayList<Node>();
        List<VirtualIp> vips = new ArrayList<VirtualIp>();
        List<NetworkItem> accessList = new ArrayList<NetworkItem>();
        LoadBalancer lb = new LoadBalancer();
        Created created = new Created();
        Updated updated = new Updated();
        created.setTime(Calendar.getInstance());
        updated.setTime(Calendar.getInstance());
        ConnectionThrottle ct = new ConnectionThrottle();
        Cluster cl = new Cluster();
        ConnectionLogging cnl = new ConnectionLogging();
        cnl.setEnabled(Boolean.TRUE);
        ct.setMaxConnectionRate(100);
        ct.setMaxConnections(200);
        ct.setMinConnections(300);
        ct.setRateInterval(60);
        cl.setName("TestCluster");
        lb.setName(name);
        lb.setAlgorithm("RANDOM");
        lb.setCluster(cl);
        lb.setConnectionLogging(cnl);
        lb.setConnectionThrottle(ct);
        lb.setPort(80);
        lb.setProtocol("HTTP");
        lb.setStatus("BUILD");
        lb.setCreated(created);
        lb.setUpdated(updated);
        nodes.add(newNode(1, 80, "127.0.0.10"));
        nodes.add(newNode(1, 443, "127.0.0.20"));
        vips.add(newVip(1, "127.0.0.1"));
        vips.add(newVip(2, "127.0.0.2"));
        lb.setVirtualIps(vips);
        lb.setNodes(nodes);
        SessionPersistence sp = new SessionPersistence();
        sp.setPersistenceType(PersistenceType.HTTP_COOKIE);
        lb.setSessionPersistence(sp);
        accessList.add(newNetworkItem(1, "10.0.0.0/8"));
        accessList.add(newNetworkItem(2, "192.168.0.0/24"));
        lb.setAccessList(accessList);
        LoadBalancerUsage lu = new LoadBalancerUsage();
        lu.setLoadBalancerId(id);
        lu.setLoadBalancerName(name);
        lu.getLoadBalancerUsageRecords().add(newLoadBalancerUsageRecord(1));
        lu.getLoadBalancerUsageRecords().add(newLoadBalancerUsageRecord(2));
        lb.setLoadBalancerUsage(lu);
        return lb;
    }

    private LoadBalancerUsageRecord newLoadBalancerUsageRecord(Integer id){
        LoadBalancerUsageRecord ur = new LoadBalancerUsageRecord();
        ur.setAverageNumConnections(3.0);
        ur.setId(id);
        ur.setEventType("EmptyEvent");
        ur.setIncomingTransfer(new Long(20));
        ur.setNumPolls(50);
        ur.setNumVips(30);
        ur.setOutgoingTransfer(new Long(30));
        ur.setEndTime(Calendar.getInstance());
        return ur;
    }

}
