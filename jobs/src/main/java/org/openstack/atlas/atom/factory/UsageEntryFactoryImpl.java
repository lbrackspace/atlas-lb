package org.openstack.atlas.atom.factory;

import com.rackspace.docs.core.event.DC;
import com.rackspace.docs.core.event.EventType;
import com.rackspace.docs.core.event.V1Element;
import com.rackspace.docs.usage.lbaas.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.atom.util.UUIDUtil;
import org.openstack.atlas.atom.util.UsageMarshaller;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.entities.SslMode;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.w3._2005.atom.ObjectFactory;
import org.w3._2005.atom.*;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Used for mapping values from usage records to generate usage objects...
 */
public class UsageEntryFactoryImpl implements UsageEntryFactory {
    private static final Log LOG = LogFactory.getLog(UsageEntryFactoryImpl.class);

    private static String region = "GLOBAL";
    private static final String label = "loadBalancerUsage";
    private static final String lbaasTitle = "cloudLoadBalancers";
    private static String SERVICE_CODE = "CloudLoadBalancers";
    private static final String version = "1"; //SCHEMA VERSIONS found in META-INF/xml and META-INF/xsd

    @Override
    public Map<Object, Object> createEntry(Usage usageRecord, Configuration configuration, String configRegion) throws AtomHopperMappingException {
        UsageEntry entry = buildEntry();

        UsageContent usageContent = new ObjectFactory().createUsageContent();
        Map<Object, Object> map = new HashMap<Object, Object>();

        try {
            usageContent.setEvent(UsageEntryFactoryImpl.generateUsageEntry(configuration, configRegion, usageRecord));

            entry.setContent(usageContent);
            entry.getContent().setType(MediaType.APPLICATION_XML);

            entry.getCategory().add(buildUsageCategory());
            map.put("entrystring", UsageMarshaller.marshallResource(new ObjectFactory().createEntry(entry),
                    JAXBContext.newInstance("org.w3._2005.atom:com.rackspace.docs.usage.lbaas")).toString());
            map.put("entryobject", entry);
        } catch (Exception e) {
            LOG.error("Error mapping usage: " + e);
            throw new AtomHopperMappingException(e);
        }
        return map;
    }

    private static V1Element generateUsageEntry(Configuration configuration, String configRegion, Usage usageRecord) throws DatatypeConfigurationException, NoSuchAlgorithmException, JAXBException {
        configRegion = configuration.getString(AtomHopperConfigurationKeys.ahusl_region);
        if (configRegion != null) {
            region = configRegion;
        }

        V1Element usageV1 = new com.rackspace.docs.core.event.ObjectFactory().createV1Element();
        usageV1.setVersion(version);
        usageV1.setRegion(AHUSLUtil.mapRegion(region));
        usageV1.setTenantId(usageRecord.getAccountId().toString());
        usageV1.setResourceId(usageRecord.getLoadbalancer().getId().toString());
        usageV1.setResourceName(usageRecord.getLoadbalancer().getName());
        usageV1.setDataCenter(DC.fromValue(configuration.getString(AtomHopperConfigurationKeys.ahusl_data_center)));

        EventType usageRecordEventType = AHUSLUtil.mapEventType(usageRecord);
        if (usageRecordEventType != null && (usageRecordEventType.equals(EventType.DELETE))) {
            usageV1.setType(usageRecordEventType);
            usageV1.setEventTime(AHUSLUtil.processCalendar(usageRecord.getStartTime()));
        } else {
            usageV1.setType(EventType.USAGE);
            usageV1.setStartTime(AHUSLUtil.processCalendar(usageRecord.getStartTime()));
            usageV1.setEndTime(AHUSLUtil.processCalendar(usageRecord.getEndTime()));
        }

        //Generate UUID
        UUID uuid = UUIDUtil.genUUIDMD5Hash(genUUIDString(usageRecord));
        usageV1.setId(uuid.toString());

        if (usageRecord.getUuid() != null) {
            //This is an updated usage record, need the reference id from previous record
            usageV1.setReferenceId(usageRecord.getUuid());
        }

        //The Product usage entry for LBaaS
        usageV1.getAny().add(new com.rackspace.docs.usage.lbaas.ObjectFactory().createProduct(buildLbaasUsageRecord(usageRecord)));

        return usageV1;
    }

    private static String genUUIDString(Usage usageRecord) {
        return SERVICE_CODE + "_" + usageRecord.getId() + "_" + usageRecord.getLoadbalancer().getId() + "_" + region + "_" + Calendar.getInstance();
    }

    private static UsageEntry buildEntry() {
        UsageEntry entry = new ObjectFactory().createUsageEntry();
        Title title = new Title();
        title.setType(Type.TEXT);
        title.setValue(lbaasTitle);
        entry.setTitle(title);
        return entry;
    }

    private static UsageCategory buildUsageCategory() {
        UsageCategory usageCategory = new ObjectFactory().createUsageCategory();
        usageCategory.setLabel(label);
        usageCategory.setTerm("plain");
        return usageCategory;
    }

    private static CloudLoadBalancersType buildLbaasUsageRecord(Usage usageRecord) throws DatatypeConfigurationException {
        //LBAAS specific values
        CloudLoadBalancersType lu = new com.rackspace.docs.usage.lbaas.ObjectFactory().createCloudLoadBalancersType();
        lu.setAvgConcurrentConnections(usageRecord.getAverageConcurrentConnections());
        lu.setAvgConcurrentConnectionsSsl(usageRecord.getAverageConcurrentConnectionsSsl());
        lu.setBandWidthOutSsl(usageRecord.getOutgoingTransferSsl());
        lu.setBandWidthInSsl(usageRecord.getIncomingTransferSsl());
        lu.setBandWidthOut(usageRecord.getOutgoingTransfer());
        lu.setBandWidthIn(usageRecord.getIncomingTransfer());
        lu.setResourceType(ResourceTypes.LOADBALANCER);
        lu.setNumPolls(usageRecord.getNumberOfPolls());
        lu.setNumVips(usageRecord.getNumVips());
        lu.setServiceCode(SERVICE_CODE);
        lu.setVersion(version);

        StatusEnum status = (AHUSLUtil.mapEventType(usageRecord) != null && AHUSLUtil.mapEventType(usageRecord).equals(EventType.SUSPEND)) ? StatusEnum.SUSPENDED : StatusEnum.ACTIVE;
        lu.setStatus(status);

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
