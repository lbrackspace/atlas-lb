package org.openstack.atlas.api.helpers;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.X509Description;
import org.openstack.atlas.service.domain.services.helpers.SslTerminationHelper;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderException;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class StubFactory {

    private static Random rnd = new Random();

    private static final String x509strExample = ""
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIIEBzCCAu+gAwIBAgIBATANBgkqhkiG9w0BAQsFADCBrDE9MDsGA1UEAww0YXBp\n"
            + "LnJhY2tzcGFjZWNsb3VkLmNvbS9lbWFpbEFkZHJlc3M9cm9vdEBub3doZXJlLmNv\n"
            + "bTEcMBoGA1UECxMTQ2xvdWQgTG9hZEJhbGFuY2luZzEaMBgGA1UEChMRUmFja3Nw\n"
            + "YWNlIEhvc3RpbmcxFDASBgNVBAcTC1NhbiBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhh\n"
            + "czELMAkGA1UEBhMCVVMwHhcNMTQwMzEzMjExODExWhcNMjQwMzEwMjExODExWjCB\n"
            + "rDE9MDsGA1UEAww0YXBpLnJhY2tzcGFjZWNsb3VkLmNvbS9lbWFpbEFkZHJlc3M9\n"
            + "cm9vdEBub3doZXJlLmNvbTEcMBoGA1UECxMTQ2xvdWQgTG9hZEJhbGFuY2luZzEa\n"
            + "MBgGA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFDASBgNVBAcTC1NhbiBBbnRvbmlv\n"
            + "MQ4wDAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVMwggEiMA0GCSqGSIb3DQEBAQUA\n"
            + "A4IBDwAwggEKAoIBAQCYT8tuAtvCCIQFixyeYas8OfsfsptV9xYXHzjuoKZ1i48b\n"
            + "i98FRLI07ewEgnZC70IW1txS0tp3eUPYvhZBeySNwnf+ltNBSqieMYrlEelQynSt\n"
            + "/bW/leOFpRpoNM19LGU34fuHkGyxRdfV1dRPnZy1EBYf1gFtT8in9WHHL/67pcBE\n"
            + "aD/EpsFf30/XjMwh8nApo66ujXWb7mxbBCmlE+ijxhBimbRbbkS1Lj1/RyuVBQau\n"
            + "cX+FUnwgrFyOzjwybdOmk2cG8PWKA6700BH9164ksZ0U4eBOPN+C55z/JDZxyNGU\n"
            + "zxKT90B/HfG7GtBH5bCuV2/5eP1r2tWHbztkO2tFAgMBAAGjMjAwMA8GA1UdEwEB\n"
            + "/wQFMAMBAf8wHQYDVR0OBBYEFEF8WZz8J6L0mmIcvU3AX8Nr1Ko4MA0GCSqGSIb3\n"
            + "DQEBCwUAA4IBAQAIyoTMvbFlv/YYdPZTD49L2ByFS2kp7j8yhtVpohHf0vJI6fuD\n"
            + "xj2O1WY+O0B+lUh25eX841gYWeJpKnR3qsLSzFPEn+Zw9nAwt0F0YLfUZddNeNLn\n"
            + "Kb2LY0uk0/MY4uTqlAAHJiiFNQuTvRTCfSWiG9CGHgZxT54B0xLSqSeZMJz28TWJ\n"
            + "RPAhHTV1HRn52HrNDYebM1Y8gywXajBJKHu2j+75SjFphWJNL9QY6SWHFzP2eoyO\n"
            + "niaxErd9DX0WoX+3KE5HPjL487bhKDyzBeTrc+/HXZejvE9H1mp7vVYxZ9nzb+/4\n"
            + "UkqrR7dL9pfouT4OQ1OChGlj17iDzRjIQUsy\n"
            + "-----END CERTIFICATE-----\n"
            + "";


    public static Clusters getClustersDetails() {
        Cluster cluster = new Cluster();
        cluster.setDataCenter(DataCenter.DFW);
        cluster.setDescription("aDesc");
        cluster.setName("aClusterName");

        Cluster cluster2 = new Cluster();
        cluster2.setDataCenter((DataCenter) rndChoice(DataCenter.values()));
        cluster2.setDescription("aDesc");
        cluster2.setName("aSecondCluster");

        Clusters clusters = new Clusters();
        clusters.getClusters().add(cluster);
        clusters.getClusters().add(cluster2);

        return clusters;
    }

    public static VirtualIps getVirtualIpsDetails() {

        VirtualIp vip1 = new VirtualIp();
        vip1.setClusterId(rndInt(0, 1000));
        vip1.setAddress(rndIp());
        vip1.setType((VipType) rndChoice(VipType.values()));
        vip1.setId(rndInt(0, 1000));
        vip1.setLoadBalancerId(rndInt(0, 1000));

        VirtualIp vip2 = new VirtualIp();
        vip2.setClusterId(2);
        vip2.setLoadBalancerId(2);
        vip2.setType(VipType.SERVICENET);
        vip2.setId(1);


        VirtualIps vips = new VirtualIps();
        vips.getVirtualIps().add(vip2);
        vips.getVirtualIps().add(vip1);

        return vips;
    }

    public static VirtualIpAvailabilityReport retrieveRndVipsAvailabilityReport() {
        VirtualIpAvailabilityReport vipar = new VirtualIpAvailabilityReport();

        vipar.setAllocatedPublicIpAddressesInLastSevenDays(rndLong(0, 1000));
        vipar.setAllocatedServiceNetIpAddressesInLastSevenDays(rndLong(0, 1000));
        vipar.setClusterId(rndInt(0, 1000));
        vipar.setClusterName("aClusterName");
        vipar.setFreeAndClearPublicIpAddresses(rndLong(0, 1000));
        vipar.setFreeAndClearServiceNetIpAddresses(rndLong(0, 1000));
        vipar.setPublicIpAddressesAllocatedToday(rndLong(0, 1000));
        vipar.setPublicIpAddressesInHolding(rndLong(0, 1000));
        vipar.setRemainingDaysOfPublicIpAddresses(rndDouble(10.0, 20.0));
        vipar.setRemainingDaysOfServiceNetIpAddresses(rndDouble(10.0, 20.0));
        vipar.setServiceNetIpAddressesAllocatedToday(rndLong(0, 1000));
        vipar.setServiceNetIpAddressesInHolding(rndLong(0, 1000));
        vipar.setTotalPublicIpAddresses(rndLong(0, 1000));
        vipar.setTotalServiceNetAddresses(rndLong(0, 1000));
        return vipar;
    }

    public static VirtualIpAvailabilityReport retrieveVipsAvailabilityReport(int id) {
        VirtualIpAvailabilityReport vipar = new VirtualIpAvailabilityReport();

        vipar.setAllocatedPublicIpAddressesInLastSevenDays(rndLong(0, 1000));
        vipar.setAllocatedServiceNetIpAddressesInLastSevenDays(rndLong(0, 1000));
        vipar.setClusterId(id);
        vipar.setClusterName("aClusterName");
        vipar.setFreeAndClearPublicIpAddresses(rndLong(0, 1000));
        vipar.setFreeAndClearServiceNetIpAddresses(rndLong(0, 1000));
        vipar.setPublicIpAddressesAllocatedToday(rndLong(0, 1000));
        vipar.setPublicIpAddressesInHolding(rndLong(0, 1000));
        vipar.setRemainingDaysOfPublicIpAddresses(rndDouble(10.0, 20.0));
        vipar.setRemainingDaysOfServiceNetIpAddresses(rndDouble(10.0, 20.0));
        vipar.setServiceNetIpAddressesAllocatedToday(rndLong(0, 1000));
        vipar.setServiceNetIpAddressesInHolding(rndLong(0, 1000));
        vipar.setTotalPublicIpAddresses(rndLong(0, 1000));
        vipar.setTotalServiceNetAddresses(rndLong(0, 1000));
        return vipar;
    }

    public static VirtualIpAvailabilityReports retrieveRndVipsAvailabilityReports(int max) {
        VirtualIpAvailabilityReports vipars = new VirtualIpAvailabilityReports();
        int nreports = max;
        for (int i = nreports; i >= 0; i--) {
            VirtualIpAvailabilityReport vipar = retrieveRndVipsAvailabilityReport();
            vipars.getVirtualIpAvailabilityReports().add(vipar);
        }
        return vipars;
    }

    public static VirtualIpAvailabilityReports retrieveVipsAvailabilityReports(int id) {
        VirtualIpAvailabilityReports vipars = new VirtualIpAvailabilityReports();
        int viparid = id;
        VirtualIpAvailabilityReport vipar = retrieveVipsAvailabilityReport(viparid);
        vipars.getVirtualIpAvailabilityReports().add(vipar);
        return vipars;
    }

    public static VirtualIp getClusterVirtualIpDetails(int id, int lbid, int cid, String address, IpVersion version, VipType vipType) {
        VirtualIp vip = new VirtualIp();

        vip.setId(id);
        vip.setLoadBalancerId(lbid);
        vip.setClusterId(cid);
        vip.setAddress(address);
        vip.setType(vipType);
        return vip;
    }

    public static Suspension getLoadBalancerSuspensionDetails(String reason, int ticketId, String user) {
        Suspension suspension = new Suspension();

        Ticket ticket = new Ticket();
        ticket.setTicketId("1234");
        ticket.setComment("My first ticket! Yuppee!");

        suspension.setReason(reason);
        suspension.setTicket(ticket);
        suspension.setUser(user);
        return suspension;
    }

    public static Backup getLoadbalancerBackupDetails() {
        return new Backup();
    }

    public static Host getHostDetails() {
        return new Host();
    }

    public static VirtualIp getLoadbalancerVirtualIpDetails(int id, int loadBalancerId, int clusterId, String address, VipType vipType) {
        VirtualIp vip = new VirtualIp();
        VirtualIp vip2 = new VirtualIp();

        vip.setId(id);
        vip.setLoadBalancerId(loadBalancerId);
        vip.setClusterId(clusterId);
        vip.setAddress(address);
        vip.setType(vipType);

        vip2.setType(vipType.PUBLIC);
        vip2.setLoadBalancerId(1);
        vip2.setClusterId(1);
        vip2.setAddress("127.0.0.1");
        vip2.setId(1);

        return vip;
    }

    public static RateLimit getRateLimitDetails(int ticketId, int maxRequestsPerSecond) throws ParseException {

        RateLimit rl = new RateLimit();

        Ticket ticket = new Ticket();
        ticket.setTicketId("1234");
        ticket.setComment("My first ticket! Yuppee!");

        rl.setExpirationTime(Calendar.getInstance());
        rl.setMaxRequestsPerSecond(ticketId);
        rl.setTicket(ticket);
        return rl;
    }

    public static Integer rndInt(int lo, int hi) {
        int ri = rnd.nextInt();
        ri = ri < 0 ? 0 - ri : ri;
        return new Integer(ri % (hi - lo + 1) + lo);
    }

    public static Long rndLong(int lo,int hi) {
        return new Long(rndInt(lo,hi));
    }

    public static String rndIp() {
        String out = String.format("%s.%s.%s.%s", rndInt(0, 255), rndInt(0, 255), rndInt(0, 255), rndInt(0, 255));
        return out;
    }

    public static Double rndDouble(double lo, double hi) {
        double d = rnd.nextDouble();
        return (Double) (hi - lo) * d + lo;
    }

    public static Object rndChoice(List oList) {
        int ri = rndInt(0, oList.size() - 1);
        return oList.get(ri);
    }

    public static Object rndChoice(Object[] oArray) {
        int ri = rndInt(0, oArray.length - 1);
        return oArray[ri];
    }

    public static Backup rndBackup() {
        Backup b = new Backup();
        int ri = rndInt(0, 10000);
        b.setName(String.format("Backup.%d", ri));
        b.setId(ri);
        b.setBackupTime(Calendar.getInstance());
        return b;
    }

    public static Backups rndBackups() {
        Backups backups = new Backups();
        int nbackups = rndInt(1, 5);
        for (int i = 0; i <= nbackups; i++) {
            backups.getBackups().add(rndBackup());
        }
        return backups;

    }

    public static HostMachineDetails newHostMachineDetails(){
        HostMachineDetails hmd = new HostMachineDetails();
        hmd.setActiveLBConfigurations(new Long(5));
        hmd.setAvailableConcurrentConnections(20);
        hmd.setCurrentUtilization("12%");
        Host host = rndHostPost();
        hmd.setHost(host);
        hmd.setTotalConcurrentConnections(25);
        hmd.setUniqueCustomers(3);
        return hmd;
    }

    public static HostCapacityReport rndHostCapacityReport() {
        HostCapacityReport h = new HostCapacityReport();
        h.setAllocatedConcurrentConnections(rndInt(0, 10000));
        h.setAllocatedConcurrentConnectionsInLastSevenDays(rndInt(0, 250));
        h.setAllocatedConcurrentConnectionsToday(rndInt(0, 1000));
        h.setAvailableConcurrentConnections(rndInt(0, 64));
        h.setHostId(rndInt(0, 10000));
        h.setHostName(String.format("Host.%d", h.getHostId()));
        h.setRemainingDaysOfCapacity(rndDouble(0, 31));
        h.setTotalConcurrentConnectionCapacity(rndInt(0, 400));
        return h;
    }

    public static Host rndHostPut() {
        Host h;
        h = new Host();

        h.setName(String.format("Host.%s", rndInt(0, 10000)));
        return h;
    }

    public static HostCapacityReports rndHostCapacityReports(int max) {
        HostCapacityReports hs = new HostCapacityReports();
        int nreports = rndInt(1, max);
        for (int i = nreports; i >= 0; i--) {
            HostCapacityReport h = rndHostCapacityReport();
            hs.getHostCapacityReports().add(h);
        }
        return hs;
    }

    public static Host rndHostPost() {
        Host h;
        h = new Host();

        h.setName(String.format("Host.%s", rndInt(0, 10000)));
        h.setClusterId(rndInt(0, 10000));
        h.setCoreDeviceId(rndInt(0, 10000).toString());
        h.setStatus((HostStatus) rndChoice(HostStatus.values()));
        h.setManagementIp(rndIp());
        h.setManagementSoapInterface(String.format("http://%s:8080/soap", rndIp()));
        h.setManagementRestInterface(String.format("http://%s:8080/rest", rndIp()));
        h.setMaxConcurrentConnections(rndInt(0, 900));
        h.setType((HostType) rndChoice(HostType.values()));
        return h;
    }

    public static ConnectionThrottle rndConnectionThrottle() {
        ConnectionThrottle cl = new ConnectionThrottle();
        cl.setMaxConnectionRate(rndInt(0, 1000));
        cl.setMaxConnections(rndInt(0, 256));
        cl.setMinConnections(rndInt(0, 24));
        cl.setMaxConnectionRate(rndInt(0, 32));
        return cl;
    }

    public static LoadBalancerUsageRecord rndCurrentUsage() {
        LoadBalancerUsageRecord usage = new LoadBalancerUsageRecord();
        usage.setIncomingTransfer((long) rndInt(0, 1000000000));
        usage.setOutgoingTransfer((long) rndInt(0, 1000000000));
        return usage;
    }

    public static Hosts rndHosts(int max) {
        int nhosts = rndInt(0, max);
        Hosts hosts = new Hosts();
        hosts.setSticky((Boolean) StubFactory.rndChoice((new Boolean[]{true, false})));
        for (int i = nhosts; i <= StubFactory.rndInt(1, 5); i++) {
            Host h = StubFactory.rndHostPost();
            h.setId(rndInt(0, 10000));
            hosts.getHosts().add(h);

        }
        return hosts;
    }

    public static Node rndNode() {
        Node n = new Node();
        n.setCondition((NodeCondition) rndChoice(NodeCondition.values()));
        n.setId(rndInt(0, 10000));
        n.setAddress(rndIp());
        n.setPort(rndInt(1, 32000));
        n.setStatus((NodeStatus) rndChoice(NodeStatus.values()));
        return n;
    }

    public static Nodes rndNodes(int max) {
        int nnodes = rndInt(0, 5);
        Nodes ns = new Nodes();
        for (int i = nnodes; i >= 0; i--) {
            Node n = rndNode();
            ns.getNodes().add(n);
        }
        return ns;
    }

    public static RateLimit rndRateLimit() {
        Ticket ticket = new Ticket();
        ticket.setTicketId("1234");
        ticket.setComment("My first ticket! Yuppee!");

        RateLimit rl = new RateLimit();
        rl.setExpirationTime(Calendar.getInstance());
        rl.setMaxRequestsPerSecond(rndInt(0, 60));
        rl.setTicket(ticket);
        return rl;
    }

    public static SessionPersistence rndSessionPersistance() {
        SessionPersistence sp = new SessionPersistence();
        sp.setPersistenceType((PersistenceType) rndChoice(PersistenceType.values()));
        return sp;
    }

    public static LoadBalancer rndLoadBalancer() {
        int ri = rndInt(0, 10000);
        LoadBalancer l = new LoadBalancer();
        l.setAlgorithm((String) rndChoice(AlgorithmType.values()));
        org.openstack.atlas.docs.loadbalancers.api.v1.Cluster c;
        c = new org.openstack.atlas.docs.loadbalancers.api.v1.Cluster();
        c.setName(String.format("Cluster for LB.%d", ri));
        l.setCluster(c);
        ConnectionThrottle cl = rndConnectionThrottle();
        l.setConnectionThrottle(cl);
        ConnectionLogging conLog = new ConnectionLogging();
        conLog.setEnabled(true);
        l.setConnectionLogging(conLog);
        Created created = new Created();
        created.setTime(Calendar.getInstance());
        l.setCreated(created);
//        l.getCurrentUsage().add(rndCurrentUsage());
//        l.setHosts(rndHosts(3));
        l.setId(rndInt(0, 10000));
        l.setName(String.format("LB.%d", l.getId()));
        l.getNodes().addAll(StubFactory.rndNodes(10).getNodes());
        l.setPort(rndInt(0, 32000));
        List<String> protocolNames = ProtocolPortBindings.getKeys();
        l.setProtocol((String)rndChoice(protocolNames));
        l.setRateLimit(rndRateLimit());
        l.setSessionPersistence(rndSessionPersistance());
        l.setStatus((String) rndChoice(LoadBalancerStatus.values()));
        l.setTotalActiveConnections(rndInt(0, 10000));
        return l;
    }

    public static VirtualIpBlock getVirtualIpBlock(String lo, String hi) {
        VirtualIpBlock virtualIpBlock = new VirtualIpBlock();
        virtualIpBlock.setFirstIp(lo);
        virtualIpBlock.setLastIp(hi);
        return virtualIpBlock;
    }

    public static VirtualIpBlocks getVirtualIpBlocks() {
        VirtualIpBlocks virtualIpBlocks = new VirtualIpBlocks();
        virtualIpBlocks.setType(VipType.SERVICENET);
        virtualIpBlocks.getVirtualIpBlocks().add(getVirtualIpBlock("10.0.0.5", "10.0.0.253"));
        virtualIpBlocks.getVirtualIpBlocks().add(getVirtualIpBlock("172.16.0.64", "172.16.0.128"));
        virtualIpBlocks.getVirtualIpBlocks().add(getVirtualIpBlock("192.168.0.3", "192.168.0.100"));
        return virtualIpBlocks;
    }

    public static NetInterface newNetInterface(String name, String... blocks) {
        int i;
        NetInterface iface = new NetInterface();
        Cidr cidr;
        iface.setName(name);
        for (i = 0; i < blocks.length; i++) {
            cidr = new Cidr();
            cidr.setBlock(blocks[i]);
            iface.getCidrs().add(cidr);
        }
        return iface;
    }

    public static X509Description newX509Description() throws X509ReaderException {
        X509Description x509des = SslTerminationHelper.toX509Description(x509strExample);
        return x509des;
    }


}
