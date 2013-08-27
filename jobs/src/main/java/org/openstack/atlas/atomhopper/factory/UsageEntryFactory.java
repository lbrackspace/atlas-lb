package org.openstack.atlas.atomhopper.factory;

import org.openstack.atlas.atomhopper.exception.AtomHopperMappingException;
import org.openstack.atlas.service.domain.entities.Usage;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

public interface UsageEntryFactory {

    Map<Object, Object> createEntry(Usage usage) throws AtomHopperMappingException;
    UUID genUUIDObject(Usage usage) throws NoSuchAlgorithmException;
    String genUUIDString(Usage usage) throws AtomHopperMappingException;

}