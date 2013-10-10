package org.openstack.atlas.atomhopper.factory;

import com.rackspace.docs.core.event.DC;
import com.rackspace.docs.core.event.EventType;
import com.rackspace.docs.core.event.Region;
import com.rackspace.docs.core.event.V1Element;
import com.rackspace.docs.usage.lbaas.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atomhopper.exception.AtomHopperMappingException;
import org.openstack.atlas.atomhopper.marshaller.UsageMarshaller;
import org.openstack.atlas.atomhopper.util.AHUSLServiceUtil;
import org.openstack.atlas.atomhopper.util.UUIDUtil;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfiguration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.SslMode;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.w3._2005.atom.*;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Used for mapping values from usage records to generate usage objects...
 */
public class UsageEntryFactoryImpl implements UsageEntryFactory {
    private static final Log LOG = LogFactory.getLog(UsageEntryFactoryImpl.class);

    private static final String USAGE_LABEL = "loadBalancerUsage";
    private static final String LBAAS_TITLE = "cloudLoadBalancers";
    private static final String SERVICE_CODE = "CloudLoadBalancers";
    private static final String USAGE_VERSION = "1";

    protected Configuration atomHopperConfig;

    private org.w3._2005.atom.ObjectFactory usageEntryFactory;
    private com.rackspace.docs.usage.lbaas.ObjectFactory lbUsageFactory;
    private com.rackspace.docs.event.lbaas.delete.ObjectFactory lbDeleteFactory;
    private com.rackspace.docs.core.event.ObjectFactory v1EventFactory;


    public UsageEntryFactoryImpl() {
        this.atomHopperConfig = new AtomHopperConfiguration();
        this.usageEntryFactory = new org.w3._2005.atom.ObjectFactory();
        this.lbUsageFactory = new com.rackspace.docs.usage.lbaas.ObjectFactory();
        this.lbDeleteFactory = new com.rackspace.docs.event.lbaas.delete.ObjectFactory();
        this.v1EventFactory = new com.rackspace.docs.core.event.ObjectFactory();
    }

    @Override
    public Map<Object, Object> createEntry(Usage usageRecord) throws AtomHopperMappingException {
        try {
            UsageEntry entry = buildEntry();
            entry.getCategory().add(buildUsageCategory());
            entry.setContent(generateUsageContent(generateV1Event(usageRecord), MediaType.APPLICATION_XML));
            return generateUsageEntryMap(entry);
        } catch (Exception e) {
            LOG.error("Error mapping usage: " + e);
            throw new AtomHopperMappingException(e);
        }
    }

    private V1Element generateV1Event(Usage usageRecord) throws DatatypeConfigurationException, NoSuchAlgorithmException, JAXBException {
        V1Element usageV1 = generateBaseUsageEvent(usageRecord);
        usageV1.setId(genUUIDObject(usageRecord).toString());

        if (usageRecord.getUuid() != null && usageRecord.isCorrected()) {
            //This is an updated usage record, need the reference id from previous record
            usageV1.setReferenceId(usageRecord.getUuid());
        }

        usageV1.setTenantId(usageRecord.getAccountId().toString());
        usageV1.setResourceId(usageRecord.getLoadbalancer().getId().toString());
        usageV1.setResourceName(usageRecord.getLoadbalancer().getName());
        EventType usageRecordEventType = mapEventType(usageRecord);
        if (usageRecordEventType != null && (usageRecordEventType.equals(EventType.DELETE))) {
            usageV1.setEventTime(AHUSLServiceUtil.processCalendar(usageRecord.getStartTime()));
            usageV1.setType(EventType.DELETE);
            usageV1.getAny().add(lbDeleteFactory.createProduct(buildLbaasUsageRecordDelete()));
        } else {
            usageV1.setType(EventType.USAGE);
            usageV1.setStartTime(AHUSLServiceUtil.processCalendar(usageRecord.getStartTime()));
            usageV1.setEndTime(AHUSLServiceUtil.processCalendar(usageRecord.getEndTime()));
            usageV1.getAny().add(lbUsageFactory.createProduct(buildLbaasUsageRecord(usageRecord)));
        }
        return usageV1;
    }

    private V1Element generateBaseUsageEvent(Usage usageRecord) throws DatatypeConfigurationException {
        V1Element baseEvent = v1EventFactory.createV1Element();
        baseEvent.setVersion(USAGE_VERSION);
        baseEvent.setRegion(mapRegion(atomHopperConfig.getString(AtomHopperConfigurationKeys.ahusl_region)));
        baseEvent.setDataCenter(DC.fromValue(atomHopperConfig.getString(AtomHopperConfigurationKeys.ahusl_data_center)));
        return baseEvent;
    }

