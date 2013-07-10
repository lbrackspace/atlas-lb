package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusRateLimitedLoadBalancer;
import org.openstack.atlas.lb.helpers.ipstring.IPv4Range;
import org.openstack.atlas.lb.helpers.ipstring.IPv4Ranges;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPBlocksOverLapException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPRangeTooBigException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.ClusterStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdClusterId;
import org.openstack.atlas.service.domain.pojos.VirtualIpAvailabilityReport;
import org.openstack.atlas.service.domain.pojos.VirtualIpBlock;
import org.openstack.atlas.service.domain.pojos.VirtualIpBlocks;
import org.openstack.atlas.service.domain.services.ClusterService;
import org.openstack.atlas.service.domain.services.TicketService;
import org.openstack.atlas.service.domain.services.VirtualIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;

@Service
public class ClusterServiceImpl extends BaseService implements ClusterService {
    private final Log LOG = LogFactory.getLog(ClusterServiceImpl.class);

    @Autowired
    private VirtualIpService virtualIpService;
    @Autowired
    private TicketService ticketService;

    @Override
    public Cluster get(Integer clusterId) throws EntityNotFoundException {
        return clusterRepository.getById(clusterId);
    }

    @Override
    public Cluster getActiveCluster() throws EntityNotFoundException, ClusterStatusException {
        return clusterRepository.getActiveCluster();
    }

    @Override
    public List<Cluster> getAll() {
        return clusterRepository.getAll();
    }

    @Override
    public List<LoadBalancerCountByAccountIdClusterId> getAccountsInCluster(Integer clusterId) {
        return clusterRepository.getAccountsInCluster(clusterId);
    }

    @Override
    public List<Host> getHosts(Integer clusterId) {
        return clusterRepository.getHosts(clusterId);
    }

    @Override
    public List<VirtualIp> getVirtualIps(Integer id, Integer offset, Integer limit) {
        return clusterRepository.getVirtualIps(id, offset, limit);
    }

    @Override
    @Transactional
    public List<ZeusRateLimitedLoadBalancer> getRateLimitedLoadBalancersInCluster(Integer clusterId) throws EntityNotFoundException {
        List<Host> hostList = clusterRepository.getHosts(clusterId);
        List<Integer> lbids = rateLimitRepository.getAllRateLimitedLoadBalancerIds();
        List<LoadBalancer> lblist = new ArrayList();
        List<ZeusRateLimitedLoadBalancer> zeusRateLimitedLoadBalancerList = new ArrayList();
        ZeusRateLimitedLoadBalancer zeusRateLimitedLoadBalancer;
        for (Integer lbId : lbids) {
            lblist.add(loadBalancerRepository.getById(lbId));
        }
        for (LoadBalancer lb : lblist) {
            for (Host host : hostList) {
                if (lb.getHost().getId() == host.getId()) {
                    zeusRateLimitedLoadBalancer = new ZeusRateLimitedLoadBalancer();
                    zeusRateLimitedLoadBalancer.setAccountId(lb.getAccountId());
                    zeusRateLimitedLoadBalancer.setLoadbalancerId(lb.getId());
                    zeusRateLimitedLoadBalancer.setExpirationTime(lb.getRateLimit().getExpirationTime().getTime().toString());
                    zeusRateLimitedLoadBalancer.setTickets(ticketService.customTicketMapper(loadBalancerRepository.getTickets(lb.getId())));
                    zeusRateLimitedLoadBalancerList.add(zeusRateLimitedLoadBalancer);
                }
            }
        }
        return zeusRateLimitedLoadBalancerList;
    }

    @Override
    @Transactional
    public List<AccountGroup> getAPIRateLimitedAccounts(Integer clusterId) throws EntityNotFoundException {
        List<AccountGroup> rateLimitedAccounts = groupRepository.getAllAccounts();
        List<AccountGroup> returnedGroups = new ArrayList();
        for (AccountGroup ag : rateLimitedAccounts) {
            if (clusterRepository.isAccountInCluster(clusterId, ag.getAccountId())) {
                //add to list
                returnedGroups.add(ag);
            }
        }
        return returnedGroups;
    }

    @Override
    public Integer getNumberOfUniqueAccountsForCluster(Integer id) {
        return clusterRepository.getNumberOfUniqueAccountsForCluster(id);
    }

    @Override
    public Integer getNumberOfActiveLoadBalancersForCluster(Integer id) {
        return clusterRepository.getNumberOfActiveLoadBalancersForCluster(id);
    }

    @Override
    public List<VirtualIpAvailabilityReport> getVirtualIpAvailabilityReport(Integer clusterId) {
        return clusterRepository.getVirtualIpAvailabilityReport(clusterId);
    }

    @Override
    @Transactional
    public VirtualIpBlocks addVirtualIpBlocks(VirtualIpBlocks vipBlocks, Integer clusterId) throws BadRequestException, IPStringConversionException, IPBlocksOverLapException, IPRangeTooBigException, IPOctetOutOfRangeException, EntityNotFoundException {
        LOG.debug("Entering " + getClass());

        Set<LoadBalancerJoinVip> vips = new HashSet<LoadBalancerJoinVip>();
        VirtualIp vip;
        IPv4Ranges ranges = new IPv4Ranges();
        VirtualIpType vipType = vipBlocks.getType();
        long ip;
        int lowerOctet;
        Cluster cluster = clusterRepository.getClusterById(clusterId);
        for (VirtualIpBlock ipBlock : vipBlocks.getVirtualIpBlocks()) {
            ranges.add(ipBlock.getFirstIp(), ipBlock.getLastIp());
        } // Will throw an Exception if an IP block is invalid;

        for (IPv4Range range : ranges.getRanges()) {
            for (ip = range.getLo(); ip <= range.getHi(); ip++) {
                vip = new VirtualIp();
                vip.setIpAddress(IPv4ToolSet.long2ip(ip));
                vip.setVipType(vipType);
                vip.setCluster(cluster);
                vip.setAllocated(false);
                if (testForDuplicatesByCluster(vip, clusterId)) {
                    LOG.warn("Duplicate vips detected");
                    throw new BadRequestException(String.format("IP addresses must be unique within a cluster: Ip is duplicated: %s", vip.getIpAddress()));
                }

                LOG.info(String.format("calling persist for %s\n", vip.getIpAddress()));
                virtualIpService.persist(vip);
                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                loadBalancerJoinVip.setVirtualIp(vip);
                vips.add(loadBalancerJoinVip);
            }
        }
        return vipBlocks;
    }

    private boolean testForDuplicatesByCluster(VirtualIp vip, Integer clusterId) {
        List<VirtualIp> dbVips = virtualIpService.getVipsByClusterId(clusterId);
        for (VirtualIp nvip : dbVips) {
            if (vip.getIpAddress().equals(nvip.getIpAddress())) {
                return true;
            }
        }
        return false;
    }
}
