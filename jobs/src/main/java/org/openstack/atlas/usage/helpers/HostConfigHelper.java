package org.openstack.atlas.usage.helpers;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;

import java.net.MalformedURLException;

public final class HostConfigHelper {

    public static LoadBalancerEndpointConfiguration getConfig(Host hostIn, HostRepository hostRepository) throws DecryptException, MalformedURLException {
        Cluster cluster = hostIn.getCluster();
        return new LoadBalancerEndpointConfiguration(hostIn, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), hostIn, hostRepository.getFailoverHostNames(cluster.getId()));
    }
}
