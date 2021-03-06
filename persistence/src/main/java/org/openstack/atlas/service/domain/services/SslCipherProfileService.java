package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.SslCipherProfile;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.*;

import java.util.List;

public interface SslCipherProfileService {

    SslCipherProfile getById(Integer id) throws EntityNotFoundException;

    SslCipherProfile getCipherProfileByLoadBalancerId(Integer lbId) throws EntityNotFoundException;

    void setCipherProfileOnSslTermination(SslTermination sslTermination, String profileName);

    boolean isCipherProfileAvailable(String profileName);

    SslCipherProfile create(SslCipherProfile sslCipherProfile) throws BadRequestException;

    SslCipherProfile update(Integer id, SslCipherProfile queueSslCipherProfile) throws BadRequestException, EntityNotFoundException;

    public void deleteSslCipherProfile(SslCipherProfile sslCipherProfile) throws EntityNotFoundException;

    List<SslCipherProfile> fetchAllProfiles() throws EntityNotFoundException;

    SslCipherProfile getByName(String name) throws EntityNotFoundException;
}
