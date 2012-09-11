package org.openstack.atlas.atom.util;

import com.rackspace.docs.core.event.EventType;
import com.rackspace.docs.usage.lbaas.ResourceTypes;
import com.rackspace.docs.usage.lbaas.SslModeEnum;
import com.rackspace.docs.usage.lbaas.VipTypeEnum;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.pojo.LBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;
import org.openstack.atlas.cfg.Configuration;
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
    private static final String version = "1";

    public static EntryPojo buildUsageEntry(Usage usageRecord, Configuration configuration, String configRegion) throws NoSuchAlgorithmException, DatatypeConfigurationException {
        EntryPojo entry = buildEntry();

        UsageContent usageContent = new UsageContent();
        usageContent.setEvent(LbaasUsageDataMap.generateUsageEntry(configuration, configRegion, usageRecord));
        entry.setContent(usageContent);
        entry.getContent().setType(MediaType.APPLICATION_XML);

        entry.getCategory().add(buildUsageCategory());
        return entry;
    }

    private static UsageV1Pojo generateUsageEntry(Configuration configuration, String configRegion, Usage usageRecord) throws DatatypeConfigurationException, NoSuchAlgorithmException {
        configRegion = configuration.getString(AtomHopperConfigurationKeys.ahusl_region);
        if (configRegion != null) {
            region = configRegion;
        }

        UsageV1Pojo usageV1 = new UsageV1Pojo();
        usageV1.setRegion(AHUSLUtil.mapRegion(region));

        usageV1.setVersion(version);
        usageV1.setStartTime(AHUSLUtil.processCalendar(usageRecord.getStartTime()));

        if (AHUSLUtil.mapEventType(usageRecord) == null) {
            usageV1.setType(EventType.USAGE);
        } else {
            usageV1.setType(AHUSLUtil.mapEventType(usageRecord));
        }

        usageV1.setTenantId(usageRecord.getAccountId().toString());
        usageV1.setResourceId(usageRecord.getLoadbalancer().getId().toString());

        //Generate UUID
        UUID uuid = UUIDUtil.genUUIDMD5(genUUIDString(usageRecord));
        usageV1.setId(uuid.toString());

        usageV1.getAny().add(buildLbaas(usageRecord));
        return usageV1;
    }

    private static String genUUIDString(Usage usageRecord) {
        return usageRecord.getId() + "_" + usageRecord.getLoadbalancer().getId() + "_" + region;
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

    private static LBaaSUsagePojo buildLbaas(Usage usageRecord) {
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
//        lu.setVersion(usageRecord.getEntryVersion().toString());
        lu.setVersion(version);

        BitTags bitTags = new BitTags(usageRecord.getTags());
        if (bitTags.isTagOn(BitTag.SERVICENET_LB)) {
            lu.setVipType(VipTypeEnum.SERVICENET);
        } else {
            lu.setVipType(VipTypeEnum.PUBLIC);
        }

        lu.setSslMode(SslModeEnum.fromValue(SslMode.getMode(bitTags).name()));
        return lu;
    }
}
