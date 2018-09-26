package org.openstack.atlas.api.mgmt.mapper.dozer;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import org.dozer.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openstack.atlas.api.mapper.dozer.MapperBuilder;
import org.openstack.atlas.api.mapper.dozer.MapperBuilder;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterType;
import org.openstack.atlas.util.crypto.HashUtil;
import org.junit.Assert;

public class MiscDomainDataModelDozerTests {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";
    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";
    private static final Mapper pubMapper;
    private static final Mapper mgmtMapper;

    static {
        pubMapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
        mgmtMapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testAccountMapper() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord dataAccountRecord;
        org.openstack.atlas.service.domain.entities.Account domainAccount;
        int id = 1;
        String expSha1 = HashUtil.sha1sumHex("1".getBytes("UTF-8"), 0, 4);
        org.openstack.atlas.service.domain.entities.ClusterType domainInteralCluster = org.openstack.atlas.service.domain.entities.ClusterType.INTERNAL;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterType dataInternalCluster = org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterType.INTERNAL;
        org.openstack.atlas.service.domain.entities.ClusterType domainStandardCluster = org.openstack.atlas.service.domain.entities.ClusterType.STANDARD;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterType dataStandardCluster = org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterType.STANDARD;

        dataAccountRecord = newDataAccount(1, expSha1, "INTERAL");
        domainAccount = (org.openstack.atlas.service.domain.entities.Account) mgmtMapper.map(dataAccountRecord, org.openstack.atlas.service.domain.entities.Account.class);
        Assert.assertTrue(domainAccount.getId().intValue() == id);
        Assert.assertTrue(domainAccount.getSha1SumForIpv6().equals(expSha1));
        Assert.assertTrue(domainAccount.getClusterType() == domainInteralCluster);

        dataAccountRecord = newDataAccount(1, expSha1, null);
        domainAccount = (org.openstack.atlas.service.domain.entities.Account) mgmtMapper.map(dataAccountRecord, org.openstack.atlas.service.domain.entities.Account.class);
        Assert.assertTrue(domainAccount.getId().intValue() == id);
        Assert.assertTrue(domainAccount.getSha1SumForIpv6().equals(expSha1));
        Assert.assertTrue(domainAccount.getClusterType() == domainStandardCluster);

        domainAccount = newDomainAccount(id, expSha1, "INTERNAL");
        dataAccountRecord = (org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord) mgmtMapper.map(domainAccount, org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord.class);
        Assert.assertTrue(dataAccountRecord.getId().intValue() == id);
        Assert.assertTrue(dataAccountRecord.getSha1SumForIpv6().equals(expSha1));
        Assert.assertTrue(dataAccountRecord.getClusterType() == dataInternalCluster);

        domainAccount = newDomainAccount(id, expSha1, null);
        dataAccountRecord = (org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord) mgmtMapper.map(domainAccount, org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord.class);
        Assert.assertTrue(dataAccountRecord.getId().intValue() == id);
        Assert.assertTrue(dataAccountRecord.getSha1SumForIpv6().equals(expSha1));
        Assert.assertTrue(dataAccountRecord.getClusterType() == null);

        nop();
    }

    public static org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord newDataAccount(int id, String sha1sum, String clusterType) {
        org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord ar = new org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord();
        ar.setId(new Integer(id));
        ar.setSha1SumForIpv6(sha1sum);
        if (clusterType != null) {
            ar.setClusterType(org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterType.valueOf(clusterType));
        }
        return ar;
    }

    public static org.openstack.atlas.service.domain.entities.Account newDomainAccount(int id, String sha1, String clusterType) {
        org.openstack.atlas.service.domain.entities.Account a = new org.openstack.atlas.service.domain.entities.Account();
        a.setId(new Integer(id));
        a.setSha1SumForIpv6(sha1);
        if (clusterType != null) {
            a.setClusterType(org.openstack.atlas.service.domain.entities.ClusterType.valueOf(clusterType));
        }
        return a;
    }

    private static void nop() {
    }
}
