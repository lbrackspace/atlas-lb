package org.openstack.atlas.atom.util;

import com.rackspace.docs.core.event.DC;
import com.rackspace.docs.usage.lbaas.ResourceTypes;
import com.rackspace.docs.usage.lbaas.SslModeEnum;
import com.rackspace.docs.usage.lbaas.VipTypeEnum;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.pojo.AccountLBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.pojo.LBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.entities.SslMode;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.w3._2005.atom.Title;
import org.w3._2005.atom.Type;
import org.w3._2005.atom.UsageCategory;
import org.w3._2005.atom.UsageContent;

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeConfigurationException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Used for mapping values from usage records to generate usage objects...
 */
public class LbaasUsageDataMap {
    private static String region = "GLOBAL";
    private static final String label = "loadBalancerUsage";
    private static final String lbaasTitle = "cloudLoadBalancers";
    private static String SERVICE_CODE = "CloudLoadBalancers";

    public static UsageV1Pojo generateUsageEntry(Configuration configuration, String configRegion, Usage usageRecord) throws DatatypeConfigurationException, NoSuchAlgorithmException {
        configRegion = configuration.getString(AtomHopperConfigurationKeys.ahusl_region);
        if (configRegion != null) {
            region = configRegion;
        }

        UsageV1Pojo usageV1 = new UsageV1Pojo();
        usageV1.setRegion(AHUSLUtil.mapRegion(region));

        usageV1.setVersion(usageRecord.getEntryVersion().toString());
        usageV1.setStartTime(AHUSLUtil.processCalendar(usageRecord.getStartTime()));
        usageV1.setEndTime(AHUSLUtil.processCalendar(usageRecord.getEndTime()));
        usageV1.setDataCenter(DC.fromValue(configuration.getString(AtomHopperConfigurationKeys.ahusl_data_center)));

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
        lu.setServiceCode(SERVICE_CODE);
        lu.setResourceType(ResourceTypes.LOADBALANCER);

        BitTags bitTags = new BitTags(usageRecord.getTags());
        if (bitTags.isTagOn(BitTag.SERVICENET_LB)) {
            lu.setVipType(VipTypeEnum.SERVICENET);
        } else {
            lu.setVipType(VipTypeEnum.PUBLIC);
        }

        lu.setSslMode(SslModeEnum.fromValue(SslMode.getMode(bitTags).name()));

        usageV1.getAny().add(lu);
        return usageV1;
    }

    public static UsageV1Pojo generateAccountUsageEntry(Configuration configuration, String configRegion, AccountUsage accountUsage) throws DatatypeConfigurationException, NoSuchAlgorithmException {
        configRegion = configuration.getString(AtomHopperConfigurationKeys.ahusl_region);
        if (configRegion != null) {
            region = configRegion;
        }

        UsageV1Pojo usageV1 = new UsageV1Pojo();
        usageV1.setRegion(AHUSLUtil.mapRegion(region));

        usageV1.setVersion("1");//Rows are not updated...
        usageV1.setStartTime(AHUSLUtil.processCalendar(accountUsage.getStartTime()));
        usageV1.setEndTime(AHUSLUtil.processCalendar(accountUsage.getStartTime()));

        usageV1.setType(null); //No events
        usageV1.setTenantId(accountUsage.getAccountId().toString());
//        usageV1.setResourceId(accountUsage.getId().toString());
        usageV1.setDataCenter(DC.fromValue(configuration.getString(AtomHopperConfigurationKeys.ahusl_data_center)));

        //Generate UUID
        UUID uuid = UUIDUtil.genUUID(genUUIDString(accountUsage));
        usageV1.setId(uuid.toString());

        //LBaaS account usage
        AccountLBaaSUsagePojo ausage = new AccountLBaaSUsagePojo();
        ausage.setNumLoadbalancers(accountUsage.getNumLoadBalancers());
        ausage.setNumPublicVips(accountUsage.getNumPublicVips());
        ausage.setNumServicenetVips(accountUsage.getNumServicenetVips());
        ausage.setServiceCode(SERVICE_CODE);
//        ausage.setResourceType(com.rackspace.docs.usage.lbaas.account.ResourceTypes.TENANT);
        usageV1.getAny().add(ausage);


        return usageV1;
    }

    private static String genUUIDString(AccountUsage usageRecord) {
        return usageRecord.getId() + "_" + usageRecord.getAccountId() + "_" + region;
    }

    private static String genUUIDString(Usage usageRecord) {
        return usageRecord.getId() + "_" + usageRecord.getLoadbalancer().getId() + "_" + region;
    }

    public static EntryPojo buildUsageEntry(Usage usageRecord, Configuration configuration, String configRegion) throws NoSuchAlgorithmException, DatatypeConfigurationException {
        EntryPojo entry = buildEntry();

        UsageContent usageContent = new UsageContent();
        usageContent.setEvent(LbaasUsageDataMap.generateUsageEntry(configuration, configRegion, usageRecord));
        entry.setContent(usageContent);
        entry.getContent().setType(MediaType.APPLICATION_XML);

        entry.getCategory().add(buildUsageCategory());
        return entry;
    }

    public static EntryPojo buildAccountUsageEntry(AccountUsage accountUsage, Configuration configuration, String configRegion) throws NoSuchAlgorithmException, DatatypeConfigurationException {
        EntryPojo entry = buildEntry();

        UsageContent usageContent = new UsageContent();
        usageContent.setEvent(generateAccountUsageEntry(configuration, configRegion, accountUsage));
        entry.setContent(usageContent);
        entry.getContent().setType(MediaType.APPLICATION_XML);

        entry.getCategory().add(buildUsageCategory());
        return entry;
    }

    private static EntryPojo buildEntry() {
        EntryPojo entry = new EntryPojo();
        Title title = new Title();
        title.setType(Type.TEXT);
        title.setValue(lbaasTitle);
        entry.setTitle(title);
        return entry;
    }

    private static UsageCategory buildUsageCategory() {
        UsageCategory usageCategory = new UsageCategory();
        usageCategory.setLabel(label);
        usageCategory.setTerm("plain");
        return usageCategory;
    }
}
