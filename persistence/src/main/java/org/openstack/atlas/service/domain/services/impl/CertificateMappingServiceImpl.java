package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.SslDetails;
import org.openstack.atlas.service.domain.services.CertificateMappingService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.helpers.SslTerminationHelper;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.openstack.atlas.util.debug.Debug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class CertificateMappingServiceImpl extends BaseService implements CertificateMappingService {
    private final Log LOG = LogFactory.getLog(CertificateMappingServiceImpl.class);
    private static final ZeusUtils zeusUtils;

    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    @Autowired
    private AccountLimitServiceImpl accountLimitService;
    @Autowired
    private RestApiConfiguration restApiConfiguration;

    static {
        zeusUtils = new ZeusUtils();
    }

    @Override
    @Transactional
    public CertificateMapping create(LoadBalancer messengerLb) throws UnprocessableEntityException, EntityNotFoundException, BadRequestException, ImmutableEntityException, LimitReachedException {
        ensureSslTerminationConfigIsAvailable(messengerLb.getId());
        List<CertificateMapping> dbCertificateMappings = certificateMappingRepository.getAllForLoadBalancerId(messengerLb.getId());
        CertificateMapping newMapping = messengerLb.getCertificateMappings().iterator().next();

        int certMappingLimit = accountLimitService.getLimit(messengerLb.getAccountId(), AccountLimitType.CERTIFICATE_MAPPING_LIMIT);
        if (dbCertificateMappings.size() >= certMappingLimit) {
            throw new LimitReachedException(String.format("Certificate mapping limit reached. Limit is set to '%d'. Please contact support if you would like to increase your limit.", certMappingLimit));
        }

        detectDuplicateHostName(dbCertificateMappings, newMapping);
        validateCertificateMapping(newMapping);


        if (newMapping.getIntermediateCertificate() != null && newMapping.getIntermediateCertificate().trim().isEmpty()) {
            newMapping.setIntermediateCertificate(null);
        }
        newMapping = certificateMappingRepository.save(newMapping, messengerLb.getId());
        try {
            newMapping.setPrivateKey(Aes.b64encryptGCM(newMapping.getPrivateKey().getBytes(),
                    restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key),
                    SslTerminationHelper.getCertificateMappingIv(newMapping,
                            messengerLb.getAccountId(), messengerLb.getId())));
        } catch (Exception e) {
            String msg = Debug.getEST(e);
            LOG.error(String.format("Error encrypting Private key on loadbalancr %d: %s\n", messengerLb.getId(), msg));
            throw new BadRequestException("Error processing certificate mapping private key, please verify formatting...");
        }

        setLbToPendingUpdate(messengerLb);
        return certificateMappingRepository.save(newMapping, messengerLb.getId());
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
    @Transactional
    public void update(LoadBalancer messengerLb) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException, ImmutableEntityException {
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
                    if (certificateMappingToUpdate.getIntermediateCertificate().trim().isEmpty()) {
                        dbCertMapping.setIntermediateCertificate(null);
                    } else {
                        dbCertMapping.setIntermediateCertificate(certificateMappingToUpdate.getIntermediateCertificate());
                    }
                }
                if (certificateMappingToUpdate.getHostName() != null) {
                    dbCertMapping.setHostName(certificateMappingToUpdate.getHostName());
                }

                validateCertificateMapping(dbCertMapping, messengerLb.getAccountId(), messengerLb.getId());
                // With any new credentials now validated re-encrypt the key
                try {
                    dbCertMapping.setPrivateKey(Aes.b64encryptGCM(dbCertMapping.getPrivateKey().getBytes(),
                            restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key),
                            SslTerminationHelper.getCertificateMappingIv(dbCertMapping,
                                    messengerLb.getAccountId(), messengerLb.getId())));
                } catch (Exception e) {
                    String msg = Debug.getEST(e);
                    LOG.error(String.format("Error encrypting Private key on loadbalancr %d: %s\n", messengerLb.getId(), msg));
                    throw new BadRequestException("Error processing certificate mapping private key, please verify formatting...");
                }
                break;
            }
        }

        setLbToPendingUpdate(messengerLb);
        certificateMappingRepository.update(dbLb);
    }

    @Override
    @Transactional
    public void validatePrivateKeys(LoadBalancer messengerLb, boolean saveKeys) throws BadRequestException {
        LOG.debug(String.format("Sync %d certificate mappings for load balancer: %d ",
                messengerLb.getCertificateMappings().size(), messengerLb.getId()));
        for (CertificateMapping certificateMapping : messengerLb.getCertificateMappings()) {
                validateCertificateMapping(certificateMapping, messengerLb.getAccountId(), messengerLb.getId());
                try {
                    // With any updated credentials now validated re-encrypt the key
                    certificateMapping.setPrivateKey(Aes.b64encryptGCM(certificateMapping.getPrivateKey().getBytes(),
                            restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key),
                            SslTerminationHelper.getCertificateMappingIv(certificateMapping,
                                    messengerLb.getAccountId(), messengerLb.getId())));
                } catch (Exception e) {
                    String msg = Debug.getEST(e);
                    LOG.error(String.format(
                            "Error encrypting Private key on load balancr %d: %s\n", messengerLb.getId(), msg));
                    throw new BadRequestException(
                            "Error processing certificate mapping private keys, please verify formatting...");
                }
            }
        if (saveKeys) {
            certificateMappingRepository.update(messengerLb);
            LOG.debug(String.format("Saved %d updated certificate mappings for load balancer: %d ",
                    messengerLb.getCertificateMappings().size(), messengerLb.getId()));
        }
    }

    @Override
    @Transactional
    public void prepareForDelete(LoadBalancer messengerLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        ensureSslTerminationConfigIsAvailable(messengerLb.getId());
        certificateMappingRepository.getByIdAndLoadBalancerId(messengerLb.getCertificateMappings().iterator().next().getId(), messengerLb.getId());
        setLbToPendingUpdate(messengerLb);
    }

    @Override
    @Transactional
    public void deleteByIdAndLoadBalancerId(Integer id, Integer lbId) throws EntityNotFoundException {
        ensureSslTerminationConfigIsAvailable(lbId);
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(lbId);
        certificateMappingRepository.delete(dbLoadBalancer, id);
    }

    private void setLbToPendingUpdate(LoadBalancer lb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException {
        lb = loadBalancerRepository.getById(lb.getId());
        if (!loadBalancerRepository.testAndSetStatus(lb.getAccountId(), lb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(lb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            loadBalancerStatusHistoryService.save(lb.getAccountId(), lb.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }
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

    private void validateCertificateMapping(CertificateMapping mapping,
                                            int accountId, int lbId) throws BadRequestException {
        SslDetails sslDetails = new SslDetails(mapping.getPrivateKey(),
                mapping.getCertificate(), mapping.getIntermediateCertificate());
        try {
            // If update is calling we need to attempt to decrypt key
            sslDetails.setPrivateKey(Aes.b64decryptGCM_str(mapping.getPrivateKey(),
                    restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key),
                    SslTerminationHelper.getCertificateMappingIv(mapping, accountId, lbId)));
            mapping.setPrivateKey(sslDetails.getPrivateKey());
        } catch (Exception ex) {
            // At this time we must assume the keys simply weren't encrypted to begin with and let validation check...
            sslDetails.setPrivateKey(mapping.getPrivateKey());
        }
        ZeusCrtFile zeusCrtFile = zeusUtils.buildZeusCrtFileLbassValidation(sslDetails.getPrivateKey(),
                sslDetails.getCertificate(), sslDetails.getIntermediateCertificate());
        SslTerminationHelper.verifyCertificationCredentials(zeusCrtFile);
    }

    private void validateCertificateMapping(CertificateMapping mapping) throws BadRequestException {
        SslDetails sslDetails = new SslDetails((mapping.getPrivateKey()), mapping.getCertificate(), mapping.getIntermediateCertificate());
        sslDetails = SslDetails.sanitize((sslDetails));
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
