package org.openstack.atlas.api.mgmt.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.NodesHelper;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Sync;
import org.openstack.atlas.service.domain.pojos.SyncLocation;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.*;
import static org.openstack.atlas.service.domain.entities.NodeStatus.ONLINE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.CREATE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.CREATE_LOADBALANCER;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_LOADBALANCER;

public class SyncListener extends BaseListener {

    final Log LOG = LogFactory.getLog(SyncListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        Sync queueSyncObject = getEsbRequestFromMessage(message).getSyncObject();
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(queueSyncObject.getLoadBalancerId());
        } catch (EntityNotFoundException enfe) {
            LOG.error("EntityNotFoundException thrown.");
            return;
        }

        if (queueSyncObject.getLocationToSyncFrom().equals(SyncLocation.DATABASE)) {
            LOG.debug(String.format("Synchronizing load balancer #%d with database configuration", queueSyncObject.getLoadBalancerId()));

            final LoadBalancerStatus loadBalancerStatus = dbLoadBalancer.getStatus();

            try {
                reverseProxyLoadBalancerService.deleteLoadBalancer(dbLoadBalancer);
            } catch (Exception e) {
                String msg = "Error deleting loadbalancer in SyncListener(): ";
                loadBalancerService.setStatus(dbLoadBalancer, ERROR);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.ZEUS_FAILURE.name(), msg);
                LOG.error(msg, e);

                //Set status record
                loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ERROR);
            }

            if (loadBalancerStatus.equals(PENDING_DELETE) || loadBalancerStatus.equals(DELETED)) {
                loadBalancerService.setStatus(dbLoadBalancer, DELETED);
                loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.DELETED);

                loadBalancerService.pseudoDelete(dbLoadBalancer);

                if (loadBalancerStatus.equals(PENDING_DELETE)) {
                    // Add atom entry
                    String atomTitle = "Load Balancer Successfully Deleted";
                    String atomSummary = "Load balancer successfully deleted";
                    notificationService.saveLoadBalancerEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_LOADBALANCER, DELETE, INFO);

                    // Notify usage processor
                    usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.DELETE_LOADBALANCER);

                    //Set status record
                    loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_DELETE);

                }
            } else {

                //First recreate the original virtual server...
                try {
                    //Ssl termination will be added on second pass...
                    LoadBalancer tempLb = loadBalancerService.get(queueSyncObject.getLoadBalancerId());
                    tempLb.setSslTermination(null);

                    loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.BUILD);
                    reverseProxyLoadBalancerService.createLoadBalancer(tempLb);
                    loadBalancerService.setStatus(dbLoadBalancer, ACTIVE);
                    loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ACTIVE);


                    if (loadBalancerStatus.equals(BUILD)) {
                        NodesHelper.setNodesToStatus(dbLoadBalancer, ONLINE);
                        dbLoadBalancer.setStatus(ACTIVE);
                        dbLoadBalancer = loadBalancerService.update(dbLoadBalancer);

                        //Set status record
                        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ACTIVE);

                        // Add atom entry
                        String atomTitle = "Load Balancer Successfully Created";
                        String atomSummary = createAtomSummary(dbLoadBalancer).toString();
                        notificationService.saveLoadBalancerEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, CREATE_LOADBALANCER, CREATE, INFO);

                        // Notify usage processor
                        usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.CREATE_LOADBALANCER);
                    }
                } catch (Exception e) {
                    String msg = "Error re-creating loadbalancer in SyncListener():";
                    loadBalancerService.setStatus(dbLoadBalancer, ERROR);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.ZEUS_FAILURE.name(), msg);
                    LOG.error(msg, e);

                    //Set status record
                    loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ERROR);

                }

                try {
                    //Now create the secure VS if ssl termination was already on the loadbalancer...
                    if (dbLoadBalancer.hasSsl()) {
                        org.openstack.atlas.service.domain.entities.SslTermination dbTermination = sslTerminationService.getSslTermination(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());

                        //Map
                        SslTermination domainSslTermination = new SslTermination();
                        domainSslTermination.setIntermediateCertificate(dbTermination.getIntermediateCertificate());
                        domainSslTermination.setCertificate(dbTermination.getCertificate());
                        domainSslTermination.setPrivatekey(dbTermination.getPrivatekey());
                        domainSslTermination.setEnabled(dbTermination.isEnabled());
                        domainSslTermination.setSecurePort(dbTermination.getSecurePort());
                        domainSslTermination.setSecureTrafficOnly(dbTermination.isSecureTrafficOnly());

                        //We must re-validate cert/keys before sending to zeus  V1-D-04287
                        ZeusSslTermination zeusTermination = sslTerminationService.updateSslTermination(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), domainSslTermination);

                        reverseProxyLoadBalancerService.updateSslTermination(dbLoadBalancer, zeusTermination);
                        loadBalancerService.setStatus(dbLoadBalancer, ACTIVE);
                        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ACTIVE);


                        if (loadBalancerStatus.equals(BUILD)) {
                            NodesHelper.setNodesToStatus(dbLoadBalancer, ONLINE);
                            dbLoadBalancer.setStatus(ACTIVE);
                            dbLoadBalancer = loadBalancerService.update(dbLoadBalancer);
                            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ACTIVE);


                            // Add atom entry
                            String atomTitle = "Load Balancer Successfully Created";
                            String atomSummary = createAtomSummary(dbLoadBalancer).toString();
                            notificationService.saveLoadBalancerEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, CREATE_LOADBALANCER, CREATE, INFO);

                            // Notify usage processor
                            usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.CREATE_LOADBALANCER);
                            if (dbLoadBalancer.isUsingSsl()) {
                                if(dbLoadBalancer.getSslTermination().isSecureTrafficOnly()) {
                                    usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.SSL_ONLY_ON);
                                } else {
                                    usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.SSL_MIXED_ON);
                                }
                            } else {
                                usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.SSL_OFF);
                            }
                        }
                    }
                } catch (Exception e) {
                    String msg = "Error re-creating ssl terminated loadbalancer in SyncListener():";
                    loadBalancerService.setStatus(dbLoadBalancer, ERROR);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.ZEUS_FAILURE.name(), msg);
                    LOG.error(msg, e);
                }
            }
        } else if (queueSyncObject.getLocationToSyncFrom().equals(SyncLocation.ZEUS)) {
            LOG.warn(String.format("Load balancers can only be synchronized with the database at this time."));
        }

        LOG.info("Sync operation complete.");
    }

    private StringBuffer createAtomSummary(LoadBalancer lb) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Load balancer successfully created with ");
        atomSummary.append("name: '").append(lb.getName()).append("', ");
        atomSummary.append("algorithm: '").append(lb.getAlgorithm()).append("', ");
        atomSummary.append("protocol: '").append(lb.getProtocol()).append("', ");
        atomSummary.append("port: '").append(lb.getPort()).append("'");
        return atomSummary;
    }
}
