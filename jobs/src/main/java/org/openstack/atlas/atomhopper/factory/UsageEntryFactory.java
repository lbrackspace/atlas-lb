package org.openstack.atlas.atomhopper.factory;

import org.openstack.atlas.atomhopper.exception.AtomHopperMappingException;
import org.openstack.atlas.service.domain.entities.Usage;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public interface UsageEntryFactory {

    UsageEntryWrapper createEntry(Usage usageRecord) throws AtomHopperMappingException;

    UUID genUUIDObject(Usage usageRecord) throws NoSuchAlgorithmException;

}