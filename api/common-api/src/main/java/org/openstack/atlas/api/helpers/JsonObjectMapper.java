package org.openstack.atlas.api.helpers;

import org.openstack.atlas.api.helpers.JsonUtils.GenericJsonListMapperSerializer;
import org.openstack.atlas.api.helpers.JsonUtils.JsonPublicDeserializers;
import org.openstack.atlas.api.helpers.JsonUtils.GenericJsonObjectMapperDeserializer;
import org.openstack.atlas.api.helpers.JsonUtils.GenericJsonObjectMapperSerializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.openstack.atlas.api.helpers.JsonDeserializer.DateTimeDeserializer;
import org.openstack.atlas.api.helpers.JsonDeserializer.DeserializerProviderBuilder;
import org.openstack.atlas.api.helpers.JsonDeserializer.ObjectWrapperDeserializer;
import org.openstack.atlas.api.helpers.JsonSerializer.DateTimeSerializer;
import org.openstack.atlas.api.helpers.JsonSerializer.ObjectWrapperSerializer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccountBilling;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccountUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomain;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomains;
import org.openstack.atlas.docs.loadbalancers.api.v1.Cluster;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.docs.loadbalancers.api.v1.ContentCaching;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvent;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.docs.loadbalancers.api.v1.SourceAddresses;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.w3.atom.Link;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.openstack.atlas.api.helpers.JsonUtils.JsonPublicSerializers;

public class JsonObjectMapper extends ObjectMapper {

