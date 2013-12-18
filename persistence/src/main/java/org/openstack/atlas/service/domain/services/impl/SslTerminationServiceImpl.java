package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.SslTerminationService;
import org.openstack.atlas.service.domain.services.helpers.SslTerminationHelper;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SslTerminationServiceImpl extends BaseService implements SslTerminationService {
    protected static final ZeusUtils zeusUtils;
    protected final Log LOG = LogFactory.getLog(SslTerminationServiceImpl.class);

    static{
        zeusUtils = new ZeusUtils();
    }

    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;


    @Override
    @Transactional
    public ZeusSslTermination updateSslTermination(int lbId, int accountId, SslTermination sslTermination) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException {
        ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
        ZeusCrtFile zeusCrtFile = null;

        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lbId, accountId);

        //Verify ports and protocols...
        SslTerminationHelper.isProtocolSecure(dbLoadBalancer);

        //Grab ports from all vips/shared vips and their lbs'
        Map<Integer, List<LoadBalancer>> vipPorts = new TreeMap<Integer, List<LoadBalancer>>();
        Map<Integer, List<LoadBalancer>> vip6Ports = new TreeMap<Integer, List<LoadBalancer>>();
        if (!dbLoadBalancer.getLoadBalancerJoinVipSet().isEmpty()) {
            vipPorts = virtualIpRepository.getPorts(dbLoadBalancer.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp().getId());
        }

        if (!dbLoadBalancer.getLoadBalancerJoinVip6Set().isEmpty()) {
            vip6Ports = virtualIpv6Repository.getPorts(dbLoadBalancer.getLoadBalancerJoinVip6Set().iterator().next().getVirtualIp().getId());
        }

        if (!SslTerminationHelper.verifyPortSecurePort(dbLoadBalancer, sslTermination, vipPorts, vip6Ports)) {
            throw new BadRequestException(String.format("Secure port: '%s'  must be unique " +
                    " Ports taken: '%s'", sslTermination.getSecurePort(), buildPortString(vipPorts, vip6Ports)));
        }

        if (dbLoadBalancer.isHttpsRedirect() != null && dbLoadBalancer.isHttpsRedirect()) {
            //Must be secure-only
            if (sslTermination.isSecureTrafficOnly() != null && !sslTermination.isSecureTrafficOnly()) {
                throw new BadRequestException("Cannot use 'mixed-mode' SSL termination while HTTPS Redirect is enabled.");
            }
            //Must use secure port 443
            if (sslTermination.getSecurePort() != null && sslTermination.getSecurePort() != 443) {
                throw new BadRequestException("Must use secure port 443 with HTTPS Redirect enabled.");
            }
        }

        org.openstack.atlas.service.domain.entities.SslTermination dbTermination = null;
        try {
            dbTermination = getSslTermination(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
        } catch (EntityNotFoundException e) {
            //this is fine...
            LOG.warn("LoadBalancer ssl termination could not be found, ");
        }

        //we wont make it here if no dbTermination and no cert/key values.
        dbTermination = SslTerminationHelper.verifyAttributes(sslTermination, dbTermination);

        if (dbTermination != null) {
            if (!SslTerminationHelper.modificationStatus(sslTermination, dbLoadBalancer)) {
                //Validate the certifications and key return the list of errors if there are any, otherwise, pass the transport object to async layer...
                SslTerminationHelper.cleanSSLCertKeyEntries(dbTermination);
                zeusCrtFile = zeusUtils.buildZeusCrtFileLbassValidation(dbTermination.getPrivatekey(), dbTermination.getCertificate(), dbTermination.getIntermediateCertificate());
                SslTerminationHelper.verifyCertificationCredentials(zeusCrtFile);
            }
        } else {
          //*Should never happen...
          LOG.error("The ssl termination service layer could not handle the request, thus not producing a proper sslTermination Object causing this failure...");
          throw new UnprocessableEntityException("There was a problem generating the ssl termination configuration, please contact support...");
        }

          LOG.debug("Updating the lb status to pending_update");
        if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }

        LOG.info(String.format("Saving ssl termination to the data base for loadbalancer: '%s'", lbId));
        sslTerminationRepository.setSslTermination(lbId, dbTermination);
        LOG.info(String.format("Succesfully saved ssl termination to the data base for loadbalancer: '%s'", lbId));

        zeusSslTermination.setSslTermination(dbTermination);
        if (zeusCrtFile != null) {
            zeusSslTermination.setCertIntermediateCert(zeusCrtFile.getPublic_cert());
        }

        return zeusSslTermination;
    }

    @Override
    @Transactional
    public boolean deleteSslTermination(Integer loadBalancerId, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LOG.info("Deleting ssl termination from the database for loadbalancer: " + loadBalancerId);
        return sslTerminationRepository.removeSslTermination(loadBalancerId, accountId);
    }

    @Override
    @Transactional
    public void pseudoDeleteSslTermination(Integer loadBalancerId, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);

        if (dbLoadBalancer.hasSsl()) {
            LOG.debug("Updating the lb status to pending_update");
            if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
                String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
                LOG.warn(message);
                throw new ImmutableEntityException(message);
            } else {
                //Set status record
                loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ACTIVE);
            }
        } else {
            throw new BadRequestException("SSL termination could not be found for the requested loadbalancer.");
        }
    }

    @Override
    public org.openstack.atlas.service.domain.entities.SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException {
        return sslTerminationRepository.getSslTerminationByLbId(lid, accountId);
    }

    private String buildPortString(Map<Integer, List<LoadBalancer>> vipPorts, Map<Integer, List<LoadBalancer>> vip6Ports) {
        final List<Integer> uniques = new ArrayList<Integer>();

        for (int i : vipPorts.keySet()) {
            if (!uniques.contains(i)) {
                uniques.add(i);
            }
        }

        for (int i : vip6Ports.keySet()) {
            if (!uniques.contains(i)) {
                uniques.add(i);
            }
        }

        return StringUtilities.buildDelemtedListFromIntegerArray(uniques.toArray(new Integer[uniques.size()]), ",");
    }

    @Override
    public Map<Integer, org.openstack.atlas.service.domain.entities.SslTermination> getAllMappedByLbId() {
        Map<Integer, org.openstack.atlas.service.domain.entities.SslTermination> sslMap = new HashMap<Integer, org.openstack.atlas.service.domain.entities.SslTermination>();
        List<org.openstack.atlas.service.domain.entities.SslTermination> sslTerms = sslTerminationRepository.getAll();
        for (org.openstack.atlas.service.domain.entities.SslTermination sslTerm : sslTerms) {
            sslMap.put(sslTerm.getLoadbalancer().getId(), sslTerm);
        }
        return sslMap;
    }
}

