package org.openstack.atlas.api.helpers;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.api.helpers.JsonSerializer.ObjectWrapperSerializer;
import org.openstack.atlas.api.helpers.JsonDeserializer.ObjectWrapperDeserializer;
import org.openstack.atlas.api.helpers.JsonDeserializer.PropertyListDeserializer;
import org.openstack.atlas.api.helpers.JsonSerializer.PropertyCollectionSerializer;
import org.openstack.atlas.api.helpers.JsonSerializer.DateTimeSerializer;
import org.openstack.atlas.api.helpers.JsonDeserializer.DateTimeDeserializer;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.docs.loadbalancers.api.v1.Updated;
import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.api.helpers.JsonDeserializer.DeserializerProviderBuilder;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;

public class JsonObjectMapper extends ObjectMapper {

    public void init() {
        CustomSerializerFactory csf = new CustomSerializerFactory();
        CustomDeserializerFactory cdf = new CustomDeserializerFactory();
        SerializationConfig serConf = this.getSerializationConfig();
        DeserializationConfig deserConf = this.getDeserializationConfig();

        csf.addSpecificMapping(GregorianCalendar.class, new DateTimeSerializer(serConf, null));
        cdf.addSpecificMapping(Calendar.class, new DateTimeDeserializer(Calendar.class));


        Class[] serializerWrapperClasses = new Class[]{HealthMonitor.class,
            SessionPersistence.class, ConnectionLogging.class, ConnectionThrottle.class,
            Node.class, RateLimit.class, Errorpage.class,SslTermination.class};

        Class[] deserializerWrapperClasses = new Class[]{Node.class, HealthMonitor.class,
            SessionPersistence.class, ConnectionLogging.class,
            ConnectionThrottle.class, LoadBalancer.class, NetworkItem.class, RateLimit.class,
            Errorpage.class, SslTermination.class};


        for (Class wrapperClass : serializerWrapperClasses) {
            csf.addSpecificMapping(wrapperClass, new ObjectWrapperSerializer(serConf, wrapperClass));
        }


        for (Class wrapperClass : deserializerWrapperClasses) {
            cdf.addSpecificMapping(wrapperClass, new ObjectWrapperDeserializer(wrapperClass));
        }

        cdf.addSpecificMapping(LoadBalancer.class, new ObjectWrapperDeserializer(LoadBalancer.class));
        // Define any collections utilizing the custom serializers above to
        // use the clean collections serializer, which will ensure proper JSON
        // formatting.


        // Load balancer is a bit of a special case since we want loadbalancer
        // wrapped, but none of the collections within loadbalancer.

        csf.addSpecificMapping(LoadBalancer.class, new ObjectWrapperSerializer(this.getSerializationConfig(), LoadBalancer.class));
        csf.addSpecificMapping(LoadBalancers.class, new PropertyCollectionSerializer(serConf, LoadBalancers.class, "getLoadBalancers"));

        csf.addSpecificMapping(AccessList.class, new PropertyCollectionSerializer(serConf, AccessList.class, "getNetworkItems"));
        csf.addSpecificMapping(Nodes.class, new PropertyCollectionSerializer(serConf, Nodes.class, "getNodes"));

        cdf.addSpecificMapping(AccessList.class, new PropertyListDeserializer(AccessList.class, NetworkItem.class, "getNetworkItems"));


        this.setSerializerFactory(csf);
        this.setDeserializerProvider(new DeserializerProviderBuilder(cdf));
        // Suppress null properties from being serialized.
        this.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
        serConf.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }
}
