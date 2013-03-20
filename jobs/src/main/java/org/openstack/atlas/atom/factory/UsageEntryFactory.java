package org.openstack.atlas.atom.factory;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.Usage;

import java.util.Map;

public interface UsageEntryFactory {

    Map<Object, Object> createEntry(Usage usage, Configuration configuration, String region) throws AtomHopperMappingException;

}