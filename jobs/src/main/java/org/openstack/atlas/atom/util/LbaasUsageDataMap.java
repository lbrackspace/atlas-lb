package org.openstack.atlas.atom.util;

import com.rackspace.docs.core.event.DC;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.pojo.AccountLBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.LBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.entities.SslMode;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;

import javax.xml.datatype.DatatypeConfigurationException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Used for mapping values from usage records to generate usage objects...
 */
public class LbaasUsageDataMap {
    private static String region = "GLOBAL"; //default..

    public static UsageV1Pojo generateUsageEntry(Configuration configuration, String configRegion, Usage usageRecord) throws DatatypeConfigurationException, NoSuchAlgorithmException {
        configRegion = configuration.getString(AtomHopperConfigurationKeys.region);
        if (configRegion != null) {
            region = configRegion;
        }

        UsageV1Pojo usageV1 = new UsageV1Pojo();
        usageV1.setRegion(AHUSLUtil.mapRegion(region));

        usageV1.setVersion(usageRecord.getEntryVersion().toString());
        usageV1.setStartTime(AHUSLUtil.processCalendar(usageRecord.getStartTime().getTimeInMillis()));
        usageV1.setEndTime(AHUSLUtil.processCalendar(usageRecord.getEndTime().getTimeInMillis()));
        usageV1.setDataCenter(DC.fromValue(configuration.getString(AtomHopperConfigurationKeys.data_center)));

        if (AHUSLUtil.mapEventType(usageRecord) == null) {
            usageV1.setType(null);
        } else {
            usageV1.setType(AHUSLUtil.mapEventType(usageRecord));
        }

        usageV1.setTenantId(usageRecord.getAccountId().toString());
        usageV1.setResourceId(usageRecord.getLoadbalancer().getId().toString());

        //Generate UUID
        UUID uuid = UUIDUtil.genUUID(genUUIDString(usageRecord));
        usageV1.setId(uuid.toString());

        //LBAAS specific values
        LBaaSUsagePojo lu = new LBaaSUsagePojo();
        lu.setAvgConcurrentConnections(usageRecord.getAverageConcurrentConnections());
        lu.setAvgConcurrentConnectionsSsl(usageRecord.getAverageConcurrentConnectionsSsl());
        lu.setBandWidthIn(usageRecord.getIncomingTransfer());
        lu.setBandWidthInSsl(usageRecord.getIncomingTransferSsl());
        lu.setBandWidthOut(usageRecord.getOutgoingTransfer());
        lu.setBandWidthOutSsl(usageRecord.getOutgoingTransferSsl());
        lu.setNumPolls(usageRecord.getNumberOfPolls());
        lu.setNumVips(usageRecord.getNumVips());

        BitTags bitTags = new BitTags(usageRecord.getTags());
        if (bitTags.isTagOn(BitTag.SERVICENET_LB)) {
            lu.setVipType(VipType.SERVICENET.value());
        } else {
            lu.setVipType(VipType.PUBLIC.value());
        }

        lu.setSslMode(SslMode.getMode(bitTags).name());

        usageV1.getAny().add(lu);
        return usageV1;
    }

    public static UsageV1Pojo generateAccountUsageEntry(Configuration configuration, String configRegion, AccountUsage accountUsage) throws DatatypeConfigurationException, NoSuchAlgorithmException {
        configRegion = configuration.getString(AtomHopperConfigurationKeys.region);
        if (configRegion != null) {
            region = configRegion;
        }

        UsageV1Pojo usageV1 = new UsageV1Pojo();
        usageV1.setRegion(AHUSLUtil.mapRegion(region));

        usageV1.setVersion("1");//Rows are not updated...
        usageV1.setStartTime(AHUSLUtil.processCalendar(accountUsage.getStartTime().getTimeInMillis()));
        usageV1.setEndTime(AHUSLUtil.processCalendar(accountUsage.getStartTime().getTimeInMillis()));

        usageV1.setType(null); //No events
        usageV1.setTenantId(accountUsage.getAccountId().toString());
        usageV1.setResourceId(accountUsage.getId().toString());
        usageV1.setDataCenter(DC.fromValue(configuration.getString(AtomHopperConfigurationKeys.data_center)));

        //Generate UUID
        UUID uuid = UUIDUtil.genUUID(genUUIDString(accountUsage));
        usageV1.setId(uuid.toString());

        //LBaaS account usage
        AccountLBaaSUsagePojo ausage = new AccountLBaaSUsagePojo();
        ausage.setId(accountUsage.getId());
        ausage.setNumLoadbalancers(accountUsage.getNumLoadBalancers());
        ausage.setNumPublicVips(accountUsage.getNumPublicVips());
        ausage.setNumServicenetVips(accountUsage.getNumServicenetVips());
        usageV1.getAny().add(ausage);

        return usageV1;
    }

    private static String genUUIDString(AccountUsage usageRecord) {
        return usageRecord.getId() + "_" + usageRecord.getAccountId() + "_" + region;
    }

    private static String genUUIDString(Usage usageRecord) {
        return usageRecord.getId() + "_" + usageRecord.getLoadbalancer().getId() + "_" + region;
    }
}
