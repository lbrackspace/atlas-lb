package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.core.api.v1.VirtualIp;
import org.openstack.atlas.core.api.v1.VirtualIps;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;
import org.openstack.atlas.service.domain.pojo.VirtualIpDozerWrapper;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.core.api.v1.VipType.PUBLIC;
import static org.openstack.atlas.core.api.v1.VipType.PRIVATE;

public class VirtualIpConverter implements CustomConverter {
    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
        final Integer VIP_ID_DEMARCATION = 9000000;
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof VirtualIpDozerWrapper) {
            VirtualIpDozerWrapper dozerWrapper = (VirtualIpDozerWrapper) sourceFieldValue;
            ArrayList<org.openstack.atlas.core.api.v1.VirtualIp> vips = new ArrayList<org.openstack.atlas.core.api.v1.VirtualIp>();

             try {
                for (LoadBalancerJoinVip loadBalancerJoinVip : dozerWrapper.getLoadBalancerJoinVipSet()) {
                    VirtualIp vip = new VirtualIp();
                    vip.setId(loadBalancerJoinVip.getVirtualIp().getId());
                    vip.setAddress(loadBalancerJoinVip.getVirtualIp().getAddress());
                    vip.setIpVersion(IpVersion.IPV4);

                    switch (loadBalancerJoinVip.getVirtualIp().getVipType()) {
                        case PUBLIC:
                            vip.setType(PUBLIC);
                            break;
                        case PRIVATE:
                            vip.setType(PRIVATE);
                            break;
                        default:
                            throw new RuntimeException(String.format("Unsupported vip type '%s' given while mapping.", loadBalancerJoinVip.getVirtualIp().getVipType().name()));
                    }
                    vips.add(vip);
                }
             } catch (NullPointerException e) {
                 //Ignore, there is nothing to map
            }

            if (dozerWrapper.getLoadBalancerJoinVipSet() != null || !dozerWrapper.getLoadBalancerJoinVip6Set().isEmpty()) {
                for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : dozerWrapper.getLoadBalancerJoinVip6Set()) {
                    VirtualIp vip = new VirtualIp();
                    vip.setId(loadBalancerJoinVip6.getVirtualIp().getId());
                    vip.setIpVersion(IpVersion.IPV6);
                    vip.setType(PUBLIC);

                    try {
                        vip.setAddress(loadBalancerJoinVip6.getVirtualIp().getDerivedIpString());
                    } catch (IPStringConversionException1 e) {
                        throw new RuntimeException("Cannot map ipv6 address. Dozer mapping canceled.");
                    }
                    vips.add(vip);
                }
            }
            return vips;
        }

        if (sourceFieldValue instanceof ArrayList) {
            ArrayList<org.openstack.atlas.core.api.v1.VirtualIp> vips = (ArrayList<org.openstack.atlas.core.api.v1.VirtualIp>) sourceFieldValue;
            Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();

            for (VirtualIp vip : vips) {
                if (vip.getId() != null) {
                    if (vip.getId() >= VIP_ID_DEMARCATION) {
                        loadBalancerJoinVip6Set = buildSharedVip6(vip);
                    } else {
                        loadBalancerJoinVipSet = buildSharedVip(vip);
                    }
                }
                if (vip.getIpVersion() != null) {
                    if (vip.getIpVersion().equals(IpVersion.IPV4)) {
                        loadBalancerJoinVipSet = buildLoadBalancerJoinVipSet(vip);

                    } else if (vip.getIpVersion().equals(IpVersion.IPV6)) {
                        LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();
                        VirtualIpv6 domainVip = new VirtualIpv6();
                        domainVip.setId(vip.getId());
                        loadBalancerJoinVip6.setVirtualIp(domainVip);
                        loadBalancerJoinVip6Set.add(loadBalancerJoinVip6);
                    }
                } else {
                    if (vip.getType() != null && vip.getType().equals(VipType.PUBLIC)) {
                        LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();
                        VirtualIpv6 domainVip = new VirtualIpv6();
                        domainVip.setId(vip.getId());
                        loadBalancerJoinVip6.setVirtualIp(domainVip);
                        loadBalancerJoinVip6Set.add(loadBalancerJoinVip6);

                        vip.setIpVersion(IpVersion.IPV4);
                        loadBalancerJoinVipSet = buildLoadBalancerJoinVipSet(vip);

                    }else if (vip.getType() != null && vip.getType().equals(VipType.PRIVATE)) {
                        vip.setIpVersion(IpVersion.IPV4);
                        loadBalancerJoinVipSet = buildLoadBalancerJoinVipSet(vip);
                    }
                }
            }

            VirtualIpDozerWrapper dozerWrapper = new VirtualIpDozerWrapper();
            dozerWrapper.setLoadBalancerJoinVipSet(loadBalancerJoinVipSet);
            dozerWrapper.setLoadBalancerJoinVip6Set(loadBalancerJoinVip6Set);
            return dozerWrapper;
        }

        if (sourceFieldValue instanceof VirtualIps) {
            VirtualIps vips = (VirtualIps) sourceFieldValue;
            Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();

            for (VirtualIp vip : vips.getVirtualIps()) {
                if (vip.getIpVersion() == null)
                    throw new RuntimeException("Ip Version must be specified for dozer mapping to work.");
                if (vip.getIpVersion().equals(IpVersion.IPV4)) {
                    LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                    org.openstack.atlas.service.domain.entity.VirtualIp domainVip = new org.openstack.atlas.service.domain.entity.VirtualIp();
                    domainVip.setId(vip.getId());
                    domainVip.setAddress(vip.getAddress());

                    switch (vip.getType()) {
                        case PUBLIC:
                            domainVip.setVipType(VirtualIpType.PUBLIC);
                            break;
                        case PRIVATE:
                            domainVip.setVipType(VirtualIpType.PRIVATE);
                            break;
                    }

                    loadBalancerJoinVip.setVirtualIp(domainVip);
                    loadBalancerJoinVipSet.add(loadBalancerJoinVip);
                } else if (vip.getIpVersion().equals(IpVersion.IPV6)) {
                    LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();
                    VirtualIpv6 domainVip = new VirtualIpv6();
                    domainVip.setId(vip.getId());

                    loadBalancerJoinVip6.setVirtualIp(domainVip);
                    loadBalancerJoinVip6Set.add(loadBalancerJoinVip6);
                }
            }

            VirtualIpDozerWrapper dozerWrapper = new VirtualIpDozerWrapper();
            dozerWrapper.setLoadBalancerJoinVipSet(loadBalancerJoinVipSet);
            dozerWrapper.setLoadBalancerJoinVip6Set(loadBalancerJoinVip6Set);
            return dozerWrapper;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }

    private Set<LoadBalancerJoinVip> buildLoadBalancerJoinVipSet(VirtualIp vip) {
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();

        org.openstack.atlas.service.domain.entity.VirtualIp domainVip = new org.openstack.atlas.service.domain.entity.VirtualIp();
        domainVip.setId(vip.getId());
        domainVip.setAddress(vip.getAddress());

        switch (vip.getType()) {
            case PUBLIC:
                domainVip.setVipType(VirtualIpType.PUBLIC);
                break;
            case PRIVATE:
                domainVip.setVipType(VirtualIpType.PRIVATE);
                break;
        }

/*        switch (vip.getIpVersion()) {
            case IPV4:
                domainVip.setIpVersion(org.openstack.atlas.service.domain.entity.IpVersion.IPV4);
                break;
            case IPV6:
                domainVip.setIpVersion(org.openstack.atlas.service.domain.entity.IpVersion.IPV6);
                break;
        }*/

        loadBalancerJoinVip.setVirtualIp(domainVip);
        loadBalancerJoinVipSet.add(loadBalancerJoinVip);

        return loadBalancerJoinVipSet;
    }

    private Set<LoadBalancerJoinVip> buildSharedVip(VirtualIp vip) {
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();

        org.openstack.atlas.service.domain.entity.VirtualIp domainVip = new org.openstack.atlas.service.domain.entity.VirtualIp();
        domainVip.setId(vip.getId());

        loadBalancerJoinVip.setVirtualIp(domainVip);
        loadBalancerJoinVipSet.add(loadBalancerJoinVip);

        return loadBalancerJoinVipSet;
    }

    private Set<LoadBalancerJoinVip6> buildSharedVip6(VirtualIp vip) {
        LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();
        Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();

        VirtualIpv6 domainVip6 = new VirtualIpv6();
        domainVip6.setId(vip.getId());

        loadBalancerJoinVip6.setVirtualIp(domainVip6);
        loadBalancerJoinVip6Set.add(loadBalancerJoinVip6);

        return loadBalancerJoinVip6Set;
    }
}