    public void init() throws NoSuchMethodException {
        CustomSerializerFactory csf = new CustomSerializerFactory();
        CustomDeserializerFactory cdf = new CustomDeserializerFactory();
        GenericJsonListMapperSerializer cls = new GenericJsonListMapperSerializer();
//        SerializationConfig serConf = this.getSerializationConfig();
//        DeserializationConfig deserConf = this.getDeserializationConfig();

//        csf.addSpecificMapping(GregorianCalendar.class, new DateTimeSerializer(serConf, null));
        cdf.addSpecificMapping(Calendar.class, new DateTimeDeserializer(Calendar.class));
        cls.addToMap(AccountBilling.class, "attachAccountBillings");
        cls.addToMap(AccountUsageRecord.class, "attachAccountUsageRecords");
        cls.addToMap(LoadBalancerUsage.class, "attachLoadBalancerUsages");
        cls.addToMap(LoadBalancerUsageRecord.class, "attachLoadBalancerUsageRecords");


//        Class[] serializerWrapperClasses = new Class[]{HealthMonitor.class,
//            SessionPersistence.class, ConnectionLogging.class, ConnectionThrottle.class, Meta.class,
//            Node.class, RateLimit.class, Errorpage.class, SslTermination.class, Link.class, AllowedDomain.class, ContentCaching.class};
//
//        Class[] deserializerWrapperClasses = new Class[]{Node.class, HealthMonitor.class,
//            SessionPersistence.class, ConnectionLogging.class, Meta.class,
//            ConnectionThrottle.class, LoadBalancer.class, NetworkItem.class, RateLimit.class,
//            Errorpage.class, SslTermination.class, Host.class, Link.class, AllowedDomain.class, ContentCaching.class};
//
//
//        for (Class wrapperClass : serializerWrapperClasses) {
//            csf.addSpecificMapping(wrapperClass, new ObjectWrapperSerializer(serConf, wrapperClass));
//        }
//
//
//        for (Class wrapperClass : deserializerWrapperClasses) {
//            cdf.addSpecificMapping(wrapperClass, new ObjectWrapperDeserializer(wrapperClass));
//        }

        cdf.addSpecificMapping(Errorpage.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeErrorpage", ObjectNode.class)));
        cdf.addSpecificMapping(Meta.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeMeta", ObjectNode.class)));
        cdf.addSpecificMapping(Metadata.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeMetadata", JsonNode.class)));
        cdf.addSpecificMapping(AccessList.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeAccessList", JsonNode.class)));
        cdf.addSpecificMapping(ContentCaching.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeContentCaching", ObjectNode.class)));
        cdf.addSpecificMapping(ConnectionThrottle.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeConnectionThrottle", ObjectNode.class)));
        cdf.addSpecificMapping(ConnectionLogging.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeConnectionLogging", ObjectNode.class)));
        cdf.addSpecificMapping(HealthMonitor.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeHealthMonitor", ObjectNode.class)));
        cdf.addSpecificMapping(SessionPersistence.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeSessionPersistence", ObjectNode.class)));
        cdf.addSpecificMapping(SslTermination.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeSslTermination", ObjectNode.class)));
        cdf.addSpecificMapping(Node.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeNode", ObjectNode.class)));
        cdf.addSpecificMapping(Nodes.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeNodes", JsonNode.class)));
        cdf.addSpecificMapping(VirtualIp.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeVirtualIp", ObjectNode.class)));
        cdf.addSpecificMapping(VirtualIps.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeVirtualIps", JsonNode.class)));
        cdf.addSpecificMapping(LoadBalancer.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeLoadBalancer", ObjectNode.class)));
        cdf.addSpecificMapping(LoadBalancers.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeLoadBalancers", JsonNode.class)));

        csf.addSpecificMapping(ArrayList.class, cls);
        csf.addSpecificMapping(AccountBilling.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachAccountBilling", ObjectNode.class, AccountBilling.class), null));
        csf.addSpecificMapping(Errorpage.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachErrorpage", ObjectNode.class, Errorpage.class), null));
        csf.addSpecificMapping(AllowedDomain.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachAllowedDomain", ObjectNode.class, AllowedDomain.class), null));
        csf.addSpecificMapping(AllowedDomains.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachAllowedDomains", ObjectNode.class, AllowedDomains.class), null));
        csf.addSpecificMapping(Meta.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachMeta", ObjectNode.class, Meta.class), null));
        csf.addSpecificMapping(Metadata.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachMetadata", ObjectNode.class, Metadata.class, boolean.class), Boolean.TRUE));
        csf.addSpecificMapping(NetworkItem.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachNetworkItem", ObjectNode.class, NetworkItem.class), null));
        csf.addSpecificMapping(AccessList.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachAccessList", ObjectNode.class, AccessList.class, boolean.class), Boolean.TRUE));
        csf.addSpecificMapping(Cluster.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachCluster", ObjectNode.class, Cluster.class), null));
        csf.addSpecificMapping(SourceAddresses.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachSourceAddresses", ObjectNode.class, SourceAddresses.class), null));
        csf.addSpecificMapping(ContentCaching.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachContentCaching", ObjectNode.class, ContentCaching.class), null));
        csf.addSpecificMapping(ConnectionLogging.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachConnectionLogging", ObjectNode.class, ConnectionLogging.class), null));
        csf.addSpecificMapping(HealthMonitor.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachHealthMonitor", ObjectNode.class, HealthMonitor.class), null));
        csf.addSpecificMapping(SessionPersistence.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachSessionPersistence", ObjectNode.class, SessionPersistence.class), null));
        csf.addSpecificMapping(SslTermination.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachSslTermination", ObjectNode.class, SslTermination.class), null));
        csf.addSpecificMapping(NodeServiceEvent.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachNodeServiceEvent", ObjectNode.class, NodeServiceEvent.class), null));
        csf.addSpecificMapping(NodeServiceEvents.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachNodeServiceEvents", ObjectNode.class, NodeServiceEvents.class, boolean.class), Boolean.TRUE));
        csf.addSpecificMapping(Node.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachNode", ObjectNode.class, Node.class, boolean.class), Boolean.TRUE));
        csf.addSpecificMapping(Nodes.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachNodes", ObjectNode.class, Nodes.class, boolean.class), Boolean.TRUE));
        csf.addSpecificMapping(VirtualIp.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachVirtualIp", ObjectNode.class, VirtualIp.class, boolean.class), Boolean.TRUE));
        csf.addSpecificMapping(VirtualIps.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachVirtualIps", ObjectNode.class, VirtualIps.class, boolean.class), Boolean.TRUE));
        csf.addSpecificMapping(LoadBalancer.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachLoadBalancer", ObjectNode.class, LoadBalancer.class, boolean.class), Boolean.TRUE));
        csf.addSpecificMapping(LoadBalancers.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachLoadBalancers", ObjectNode.class, LoadBalancers.class, boolean.class), Boolean.TRUE));


//        csf.addSpecificMapping(AccessList.class, new PropertyCollectionSerializer(serConf, AccessList.class, "getNetworkItems"));
//        csf.addSpecificMapping(Nodes.class, new PropertyCollectionSerializer(serConf, Nodes.class, "getNodes"));
//        csf.addSpecificMapping(Metadata.class, new PropertyCollectionSerializer(serConf, Metadata.class, "getMetas"));
//        cdf.addSpecificMapping(Metadata.class, new PropertyListDeserializer(Metadata.class, Meta.class, "getMetas"));
//        cdf.addSpecificMapping(AccessList.class, new PropertyListDeserializer(AccessList.class, NetworkItem.class, "getNetworkItems"));
//        cdf.addSpecificMapping(VirtualIps.class, new PropertyListDeserializer(VirtualIps.class, VirtualIp.class, "getVirtualIps"));


        this.setSerializerFactory(csf);
        this.setDeserializerProvider(new DeserializerProviderBuilder(cdf));
        // Suppress null properties from being serialized.
        this.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
//        serConf.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }
}
