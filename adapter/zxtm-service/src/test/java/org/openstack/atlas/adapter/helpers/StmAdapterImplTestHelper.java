package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.entities.*;

import java.util.*;

import static org.mockito.Mockito.spy;

public class StmAdapterImplTestHelper {

    static String username = "username";
    static String ipv41 = "10.0.0.1";
    static String ipv42 = "10.0.0.2";
    static String ipv43 = "10.0.1.0";
    static String ipv44 = "10.0.1.1";
    static String user = "user";
    static String name = "loadbalancer";
    static String ipv6 = "::";
    static String reason = "because";
    static String comment = "comment";
    static String metaKey = "color";
    static String metaValue = "red";
    static String ticketId = "1234321";
    static String errorPage = "ERROR";
    static String regex = "regex";
    static String path = "path";
    static String header = "header";
    public static final String cert = ""
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIID+TCCAuGgAwIBAgIBATANBgkqhkiG9w0BAQsFADCBpTE2MDQGA1UEAwwtd3d3\n"
            + "LnJhY2tleHAub3JnL2VtYWlsQWRkcmVzcz1yb290QHJhY2tleHAub3JnMRwwGgYD\n"
            + "VQQLExNDbG91ZCBMb2FkQmFsYW5jaW5nMRowGAYDVQQKExFSYWNrc3BhY2UgSG9z\n"
            + "dGluZzEUMBIGA1UEBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQswCQYD\n"
            + "VQQGEwJVUzAeFw0xNDAxMTMxODU4NDZaFw0xODAxMTMxODU4NDZaMIGlMTYwNAYD\n"
            + "VQQDDC13d3cucmFja2V4cC5vcmcvZW1haWxBZGRyZXNzPXJvb3RAcmFja2V4cC5v\n"
            + "cmcxHDAaBgNVBAsTE0Nsb3VkIExvYWRCYWxhbmNpbmcxGjAYBgNVBAoTEVJhY2tz\n"
            + "cGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4\n"
            + "YXMxCzAJBgNVBAYTAlVTMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\n"
            + "hrKPELJaJw2xvQI2qeYhq66qPAHJGV7WQgaN9k3HgFTYePz6SpUC1DORbg1hkxbi\n"
            + "GxIbzuwaRu4AqIyjb48yrkgQb9+1iWLHx1q01LEk/8lMRSXBdzMizs5t58zKOZ6W\n"
            + "dR3ENsGEGAw+sHPzCKVFzi2Y3moLmpVDs6DFzeWA6lZ4XPnGuFYIDhkmeLVVLlBk\n"
            + "MI0vyaQMrwCo6IlBlEvbn9L1Sq7dQYj7Fz/I6vzxdr7qlBbasVT5QfQzOgNIdZCM\n"
            + "OQ+mHUVCs2Q93tdllgCK/AtEbrNMnhlsa/1MvhBmRkvwav01y8dETTpg1zom7ZIV\n"
            + "EcyyitXxg7N+al2DSagbtQIDAQABozIwMDAPBgNVHRMBAf8EBTADAQH/MB0GA1Ud\n"
            + "DgQWBBQ0fOl4Hc6VAMmaJHHi7LogvLHJ4TANBgkqhkiG9w0BAQsFAAOCAQEAgOgc\n"
            + "BYiLRA/q26i0FbbvehXgaKHBnDbjx7aW9bDY6RFY3hcmzHC94+pHatwLR9leamou\n"
            + "3zuqcSujFsClOigubaxxhs6BSA55rDJExDvwWvhDxHl2kV8LOuFu2WqgFjd+WjKE\n"
            + "n0Dp8d3DpGfAl5ruB6V8vJ0GYzP8v33lYP4uI2ZMOBQGfJjfZ1dURTBnxGKQ+/EZ\n"
            + "yTfpEuL+H+p4SDLLYTMzlEc/wR1Y+lUgIRDxDZyLsFKnAcnrLaZ+3Ds7pZMuISU/\n"
            + "caGHLzz7AVetPUYnQdCMa2ReveZdDl04NbENv0SL5z3R924SrjXxkE3FdABBNoVf\n"
            + "fD0ygn3i5Jfn5iHy6Q==\n"
            + "-----END CERTIFICATE-----\n"
            + "";
    private static final String key = ""
            + "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEogIBAAKCAQEAhrKPELJaJw2xvQI2qeYhq66qPAHJGV7WQgaN9k3HgFTYePz6\n"
            + "SpUC1DORbg1hkxbiGxIbzuwaRu4AqIyjb48yrkgQb9+1iWLHx1q01LEk/8lMRSXB\n"
            + "dzMizs5t58zKOZ6WdR3ENsGEGAw+sHPzCKVFzi2Y3moLmpVDs6DFzeWA6lZ4XPnG\n"
            + "uFYIDhkmeLVVLlBkMI0vyaQMrwCo6IlBlEvbn9L1Sq7dQYj7Fz/I6vzxdr7qlBba\n"
            + "sVT5QfQzOgNIdZCMOQ+mHUVCs2Q93tdllgCK/AtEbrNMnhlsa/1MvhBmRkvwav01\n"
            + "y8dETTpg1zom7ZIVEcyyitXxg7N+al2DSagbtQIDAQABAoIBACaA+GB/e0Haqkor\n"
            + "TdD3Vfyrq0IICKJV981b7kzQIw1OM6syNaIIQozCSPo6nP/c9LlkTcqytzg7ZLjJ\n"
            + "qxaCyqwDMZ+pRU2GjTExfqumD7uGoWTKzCj01yhi3gv7AHvEF6JW5fX5D3eIWIs8\n"
            + "Oebox7GOWzqI+dQwgu/gL2sSBe32a7s//G8+Jem4wYAQyQOWfN4xYl8ax6a8oVhZ\n"
            + "xnSX+rwidvtKHuTWQAVvMfmkS2wrCS1t6w1r91ZDEzsaik/4KZSDeclmQz2t2/wg\n"
            + "fulDZUJvLXqcm9EM0/D1aVSI1ZcbeT9lafYWyRL/n3fvJlUgeHg/HINMOr6ZL7qu\n"
            + "WVk6EUECgYEAuo/BP2wmzpYhAapFBhnU8QsF7ITxEICiQCaRnGx9QV+GfnbNOLkz\n"
            + "vPCTha3ivQlsY7fzpmYSppdTyoxzXhOi4H0MKQzN6YUMMeDRN1ZEEq++51dEWiLV\n"
            + "ZtS6hojaR5L30wp7YWXiQeHhJZpdfPzD1bPNlHIaDyeQ46Zqn+qqfc0CgYEAuNUD\n"
            + "gzxoOBOavZHfHgSAQO6edU2J0NelmksmClp4jLGlp2x6HTxsBjX9B+TSo+1Ua+f7\n"
            + "166B9s6rUgvaj83SGd1ze5OYRbIPN4504Vh+NH6iWE/JYFNM6vlwYlFG7RjtS2OI\n"
            + "H9Xo9Nba7uJBLgzO07Rdc0dSR9MogNvAi8HE7YkCgYAEN0Vtsnr6Bih+yiM/LQxD\n"
            + "2jTrEGIGNnH0MyrtNwJpnMgESZj/pCfIKEOdLL3H+dOKOVj3NK1NOHQ7Sos84AK5\n"
            + "QfopX1Gz6j7JTcRMlNV6+p+8yJsR0mGyLjJ84dDU9zxEd6MsI0pGpN0apW9haLDg\n"
            + "Pmzx4tY163X5vwP/0yTBLQKBgH+jEgbpiyrWVvB8MHChORQd9C0bO4WNtZ1kMcPK\n"
            + "kB7Qeo2wem+ADzUGyVJPMSrktHzDuYVKzBLsyyAgcXEG+ATGiX4YAXp3qMx7wrjv\n"
            + "DFAh9ug3SOANBjvpvzMV333RzeRWumUJqhKR4KRAeDRdweIuVq5J3UceSYUau8+H\n"
            + "Iv/JAoGAf9dyE5wW0fehbq9LYzxG+s8VO+EGjdoZgjO1xGsTo6IeLgq17nuIkvhu\n"
            + "NAoyMnaHbJQp1dYGwKBYzrHs6h+RHTBkJ+TOgtMjvOzFCkm5SlDnVe4MJ4FMLwJp\n"
            + "o4lKPbuTPdYY2DFYRl+Yc1guima/WdXznA80lOuxWqqJT4Vy3zg=\n"
            + "-----END RSA PRIVATE KEY-----\n"
            + "";
    static Integer accountId = 13531;
    static Integer securePort = 443;
    static Integer port = 80;
    static Integer id = 0;
    static Integer timeout = 10;
    static Integer weight = 5;
    static Integer maxConnectRate = 10;
    static Integer maxConnections = 10;
    static Integer minConnections = 1;
    static Integer rateInterval = 3;
    static Integer numAttempts = 10;
    static Integer delay = 1;
    static Integer maxRequests = 3;
    static Boolean active = true;
    static Boolean inactive = false;

