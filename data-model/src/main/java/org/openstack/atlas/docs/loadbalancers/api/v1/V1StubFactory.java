package org.openstack.atlas.docs.loadbalancers.api.v1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import javax.xml.namespace.QName;
import org.w3.atom.Link;

public class V1StubFactory {

    private static Random rnd;

    static {
        rnd = new Random();
    }

    public static VirtualIp newVirtualIp() {
        VirtualIp vip = new VirtualIp();
        vip.setAddress(newIPv4Addr());
        vip.setId(rnd.nextInt(4096));
        vip.setType(rndChoice(VipType.values()));
        vip.setIpVersion(rndChoice(IpVersion.values()));
        return vip;
    }

    public static <E> E rndChoice(E[] list) {
        E chosen = list[rnd.nextInt(list.length)];
        return chosen;
    }

    public static VirtualIps newVirtualIps(int nVips, int nLinks) {
        VirtualIps vips = new VirtualIps();
        for (int i = 0; i < nVips; i++) {
            vips.getVirtualIps().add(newVirtualIp());
        }
        for (int i = 0; i < nLinks; i++) {
            vips.getLinks().add(newAtomLink());
        }
        return vips;
    }

    public static Link newAtomLink() {
        String ri = Integer.toString(rnd.nextInt(4096));
        Link atomLink = new Link();
        atomLink.setBase("SomeBase_" + ri);
        atomLink.setContent("SomeContent_" + ri);
        atomLink.setHref("SomeHref_" + ri);
        atomLink.setHreflang("SomeLangRef_" + ri);
        atomLink.setLang("SomeLang_" + ri);
        atomLink.setLength(new BigInteger("10000000000000000000"));
        atomLink.setRel("somRel_" + ri);
        atomLink.setTitle("someTitle_" + ri);
        atomLink.setType("someType");
        atomLink.getOtherAttributes().put(new QName("NSURI", "SomelocalPart", "somePrefix"), "SomeAttr_" + ri);
        atomLink.getOtherAttributes().put(new QName("anotherNSURI", "anotherLocalPart", "AnoterPrefix"), "AnotherAttr_" + ri);
        return atomLink;
    }

    public static String newIPv4Addr() {
        return Integer.toString(rnd.nextInt(256)) + "."
                + Integer.toString(rnd.nextInt(256)) + "."
                + Integer.toString(rnd.nextInt(256)) + "."
                + Integer.toString(rnd.nextInt(256)) + ".";

    }

    public static Node newNode(Integer id, Integer port, String address) {
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

    public static VirtualIp newVip(Integer id, String address) {
        VirtualIp vip;
        vip = new VirtualIp();
        vip.setId(id);
        vip.setAddress(address);
        vip.setIpVersion(IpVersion.IPV4);
        vip.setType(VipType.PUBLIC);
        return vip;
    }

    public static NetworkItem newNetworkItem(Integer id, String address) {
        NetworkItem n = new NetworkItem();
        n.setId(id);
        n.setAddress(address);
        n.setIpVersion(rndChoice(IpVersion.values()));
        n.setType(rndChoice(NetworkItemType.values()));
        return n;
    }

    public static LoadBalancer newLoadBalancer(Integer id, String name) {
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
        lb.setVirtualIps(new VirtualIps());
        lb.getVirtualIps().getVirtualIps().addAll(vips);
        lb.getVirtualIps().getLinks().add(makeLink("http://virtualIpLink", "self"));
        lb.setNodes(new Nodes());
        lb.getNodes().getNodes().addAll(nodes);
        lb.getNodes().getLinks().add(makeLink("http://nodesLink", "self"));
        SessionPersistence sp = new SessionPersistence();
        sp.setPersistenceType(PersistenceType.HTTP_COOKIE);
        lb.setSessionPersistence(sp);
        accessList.add(newNetworkItem(1, "10.0.0.0/8"));
        accessList.add(newNetworkItem(2, "192.168.0.0/24"));
        lb.setAccessList(new AccessList());
        lb.getAccessList().getNetworkItems().addAll(accessList);
        lb.getAccessList().getLinks().add(makeLink("http://AccessListLink", "self"));
        LoadBalancerUsage lu = new LoadBalancerUsage();
        lu.setLoadBalancerId(id);
        lu.setLoadBalancerName(name);
        lu.getLoadBalancerUsageRecords().add(newLoadBalancerUsageRecord(1));
        lu.getLoadBalancerUsageRecords().add(newLoadBalancerUsageRecord(2));
        lb.setLoadBalancerUsage(lu);
        return lb;
    }

    public static LoadBalancerUsageRecord newLoadBalancerUsageRecord(Integer id) {
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

    public static Link makeLink(String href, String rel) {
        Link link = new Link();
        link.setHref(href);
        link.setRel(rel);
        return link;
    }
}
