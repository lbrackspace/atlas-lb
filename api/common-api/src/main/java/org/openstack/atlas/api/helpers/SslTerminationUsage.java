package org.openstack.atlas.api.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SslTerminationUsage {

    private final Log LOG = LogFactory.getLog(SslTerminationUsage.class);

    public List<SnmpUsage> getUsagesToInsert(Integer loadbalancerId, SslTermination fromSslTerm, SslTermination toSslTerm,
                                             Map<Integer, SnmpUsage> firstPass, Map<Integer, SnmpUsage> secondPass){
        List<SnmpUsage> retUsages;

        if (fromSslTerm.getEnabled()){
            LOG.debug(String.format("SSL Termination was previoiusly enabled for load balancer: %s", loadbalancerId));
            if (fromSslTerm.getSecureTrafficOnly()) {
                LOG.debug(String.format("Secure Traffic only was previously enabled for load balancer: %s", loadbalancerId));
                if (toSslTerm.getEnabled()) {
                    LOG.debug(String.format("SSL Termination is now enabled for load balancer: %s", loadbalancerId));
                    if (toSslTerm.getSecureTrafficOnly()) {
                        //FROM SSL ONLY TO SSL ONLY
                        LOG.debug(String.format("Secure Traffic Only is now enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_ONLY_ON, UsageEvent.SSL_ONLY_ON, firstPass, secondPass);
                    } else {
                        //FROM SSL ONLY TO SSL MIXED
                        LOG.debug(String.format("Secure Traffic Only is now NOT enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_ONLY_ON, UsageEvent.SSL_MIXED_ON, firstPass, secondPass);
                    }
                } else {
                    //FROM SSL ONLY TO SSL OFF
                    LOG.debug(String.format("SSL Termination is now NOT enabled for load balancer: %s", loadbalancerId));
                    retUsages = getUsagesToInsertByStates(UsageEvent.SSL_ONLY_ON, UsageEvent.SSL_OFF, firstPass, secondPass);
                }
            } else {
                LOG.debug(String.format("Secure Traffic only was previously NOT enabled for load balancer: %s", loadbalancerId));
                if (toSslTerm.getEnabled()) {
                    LOG.debug(String.format("SSL Termination is now enabled for load balancer: %s", loadbalancerId));
                    if (toSslTerm.getSecureTrafficOnly()) {
                        //FROM SSL MIXED TO SSL ONLY
                        LOG.debug(String.format("Secure Traffic Only is now enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_MIXED_ON, UsageEvent.SSL_ONLY_ON, firstPass, secondPass);
                    } else {
                        //FROM SSL MIXED TO SSL MIXED
                        LOG.debug(String.format("Secure Traffic Only is now NOT enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_MIXED_ON, UsageEvent.SSL_MIXED_ON, firstPass, secondPass);
                    }
                } else {
                    //FROM SSL MIXED TO SSL OFF
                    LOG.debug(String.format("SSL Termination is now NOT enabled for load balancer: %s", loadbalancerId));
                    retUsages = getUsagesToInsertByStates(UsageEvent.SSL_MIXED_ON, UsageEvent.SSL_OFF, firstPass, secondPass);
                }
            }
        } else {
            LOG.debug(String.format("SSL Termination was previoiusly NOT enabled for load balancer: %s", loadbalancerId));
            if (toSslTerm.getEnabled()) {
                    LOG.debug(String.format("SSL Termination is now enabled for load balancer: %s", loadbalancerId));
                    if (toSslTerm.getSecureTrafficOnly()) {
                        //FROM SSL OFF TO SSL ONLY
                        LOG.debug(String.format("Secure Traffic Only is now enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_OFF, UsageEvent.SSL_ONLY_ON, firstPass, secondPass);
                    } else {
                        //FROM SSL OFF TO SSL MIXED
                        LOG.debug(String.format("Secure Traffic Only is now NOT enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_OFF, UsageEvent.SSL_MIXED_ON, firstPass, secondPass);
                    }
                } else {
                    //FROM SSL OFF TO SSL OFF
                    LOG.debug(String.format("SSL Termination is now NOT enabled for load balancer: %s", loadbalancerId));
                retUsages = getUsagesToInsertByStates(UsageEvent.SSL_OFF, UsageEvent.SSL_OFF, firstPass, secondPass);
                }
        }
        return retUsages;
    }

    public List<SnmpUsage> getUsagesToInsertByStates(UsageEvent fromState, UsageEvent toState,
                                                     Map<Integer, SnmpUsage> firstPass, Map<Integer, SnmpUsage> secondPass) {
        List<SnmpUsage> retUsages = new ArrayList<SnmpUsage>();
        if (fromState == UsageEvent.SSL_ONLY_ON && toState == UsageEvent.SSL_ONLY_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(-1L);
                usageToInsert.setBytesOut(-1L);
                usageToInsert.setConcurrentConnections(0);
                usageToInsert.setBytesInSsl(usage2.getBytesInSsl() > usage1.getBytesInSsl() ? usage2.getBytesInSsl() : usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage2.getBytesOutSsl() > usage1.getBytesOutSsl() ? usage2.getBytesOutSsl() : usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage2.getConcurrentConnectionsSsl() > usage1.getConcurrentConnectionsSsl() ?
                                                   usage2.getConcurrentConnectionsSsl() : usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_ONLY_ON && toState == UsageEvent.SSL_MIXED_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(0L);
                usageToInsert.setBytesOut(0L);
                usageToInsert.setConcurrentConnections(0);
                usageToInsert.setBytesInSsl(usage2.getBytesInSsl() > usage1.getBytesInSsl() ? usage2.getBytesInSsl() : usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage2.getBytesOutSsl() > usage1.getBytesOutSsl() ? usage2.getBytesOutSsl() : usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage2.getConcurrentConnectionsSsl() > usage1.getConcurrentConnectionsSsl() ?
                                                   usage2.getConcurrentConnectionsSsl() : usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_ONLY_ON && toState == UsageEvent.SSL_OFF) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(0L);
                usageToInsert.setBytesOut(0L);
                usageToInsert.setConcurrentConnections(0);
                usageToInsert.setBytesInSsl(usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_MIXED_ON && toState == UsageEvent.SSL_ONLY_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage1.getBytesIn());
                usageToInsert.setBytesOut(usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(usage2.getBytesInSsl() > usage1.getBytesInSsl() ? usage2.getBytesInSsl() : usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage2.getBytesOutSsl() > usage1.getBytesOutSsl() ? usage2.getBytesOutSsl() : usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage2.getConcurrentConnectionsSsl() > usage1.getConcurrentConnectionsSsl() ?
                                                   usage2.getConcurrentConnectionsSsl() : usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_MIXED_ON && toState == UsageEvent.SSL_MIXED_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage2.getBytesIn() > usage1.getBytesIn() ? usage2.getBytesIn() : usage1.getBytesIn());
                usageToInsert.setBytesOut(usage2.getBytesOut() > usage1.getBytesOut() ? usage2.getBytesOut() : usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage2.getConcurrentConnections() > usage1.getConcurrentConnections() ?
                                                usage2.getConcurrentConnections() : usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(usage2.getBytesInSsl() > usage1.getBytesInSsl() ? usage2.getBytesInSsl() : usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage2.getBytesOutSsl() > usage1.getBytesOutSsl() ? usage2.getBytesOutSsl() : usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage2.getConcurrentConnectionsSsl() > usage1.getConcurrentConnectionsSsl() ?
                                                   usage2.getConcurrentConnectionsSsl() : usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_MIXED_ON && toState == UsageEvent.SSL_OFF) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage2.getBytesIn() > usage1.getBytesIn() ? usage2.getBytesIn() : usage1.getBytesIn());
                usageToInsert.setBytesOut(usage2.getBytesOut() > usage1.getBytesOut() ? usage2.getBytesOut() : usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage2.getConcurrentConnections() > usage1.getConcurrentConnections() ?
                        usage2.getConcurrentConnections() : usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_OFF && toState == UsageEvent.SSL_ONLY_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage1.getBytesIn());
                usageToInsert.setBytesOut(usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(0L);
                usageToInsert.setBytesOutSsl(0L);
                usageToInsert.setConcurrentConnectionsSsl(0);
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_OFF && toState == UsageEvent.SSL_MIXED_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage2.getBytesIn() > usage1.getBytesIn() ? usage2.getBytesIn() : usage1.getBytesIn());
                usageToInsert.setBytesOut(usage2.getBytesOut() > usage1.getBytesOut() ? usage2.getBytesOut() : usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage2.getConcurrentConnections() > usage1.getConcurrentConnections() ?
                                                       usage2.getConcurrentConnections() : usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(0L);
                usageToInsert.setBytesOutSsl(0L);
                usageToInsert.setConcurrentConnectionsSsl(0);
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_OFF && toState == UsageEvent.SSL_OFF) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage2.getBytesIn() > usage1.getBytesIn() ? usage2.getBytesIn() : usage1.getBytesIn());
                usageToInsert.setBytesOut(usage2.getBytesOut() > usage1.getBytesOut() ? usage2.getBytesOut() : usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage2.getConcurrentConnections() > usage1.getConcurrentConnections() ?
                                                       usage2.getConcurrentConnections() : usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(-1L);
                usageToInsert.setBytesOutSsl(-1L);
                usageToInsert.setConcurrentConnectionsSsl(0);
                retUsages.add(usageToInsert);
            }
        } else {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(-1L);
                usageToInsert.setBytesOut(-1L);
                usageToInsert.setConcurrentConnections(0);
                usageToInsert.setBytesInSsl(-1L);
                usageToInsert.setBytesOutSsl(-1L);
                usageToInsert.setConcurrentConnectionsSsl(0);
                retUsages.add(usageToInsert);
            }
        }
        return retUsages;
    }
}
