package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.SslCipherProfile;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.*;

public interface SslCipherProfileService {

    SslCipherProfile getById(Integer id) throws EntityNotFoundException;

    SslCipherProfile getCipherProfileByLoadBalancerId(Integer lbId) throws EntityNotFoundException;

    SslCipherProfile getCipherProfileForSSLTerminationId(Integer SSLTermId) throws EntityNotFoundException;

    void setCipherProfileToSslTermination(SslTermination sslTermination, String profileName);

    boolean isCipherProfileExists(String profileName);

    void createCipherProfile(SslCipherProfile profile);

    void updateCipherProfile(SslTermination sslTermination) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException, ImmutableEntityException;

    void removeCipherProfile(SslTermination sslTermination) throws EntityNotFoundException;
}
