package org.openstack.atlas.usage.thread;

import org.openstack.atlas.atomhopper.exception.AtomHopperMappingException;
import org.openstack.atlas.atomhopper.factory.UsageEntryFactory;
import org.openstack.atlas.atomhopper.factory.UsageEntryFactoryImpl;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.auth.IdentityAuthClient;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UsageThread extends AbstractAtomHopperThread {
    private UsageEntryFactory usageEntryFactory;
    private UsageRepository usageRepository;

    public UsageThread(Collection<Usage> usages, AtomHopperClient client, IdentityAuthClient identityAuthClient,
                       UsageRepository usageRepository,
                       LoadBalancerEventRepository loadBalancerEventRepository,
                       AlertRepository alertRepository) {
        super(new ArrayList<Usage>(usages), client, identityAuthClient, loadBalancerEventRepository, alertRepository);
        this.usageRepository = usageRepository;
        this.usageEntryFactory = new UsageEntryFactoryImpl();
    }

    @Override
    public void updatePushedRecords(List<Usage> successfullyPushedRecords) {
        if (!successfullyPushedRecords.isEmpty()) {
            usageRepository.batchUpdate(successfullyPushedRecords, false);
        }
    }

    @Override
    public String getThreadName() {
        return this.getClass().getName();
    }

    @Override
    public Map<Object, Object> generateAtomHopperEntry(Usage usage) throws AtomHopperMappingException {
        return usageEntryFactory.createEntry(usage);
    }
}
