package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.SslCipherProfile;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.*;

public interface SslCipherProfileService {

    SslCipherProfile getById(Integer id) throws EntityNotFoundException;

    SslCipherProfile getCipherProfileByLoadBalancerId(Integer lbId) throws EntityNotFoundException;

    void setCipherProfileOnSslTermination(SslTermination sslTermination, String profileName);

    boolean isCipherProfileAvailable(String profileName);

}
