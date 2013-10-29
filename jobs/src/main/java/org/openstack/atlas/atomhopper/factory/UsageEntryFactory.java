package org.openstack.atlas.atomhopper.factory;

import org.openstack.atlas.atomhopper.exception.AtomHopperMappingException;
import org.openstack.atlas.service.domain.entities.Usage;

import java.util.Map;

public interface UsageEntryFactory {

    Map<Object, Object> createEntry(Usage usage) throws AtomHopperMappingException;

}