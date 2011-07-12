package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.CustomLimitAccount;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.CustomLimitAccounts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Limit;
import org.openstack.atlas.service.domain.entities.AccountLimit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DomainToRestModel {
    public static CustomLimitAccounts customLimitAccountsInClusterMapToCustomLimitAccounts(Map<Integer, List<AccountLimit>> customLimitsInCluster) {
        List<Integer> accountIds = new ArrayList<Integer>(customLimitsInCluster.keySet());
        Collections.sort(accountIds);
        CustomLimitAccounts accounts = new CustomLimitAccounts();
        for (Integer accountId : accountIds) {
            CustomLimitAccount account = new CustomLimitAccount();
            account.setAccountId(accountId);
            for (AccountLimit ls : customLimitsInCluster.get(accountId)) {
                Limit limit = new Limit();
                limit.setName(ls.getLimitType().getName().name());
                limit.setValue(ls.getLimit());
                limit.setId(ls.getId());
                account.getCustomLimits().add(limit);
            }
            accounts.getCustomLimitAccounts().add(account);
        }
        return accounts;
    }

}