    private CloudLoadBalancersType buildLbaasUsageRecord(Usage usageRecord) throws DatatypeConfigurationException {
        CloudLoadBalancersType lbaasUsage = lbUsageFactory.createCloudLoadBalancersType();
        lbaasUsage.setAvgConcurrentConnections(usageRecord.getAverageConcurrentConnections());
        lbaasUsage.setAvgConcurrentConnectionsSsl(usageRecord.getAverageConcurrentConnectionsSsl());
        lbaasUsage.setBandWidthOutSsl(usageRecord.getOutgoingTransferSsl());
        lbaasUsage.setBandWidthInSsl(usageRecord.getIncomingTransferSsl());
        lbaasUsage.setBandWidthOut(usageRecord.getOutgoingTransfer());
        lbaasUsage.setBandWidthIn(usageRecord.getIncomingTransfer());
        lbaasUsage.setResourceType(ResourceTypes.LOADBALANCER);
        lbaasUsage.setNumPolls(usageRecord.getNumberOfPolls());
        lbaasUsage.setNumVips(usageRecord.getNumVips());
        lbaasUsage.setServiceCode(SERVICE_CODE);
        lbaasUsage.setVersion(USAGE_VERSION);

        StatusEnum status = (mapEventType(usageRecord) != null
                && mapEventType(usageRecord).equals(EventType.SUSPEND))
                ? StatusEnum.SUSPENDED : StatusEnum.ACTIVE;
        lbaasUsage.setStatus(status);

        BitTags bitTags = new BitTags(usageRecord.getTags());
        if (bitTags.isTagOn(BitTag.SERVICENET_LB)) {
            lbaasUsage.setVipType(VipTypeEnum.SERVICENET);
        } else {
            lbaasUsage.setVipType(VipTypeEnum.PUBLIC);
        }
        lbaasUsage.setSslMode(SslModeEnum.fromValue(SslMode.getMode(bitTags).name()));

        return lbaasUsage;
    }

    private com.rackspace.docs.event.lbaas.delete.CloudLoadBalancersType buildLbaasUsageRecordDelete() throws DatatypeConfigurationException {
        com.rackspace.docs.event.lbaas.delete.CloudLoadBalancersType lbaasUsage = lbDeleteFactory.createCloudLoadBalancersType();
        lbaasUsage.setResourceType(com.rackspace.docs.event.lbaas.delete.ResourceTypes.LOADBALANCER);
        lbaasUsage.setServiceCode(SERVICE_CODE);
        lbaasUsage.setVersion(USAGE_VERSION);

        return lbaasUsage;
    }


    private UsageContent generateUsageContent(V1Element v1Element, String contentType) {
        UsageContent content = usageEntryFactory.createUsageContent();
        content.setEvent(v1Element);
        content.setType(contentType);
        return content;
    }

    private String genUUIDString(Usage usageRecord) {
        return SERVICE_CODE
                + "_" + usageRecord.getLoadbalancer().getId()
                + "_" + usageRecord.getStartTime()
                + "_" + usageRecord.getEndTime()
                + "_" + atomHopperConfig.getString(AtomHopperConfigurationKeys.ahusl_region)
                + "_" + usageRecord.getEntryVersion();
    }

    private UsageEntry buildEntry() {
        UsageEntry entry = usageEntryFactory.createUsageEntry();
        Title title = new Title();
        title.setType(Type.TEXT);
        title.setValue(LBAAS_TITLE);
        entry.setTitle(title);
        return entry;
    }

    private UUID genUUIDObject(Usage usageRecord) throws NoSuchAlgorithmException {
        return UUIDUtil.genUUIDMD5Hash(genUUIDString(usageRecord));
    }

    private UsageCategory buildUsageCategory() {
        UsageCategory usageCategory = usageEntryFactory.createUsageCategory();
        usageCategory.setLabel(USAGE_LABEL);
        usageCategory.setTerm("plain");
        return usageCategory;
    }

    private Map<Object, Object> generateUsageEntryMap(UsageEntry usageEntry) throws JAXBException {
        Map<Object, Object> map = new HashMap<Object, Object>();
        JAXBContext context;
        if (usageEntry.getContent().getEvent().getType().equals(EventType.DELETE)) {
               context = JAXBContext.newInstance("org.w3._2005.atom:com.rackspace.docs.event.lbaas.delete");
        } else {
            context = JAXBContext.newInstance("org.w3._2005.atom:com.rackspace.docs.usage.lbaas");
        }
        map.put("entrystring", UsageMarshaller
                .marshallResource(usageEntryFactory.createEntry(usageEntry), context).toString());
        map.put("entryobject", usageEntry);
        return map;
    }


    /**
     * This method maps string events to EventType
     *
     * @param usageRecord
     * @return
     * @throws javax.xml.datatype.DatatypeConfigurationException
     */
    public static EventType mapEventType(Usage usageRecord) throws DatatypeConfigurationException {
        if (usageRecord.getEventType() != null) {
            if (usageRecord.getEventType().equals(UsageEvent.CREATE_LOADBALANCER.name())) {
                return EventType.CREATE;
            } else if (usageRecord.getEventType().equals(UsageEvent.DELETE_LOADBALANCER.name())) {
                return EventType.DELETE;
            } else if (usageRecord.getEventType().equals(UsageEvent.SUSPEND_LOADBALANCER.name())) {
                return EventType.SUSPEND;
            } else if (usageRecord.getEventType().equals(UsageEvent.SUSPENDED_LOADBALANCER.name())) {
                return EventType.SUSPEND;
            } else if (usageRecord.getEventType().equals(UsageEvent.UNSUSPEND_LOADBALANCER.name())) {
                return EventType.UNSUSPEND;
            }
        }
        return null;
    }

    public static Region mapRegion(String configRegion) {
        if (configRegion.equals("DFW")) {
            return Region.DFW;
        } else if (configRegion.equals("ORD")) {
            return Region.ORD;
        } else if (configRegion.equals("LON")) {
            return Region.LON;
        } else if (configRegion.equals("SYD")) {
            return Region.SYD;
        } else if (configRegion.equals("IAD")) {
            return Region.IAD;
        } else if (configRegion.equals("HKG")) {
            return Region.HKG;
        } else {
            LOG.error("Region could not be mapped from config, using default");
            return Region.GLOBAL;
        }
    }
}
