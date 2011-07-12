package org.openstack.atlas.api.mgmt.repository;

import org.openstack.atlas.api.mgmt.validation.validators.*;
import org.openstack.atlas.api.validation.validators.ResourceValidator;

import java.util.HashMap;
import java.util.Map;

public final class ValidatorRepository {

    private static final Map<Class, ResourceValidator> classKeyedValidatorMap = new HashMap<Class, ResourceValidator>();

    public static <R> ResourceValidator<R> getValidatorFor(Class<R> classOfObjectToValidate) {
        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Backup.class, new BackupValidator());
        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Blacklist.class, new BlacklistValidator());
        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem.class, new BlacklistItemValidator());

        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster.class, new ClusterValidator());
        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Clusters.class, new ClustersValidator());

        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Host.class, new HostValidator());
        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts.class, new HostsValidator());

        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Limit.class, new AccountLimitValidator());
        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerLimitGroup.class, new LoadBalancerLimitGroupValidator());
        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerLimitGroups.class, new LoadBalancerLimitGroupsValidator());

        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers.class, new LoadBalancersValidator());

        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit.class, new RateLimitValidator());

        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension.class, new LoadBalancerSuspensionValidator());

        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Tickets.class, new TicketsValidator());
        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket.class, new TicketValidator());

        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp.class, new VirtualIpValidator());
        classKeyedValidatorMap.put(org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIps.class, new VirtualIpsValidator());


        if (!classKeyedValidatorMap.containsKey(classOfObjectToValidate)) {
            throw new NullPointerException(String.format("No Validator registered in repository for Class: %s", classOfObjectToValidate.getName()));
        }
        return classKeyedValidatorMap.get(classOfObjectToValidate);
    }
}