    public static LoadBalancer generateLoadBalancer() {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setPort(port);
        loadBalancer.setIpv6Public(ipv6);
        loadBalancer.setIpv4Public(ipv41);
        Set<AccessList> accessLists = new HashSet<AccessList>();
        AccessList item = new AccessList();
        item.setUserName(username);
        item.setId(id);
        item.setIpAddress(ipv42);
        item.setType(AccessListType.DENY);
        item.setLoadbalancer(loadBalancer);
        accessLists.add(item);
        loadBalancer.setAccessLists(accessLists);
        loadBalancer.setAccountId(accountId);
        loadBalancer.setAlgorithm(LoadBalancerAlgorithm.ROUND_ROBIN);
        ConnectionLimit limit = new ConnectionLimit();
        limit.setId(id);
        limit.setUserName(username);
        limit.setLoadBalancer(loadBalancer);
        limit.setMaxConnectionRate(maxConnectRate);
        limit.setMaxConnections(maxConnections);
        limit.setMinConnections(minConnections);
        limit.setRateInterval(rateInterval);
        loadBalancer.setConnectionLimit(limit);
        loadBalancer.setConnectionLogging(active);
        loadBalancer.setContentCaching(active);
        loadBalancer.setCreated(Calendar.getInstance());
        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer.setHalfClosed(active);
        HealthMonitor monitor = new HealthMonitor();
        monitor.setUserName(username);
        monitor.setId(id);
        monitor.setAttemptsBeforeDeactivation(numAttempts);
        monitor.setBodyRegex(regex);
        monitor.setDelay(delay);
        monitor.setHostHeader(header);
        monitor.setLoadbalancer(loadBalancer);
        monitor.setStatusRegex(regex);
        monitor.setPath(path);
        monitor.setTimeout(timeout);
        monitor.setType(HealthMonitorType.CONNECT);
        loadBalancer.setHealthMonitor(monitor);
        loadBalancer.setHost(new Host());
        loadBalancer.setName(name);
        Set<Node> nodes = new HashSet<Node>();
        Node node = new Node();
        node.setId(id);
        node.setPort(port);
        node.setLoadbalancer(loadBalancer);
        node.setCondition(NodeCondition.ENABLED);
        node.setIpAddress(ipv43);
        List<NodeMeta> nodeMetadata = new ArrayList<NodeMeta>();
        NodeMeta nodeMeta = new NodeMeta();
        nodeMeta.setKey(metaKey);
        nodeMeta.setNode(node);
        nodeMeta.setValue(metaValue);
        nodeMeta.setId(id);
        nodeMeta.setUserName(username);
        nodeMetadata.add(nodeMeta);
        node.setNodeMetadata(nodeMetadata);
        node.setStatus(NodeStatus.ONLINE);
        node.setType(NodeType.PRIMARY);
        node.setWeight(weight);
        nodes.add(node);
        node = new Node();
        node.setId(id + 1);
        node.setPort(port);
        node.setLoadbalancer(loadBalancer);
        node.setCondition(NodeCondition.ENABLED);
        node.setIpAddress(ipv44);
        nodeMetadata = new ArrayList<NodeMeta>();
        nodeMeta = new NodeMeta();
        nodeMeta.setKey(metaKey);
        nodeMeta.setNode(node);
        nodeMeta.setValue(metaValue);
        nodeMeta.setId(id + 1);
        nodeMeta.setUserName(username);
        nodeMetadata.add(nodeMeta);
        node.setNodeMetadata(nodeMetadata);
        node.setStatus(NodeStatus.ONLINE);
        node.setType(NodeType.PRIMARY);
        node.setWeight(weight);
        nodes.add(node);
        loadBalancer.setNodes(nodes);
        Set<LoadbalancerMeta> lbMetadata = new HashSet<LoadbalancerMeta>();
        LoadbalancerMeta lbMeta = new LoadbalancerMeta();
        lbMeta.setUserName(username);
        lbMeta.setId(id);
        lbMeta.setKey(metaKey);
        lbMeta.setValue(metaValue);
        lbMeta.setLoadbalancer(loadBalancer);
        lbMetadata.add(lbMeta);
        loadBalancer.setLoadbalancerMetadata(lbMetadata);
        loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
        RateLimit limits = new RateLimit();
        limits.setLoadbalancer(loadBalancer);
        limits.setId(id);
        limits.setUserName(username);
        limits.setExpirationTime(Calendar.getInstance());
        limits.setMaxRequestsPerSecond(maxRequests);
        Ticket ticket = new Ticket();
        ticket.setUserName(username);
        ticket.setId(id);
        ticket.setLoadbalancer(loadBalancer);
        ticket.setComment(comment);
        ticket.setTicketId(ticketId);
        limits.setTicket(ticket);
        loadBalancer.setRateLimit(limits);
        loadBalancer.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        SslTermination termination = new SslTermination();
        termination.setId(id);
        termination.setEnabled(active);
        termination.setUserName(username);
        termination.setSecurePort(securePort);
        termination.setCertificate(cert);
        termination.setPrivatekey(key);
        termination.setSecureTrafficOnly(inactive);
        termination.setLoadbalancer(loadBalancer);
        loadBalancer.setSslTermination(termination);
        loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
        loadBalancer.setSticky(inactive);
        Suspension suspension = new Suspension();
        suspension.setUserName(username);
        suspension.setId(id);
        suspension.setLoadbalancer(loadBalancer);
        suspension.setUser(user);
        suspension.setReason(reason);
        suspension.setTicket(ticket);
        loadBalancer.setSuspension(suspension);
        Set<Ticket> tickets = new HashSet<Ticket>();
        tickets.add(ticket);
        loadBalancer.setTickets(tickets);
        loadBalancer.setTimeout(timeout);
        UserPages pages = new UserPages();
        pages.setLoadbalancer(loadBalancer);
        pages.setId(id);
        pages.setUserName(username);
        pages.setErrorpage(errorPage);
        loadBalancer.setUserPages(pages);
        loadBalancer.setId(id);
        loadBalancer.setUserName(username);
        Set<LoadBalancerJoinVip> vipList = spy(new HashSet<LoadBalancerJoinVip>());
        VirtualIp vip = new VirtualIp();
        vip.setId(1234);
        vip.setIpAddress("10.69.0.60");
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        loadBalancerJoinVip.setVirtualIp(vip);
        vipList.add(loadBalancerJoinVip);
        loadBalancer.setLoadBalancerJoinVipSet(vipList);
        return loadBalancer;
    }
}
