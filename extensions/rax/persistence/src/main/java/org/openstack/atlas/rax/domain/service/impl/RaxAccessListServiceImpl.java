package org.openstack.atlas.rax.domain.service.impl;

import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.repository.RaxAccessListRepository;
import org.openstack.atlas.rax.domain.service.RaxAccessListService;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.AccountLimitType;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.AccountLimitRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.AccountLimitService;
import org.openstack.atlas.service.domain.service.impl.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class RaxAccessListServiceImpl extends BaseService implements RaxAccessListService {

    @Autowired
    private RaxAccessListRepository accessListRepository;

    @Autowired
    private AccountLimitRepository accountLimitRepository;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Override
    public LoadBalancer updateAccessList(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        RaxLoadBalancer dbLoadBalancer = (RaxLoadBalancer) loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());
        isLbActive(dbLoadBalancer);

        if (loadBalancer instanceof RaxLoadBalancer) {
            RaxLoadBalancer raxLoadBalancer = (RaxLoadBalancer) loadBalancer;
            int total = dbLoadBalancer.getAccessLists().size() + raxLoadBalancer.getAccessLists().size();
            int max = accountLimitRepository.getLimit(dbLoadBalancer.getAccountId(), AccountLimitType.ACCESS_LIST_LIMIT);
            if (total > max) {
                throw new BadRequestException(ErrorMessages.ACCOUNT_LIMIT_REACHED.getMessage("Access List", max));
            }
            if (hasDupeIpInAccessLists(dbLoadBalancer.getAccessLists(), raxLoadBalancer.getAccessLists())) {
                throw new BadRequestException(ErrorMessages.DUPLICATE_ITEMS_FOUND.getMessage("Access List"));
            }
            for (RaxAccessList accessList : raxLoadBalancer.getAccessLists()) {
                dbLoadBalancer.addAccessList(accessList);
            }
            dbLoadBalancer.setStatus(CoreLoadBalancerStatus.PENDING_UPDATE);
            loadBalancerRepository.update(dbLoadBalancer);
        }
        return dbLoadBalancer;
    }

    private boolean hasDupeIpInAccessLists(Set<RaxAccessList>... accessLists) {
        boolean out;
        Set<String> ipSet = new HashSet<String>();
        for (int i = 0; i < accessLists.length; i++) {
            for (RaxAccessList accessList : accessLists[i]) {
                String ip = accessList.getIpAddress();
                if (ipSet.contains(ip)) {
                    out = true;
                    return out;
                }
                ipSet.add(ip);
            }
        }
        out = false;
        return out;
    }
}