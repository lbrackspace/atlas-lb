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
import org.openstack.atlas.service.domain.services.NotificationService;
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

import static org.openstack.atlas.service.domain.services.helpers.AlertType.API_FAILURE;

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
    @Autowired
    private NotificationService notificationService;

    static {
        zeusUtils = new ZeusUtils();
    }

    @Override
    @Transactional
    public CertificateMapping create(LoadBalancer messengerLb) throws UnprocessableEntityException, EntityNotFoundException, BadRequestException, ImmutableEntityException, LimitReachedException, InternalProcessingException {
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
        // We need the mapping id to build the iv
        newMapping = certificateMappingRepository.save(newMapping, messengerLb.getId());

        // If the revised encryption key exists we should be using that one...
        String encryptionKey = restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key);
        if (restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev) != null) {
            encryptionKey = restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev);
        }
        newMapping.setPrivateKey(SslTerminationHelper.encryptPrivateKeyForCertMapping(
                messengerLb.getAccountId(), messengerLb.getId(), newMapping.getId(),
                newMapping.getPrivateKey(), encryptionKey));

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
    public void deleteAllCertMappingForLB(Integer lbId) throws EntityNotFoundException {
        for(CertificateMapping certificateMapping : getAllForLoadBalancerId(lbId)){
            deleteByIdAndLoadBalancerId(certificateMapping.getId(), lbId);
        }
    }

    @Override
    @Transactional
    public void update(LoadBalancer messengerLb) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException, ImmutableEntityException, InternalProcessingException {
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

        // If the revised encryption key exists we should be using that one...
        String encryptionKey = restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key);
        if (restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev) != null) {
            encryptionKey = restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev);
        }
        for (CertificateMapping dbCertMapping : dbCertMappings) {
            if (dbCertMapping.getId().equals(certificateMappingToUpdate.getId())) {
                LOG.info("Certificate mapping to be update found: " + dbCertMapping.getId());
                if (certificateMappingToUpdate.getPrivateKey() != null) {
                    // Updated private key, let's encrypt it...
                    dbCertMapping.setPrivateKey(SslTerminationHelper.encryptPrivateKeyForCertMapping(
                            dbLb.getAccountId(), dbLb.getId(), dbCertMapping.getId(),
                            certificateMappingToUpdate.getPrivateKey(), encryptionKey));
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
//                // With any updated credentials now validated re-encrypt the key
                dbCertMapping.setPrivateKey(SslTerminationHelper.encryptPrivateKeyForCertMapping(dbLb.getAccountId(),
                        dbLb.getId(), dbCertMapping.getId(), dbCertMapping.getPrivateKey(), encryptionKey));
                break;
            }
        }

        setLbToPendingUpdate(messengerLb);
        certificateMappingRepository.update(dbLb);
    }

    @Override
    @Transactional
    public void validatePrivateKeys(LoadBalancer messengerLb, boolean saveKeys)
            throws BadRequestException, InternalProcessingException {
        LOG.debug(String.format("Sync %d certificate mappings for load balancer: %d ",
                messengerLb.getCertificateMappings().size(), messengerLb.getId()));

        // If the revised encryption key exists we should be using that one...
        String encryptionKey = restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key);
        if (restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev) != null) {
            encryptionKey = restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev);
        }
        for (CertificateMapping certificateMapping : messengerLb.getCertificateMappings()) {
                validateCertificateMapping(certificateMapping, messengerLb.getAccountId(), messengerLb.getId());
                try {
                    // With any updated credentials now validated re-encrypt the key
                    certificateMapping.setPrivateKey(Aes.b64encryptGCM(certificateMapping.getPrivateKey().getBytes(),
                            encryptionKey,
                            SslTerminationHelper.getCertificateMappingIv(
                                    certificateMapping, messengerLb.getAccountId(), messengerLb.getId())));
                } catch (Exception e) {
                    String msg = Debug.getEST(e);
                    LOG.error(String.format(
                            "Error encrypting Private key on load balancr %d for mapping %s, %s\n",
                            messengerLb.getId(), certificateMapping.getId(), msg));
                    throw new InternalProcessingException(
                            "Error processing certificate mapping private keys, " +
                                    "please try again later or notify support if problem persists...");
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

    private void validateCertificateMapping(CertificateMapping mapping, int accountId, int lbId)
            throws BadRequestException, InternalProcessingException {
        SslDetails sslDetails = new SslDetails(mapping.getPrivateKey(),
                mapping.getCertificate(), mapping.getIntermediateCertificate());
        try {
            // If update is calling we need to attempt to decrypt key
            sslDetails.setPrivateKey(Aes.b64decryptGCM_str(mapping.getPrivateKey(),
                    restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key),
                    SslTerminationHelper.getCertificateMappingIv(mapping, accountId, lbId)));
            mapping.setPrivateKey(sslDetails.getPrivateKey());
        } catch (Exception ex) {
            try {
                // It's possible the encryption key has been revised, try again...
                sslDetails.setPrivateKey(Aes.b64decryptGCM_str(mapping.getPrivateKey(),
                        restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev),
                        SslTerminationHelper.getCertificateMappingIv(mapping, accountId, lbId)));
                mapping.setPrivateKey(sslDetails.getPrivateKey());
            } catch (Exception e) {
                // If we've failed here then we have something else quite wrong and failures should bubble up
                String msg = String.format("Error encrypting Private key on loadbalancr %d for mapping %s; %s\n",
                        lbId, mapping.getId(), Debug.getEST(e));
                LOG.error(msg);
                notificationService.saveAlert(accountId, lbId, ex, API_FAILURE.name(),
                        String.format("Error encrypting Private key on loadbalancr %d for mapping %s",
                        lbId, mapping.getId()));
                throw new InternalProcessingException("Error processing Certificate Mapping " +
                        "private key, please try again later or notify support if problem persists...");
            }
        }

        ZeusCrtFile zeusCrtFile = zeusUtils.buildZeusCrtFileLbassValidation(sslDetails.getPrivateKey(),
                sslDetails.getCertificate(), sslDetails.getIntermediateCertificate());
        SslTerminationHelper.verifyCertificationCredentials(zeusCrtFile);
    }

    private void validateCertificateMapping(CertificateMapping mapping) throws BadRequestException {
        SslDetails sslDetails = new SslDetails((mapping.getPrivateKey()), mapping.getCertificate(), mapping.getIntermediateCertificate());
        ZeusCrtFile zeusCrtFile = zeusUtils.buildZeusCrtFileLbassValidation(sslDetails.getPrivateKey(), sslDetails.getCertificate(), sslDetails.getIntermediateCertificate());
        SslTerminationHelper.verifyCertificationCredentials(zeusCrtFile);
    }

    private CertificateMapping getMappingWithDuplicateHostName(CertificateMapping newMapping, Collection<CertificateMapping> dbCertificateMappings) {
        for (CertificateMapping dbCertificateMapping : dbCertificateMappings) {
            if (newMapping.getHostName() != null && newMapping.getHostName().equals(dbCertificateMapping.getHostName())) {
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
