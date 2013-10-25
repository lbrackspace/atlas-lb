package org.openstack.atlas.api.helpers;

import java.util.ArrayList;
import org.openstack.atlas.api.helpers.JsonUtils.JsonPublicDeserializers;
import org.openstack.atlas.api.helpers.JsonUtils.GenericJsonObjectMapperDeserializer;
import org.openstack.atlas.api.helpers.JsonUtils.GenericJsonObjectMapperSerializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ArrayNode;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.openstack.atlas.api.helpers.JsonDeserializer.DateTimeDeserializer;
import org.openstack.atlas.api.helpers.JsonDeserializer.DeserializerProviderBuilder;
import org.openstack.atlas.api.helpers.JsonDeserializer.ObjectWrapperDeserializer;
import org.openstack.atlas.api.helpers.JsonDeserializer.PropertyListDeserializer;
import org.openstack.atlas.api.helpers.JsonSerializer.DateTimeSerializer;
import org.openstack.atlas.api.helpers.JsonSerializer.ObjectWrapperSerializer;
import org.openstack.atlas.api.helpers.JsonSerializer.PropertyCollectionSerializer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomain;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.docs.loadbalancers.api.v1.ContentCaching;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.w3.atom.Link;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.openstack.atlas.api.helpers.JsonUtils.JsonPublicSerializers;
import org.openstack.atlas.util.debug.Debug;

public class JsonObjectMapper extends ObjectMapper {

    public void init() throws NoSuchMethodException {
        CustomSerializerFactory csf = new CustomSerializerFactory();
        CustomDeserializerFactory cdf = new CustomDeserializerFactory();
        SerializationConfig serConf = this.getSerializationConfig();
        DeserializationConfig deserConf = this.getDeserializationConfig();

        csf.addSpecificMapping(GregorianCalendar.class, new DateTimeSerializer(serConf, null));
        cdf.addSpecificMapping(Calendar.class, new DateTimeDeserializer(Calendar.class));


        Class[] serializerWrapperClasses = new Class[]{HealthMonitor.class,
            SessionPersistence.class, ConnectionLogging.class, ConnectionThrottle.class, Meta.class,
            Node.class, RateLimit.class, Errorpage.class, SslTermination.class, Link.class, AllowedDomain.class, ContentCaching.class};

        Class[] deserializerWrapperClasses = new Class[]{Node.class, HealthMonitor.class,
            SessionPersistence.class, ConnectionLogging.class, Meta.class,
            ConnectionThrottle.class, LoadBalancer.class, NetworkItem.class, RateLimit.class,
            Errorpage.class, SslTermination.class, Host.class, Link.class, AllowedDomain.class, ContentCaching.class};


        for (Class wrapperClass : serializerWrapperClasses) {
            csf.addSpecificMapping(wrapperClass, new ObjectWrapperSerializer(serConf, wrapperClass));
        }


        for (Class wrapperClass : deserializerWrapperClasses) {
            cdf.addSpecificMapping(wrapperClass, new ObjectWrapperDeserializer(wrapperClass));
        }

        cdf.addSpecificMapping(VirtualIps.class, new ObjectWrapperDeserializer(VirtualIps.class));
        cdf.addSpecificMapping(LoadBalancers.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeLoadBalancers", JsonNode.class)));
        cdf.addSpecificMapping(LoadBalancer.class, new GenericJsonObjectMapperDeserializer(JsonPublicDeserializers.class.getMethod("decodeLoadBalancer", ObjectNode.class)));
        // Define any collections utilizing the custom serializers above to
        // use the clean collections serializer, which will ensure proper JSON
        // formatting.

        // Load balancer is a bit of a special case since we want loadbalancer
        // wrapped, but none of the collections within loadbalancer.

        //csf.addSpecificMapping(LoadBalancer.class, new ObjectWrapperSerializer(this.getSerializationConfig(), LoadBalancer.class));
        csf.addSpecificMapping(LoadBalancer.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachLoadBalancer", ObjectNode.class, LoadBalancer.class, boolean.class), Boolean.TRUE));
        csf.addSpecificMapping(LoadBalancers.class, new GenericJsonObjectMapperSerializer(JsonPublicSerializers.class.getMethod("attachLoadBalancers", ObjectNode.class, LoadBalancers.class, boolean.class), Boolean.TRUE));


        csf.addSpecificMapping(AccessList.class, new PropertyCollectionSerializer(serConf, AccessList.class, "getNetworkItems"));
        csf.addSpecificMapping(Nodes.class, new PropertyCollectionSerializer(serConf, Nodes.class, "getNodes"));
        csf.addSpecificMapping(Metadata.class, new PropertyCollectionSerializer(serConf, Metadata.class, "getMetas"));

        cdf.addSpecificMapping(Metadata.class, new PropertyListDeserializer(Metadata.class, Meta.class, "getMetas"));
        cdf.addSpecificMapping(AccessList.class, new PropertyListDeserializer(AccessList.class, NetworkItem.class, "getNetworkItems"));
        cdf.addSpecificMapping(VirtualIps.class, new PropertyListDeserializer(VirtualIps.class, VirtualIp.class, "getVirtualIps"));


        this.setSerializerFactory(csf);
        this.setDeserializerProvider(new DeserializerProviderBuilder(cdf));
        // Suppress null properties from being serialized.
        this.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
        serConf.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }
}
