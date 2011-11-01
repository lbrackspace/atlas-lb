package org.openstack.atlas.jobs.helper;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.common.crypto.CryptoUtil;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.service.domain.entity.Cluster;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;

import java.net.MalformedURLException;

public final class HostConfigHelper {

    public static LoadBalancerEndpointConfiguration getConfig(Host hostIn, HostRepository hostRepository) throws DecryptException, MalformedURLException {
        Cluster cluster = hostIn.getCluster();
        return new LoadBalancerEndpointConfiguration(hostIn, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), hostIn, hostRepository.getFailoverHostNames(cluster.getId()));
    }
}
