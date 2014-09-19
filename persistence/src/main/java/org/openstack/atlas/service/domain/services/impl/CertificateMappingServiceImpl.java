package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojos.SslDetails;
import org.openstack.atlas.service.domain.services.CertificateMappingService;
import org.openstack.atlas.service.domain.services.helpers.SslTerminationHelper;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class CertificateMappingServiceImpl extends BaseService implements CertificateMappingService {
    private final Log LOG = LogFactory.getLog(CertificateMappingServiceImpl.class);
    private static final ZeusUtils zeusUtils;

    static {
        zeusUtils = new ZeusUtils();
    }

    // TODO: Perhaps add AOP for ensureSslTerminationConfigIsAvailable since it is called "before" every method.

    @Override
    public CertificateMapping create(LoadBalancer lb) throws UnprocessableEntityException, EntityNotFoundException, BadRequestException {
        ensureSslTerminationConfigIsAvailable(lb.getId());
        List<CertificateMapping> dbCertificateMappings = certificateMappingRepository.getAllForLoadBalancerId(lb.getId());
        CertificateMapping newMapping = lb.getCertificateMappings().iterator().next();

        detectDuplicateHostName(dbCertificateMappings, newMapping);
        validateCertificateMapping(newMapping);

        return certificateMappingRepository.save(newMapping, lb.getId());
    }

    @Override
    public List<CertificateMapping> getAllForLoadBalancerId(Integer lbId) throws EntityNotFoundException {
        ensureSslTerminationConfigIsAvailable(lbId);
        return certificateMappingRepository.getAllForLoadBalancerId(lbId);
    }

    @Override
    public CertificateMapping getByIdAndLoadBalancerId(Integer id, Integer lbId) throws EntityNotFoundException {
        ensureSslTerminationConfigIsAvailable(lbId);
        return certificateMappingRepository.getByIdAndLoadBalancerId(id, lbId);
    }

    @Override
    public void update(LoadBalancer messengerLb) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException {
        ensureSslTerminationConfigIsAvailable(messengerLb.getId());
        LoadBalancer dbLb = loadBalancerRepository.getByIdAndAccountId(messengerLb.getId(), messengerLb.getAccountId());
        Set<CertificateMapping> dbCertMappings = dbLb.getCertificateMappings();

        CertificateMapping certificateMappingToUpdate = messengerLb.getCertificateMappings().iterator().next();
        if (!loadBalancerContainsMapping(dbLb, certificateMappingToUpdate)) {
            LOG.debug("Certificate mapping to update not found. Sending response to client...");
            throw new EntityNotFoundException(Constants.CertificateMappingNotFound);
        }

        detectDuplicateHostName(dbCertMappings, certificateMappingToUpdate);

        LOG.debug("Certificate mappings on dbLoadBalancer: " + dbCertMappings.size());
        for (CertificateMapping dbCertMapping : dbCertMappings) {
            if (dbCertMapping.getId().equals(certificateMappingToUpdate.getId())) {
                LOG.info("Certificate mapping to be update found: " + dbCertMapping.getId());
                if (certificateMappingToUpdate.getPrivateKey() != null) {
                    dbCertMapping.setPrivateKey(certificateMappingToUpdate.getPrivateKey());
                }
                if (certificateMappingToUpdate.getCertificate() != null) {
                    dbCertMapping.setCertificate(certificateMappingToUpdate.getCertificate());
                }
                if (certificateMappingToUpdate.getIntermediateCertificate() != null) {
                    dbCertMapping.setIntermediateCertificate(certificateMappingToUpdate.getIntermediateCertificate());
                }
                if (certificateMappingToUpdate.getHostName() != null) {
                    dbCertMapping.setHostName(certificateMappingToUpdate.getHostName());
                }

                validateCertificateMapping(dbCertMapping);
                break;
            }
        }

        certificateMappingRepository.update(dbLb);
    }

    @Override
    public void prepareForDelete(Integer id, Integer loadBalancerId) {
        // TODO: Implement
    }

    @Transactional
    @Override
    public void deleteByIdAndLoadBalancerId(Integer id, Integer lbId) throws EntityNotFoundException {
        ensureSslTerminationConfigIsAvailable(lbId);
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(lbId);
        certificateMappingRepository.delete(dbLoadBalancer, id);
    }

    private void ensureSslTerminationConfigIsAvailable(Integer lbId) throws EntityNotFoundException {
        try {
            sslTerminationRepository.getSslTerminationByLbId(lbId);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(Constants.SslTerminationNotFound);
        }
    }

    private void detectDuplicateHostName(Collection<CertificateMapping> dbCertificateMappings, CertificateMapping mappingToCheck) throws UnprocessableEntityException {
        CertificateMapping dbMappingWithHostName = getMappingWithDuplicateHostName(mappingToCheck, dbCertificateMappings);
        if (dbMappingWithHostName != null && !dbMappingWithHostName.getId().equals(mappingToCheck.getId())) {
            String message = String.format("Duplicate host name detected. Certificate mapping with id '%d' has already configured the host name provided.", dbMappingWithHostName.getId());
            throw new UnprocessableEntityException(message);
        }
    }

    private void validateCertificateMapping(CertificateMapping mapping) throws BadRequestException {
        SslDetails sslDetails = new SslDetails(mapping.getPrivateKey(), mapping.getCertificate(), mapping.getIntermediateCertificate());
        sslDetails = SslDetails.sanitize(sslDetails);

        ZeusCrtFile zeusCrtFile = zeusUtils.buildZeusCrtFileLbassValidation(sslDetails.getPrivateKey(), sslDetails.getCertificate(), sslDetails.getIntermediateCertificate());
        SslTerminationHelper.verifyCertificationCredentials(zeusCrtFile);
    }

    private CertificateMapping getMappingWithDuplicateHostName(CertificateMapping newMapping, Collection<CertificateMapping> dbCertificateMappings) {
        for (CertificateMapping dbCertificateMapping : dbCertificateMappings) {
            if (newMapping.getHostName().equals(dbCertificateMapping.getHostName())) {
                return dbCertificateMapping;
            }
        }

        return null;
    }

    private boolean loadBalancerContainsMapping(LoadBalancer lb, CertificateMapping certificateMapping) {
        for (CertificateMapping m : lb.getCertificateMappings()) {
            if (m.getId().equals(certificateMapping.getId())) {
                return true;
            }
        }
        return false;
    }
}
