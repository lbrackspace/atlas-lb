package org.openstack.atlas.api.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.openstack.atlas.api.helpers.JsonDeserializer.*;
import org.openstack.atlas.api.helpers.JsonSerializer.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.w3.atom.Link;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostMachineDetails;

public class JsonObjectMapper extends ObjectMapper {

    public void init() {
//        CustomSerializerFactory csf = new CustomSerializerFactory();
//        CustomDeserializerFactory cdf = new CustomDeserializerFactory();
        SerializationConfig serConf = this.getSerializationConfig();
//        DeserializationConfig deserConf = this.getDeserializationConfig();




        registerModule(new GregorianDateSerializerModule(serConf, null));
        registerModule(new SimpleModule().addSerializer(GregorianCalendar.class,  new DateTimeSerializer(serConf, null)));
        registerModule(new SimpleModule().addDeserializer(Calendar.class,  new DateTimeDeserializer(Calendar.class)));


        Class[] serializerWrapperClasses = new Class[]{HostMachineDetails.class, AccountRecord.class, HealthMonitor.class,
            SessionPersistence.class, ConnectionLogging.class, ConnectionThrottle.class, Meta.class,
            Node.class, RateLimit.class, Errorpage.class, SslTermination.class, CertificateMapping.class,
            Link.class, AllowedDomain.class, ContentCaching.class};

        Class[] deserializerWrapperClasses = new Class[]{HostMachineDetails.class, AccountRecord.class, Node.class, HealthMonitor.class,
            SessionPersistence.class, ConnectionLogging.class, Meta.class, ConnectionThrottle.class, LoadBalancer.class,
            NetworkItem.class, RateLimit.class, Errorpage.class, SslTermination.class, CertificateMapping.class,
            Host.class, Link.class, AllowedDomain.class, ContentCaching.class};


        for (Class wrapperClass : serializerWrapperClasses) {
            registerModule(new SimpleModule().addSerializer(wrapperClass,  new ObjectWrapperSerializer(serConf, wrapperClass)));
        }


        for (Class wrapperClass : deserializerWrapperClasses) {
            registerModule(new SimpleModule().addDeserializer(wrapperClass, new ObjectWrapperDeserializer(wrapperClass)));
        }

//        registerModule(new ObjectWrapperSerializerModule(serConf, LoadBalancer.class));
        // Define any collections utilizing the custom serializers above to
        // use the clean collections serializer, which will ensure proper JSON
        // formatting.

        // Load balancer is a bit of a special case since we want loadbalancer
        // wrapped, but none of the collections within loadbalancer.

        registerModule(new SimpleModule().addSerializer(LoadBalancer.class,  new ObjectWrapperSerializer(serConf, LoadBalancer.class)));
        registerModule(new SimpleModule().addSerializer(LoadBalancers.class,  new PropertyCollectionSerializer(serConf, LoadBalancers.class, "getLoadBalancers", true)));


        registerModule(new SimpleModule().addSerializer(AccessList.class, new PropertyCollectionSerializer(serConf, AccessList.class, "getNetworkItems")));
        registerModule(new SimpleModule().addSerializer(Nodes.class, new PropertyCollectionSerializer(serConf, Nodes.class, "getNodes")));
        registerModule(new SimpleModule().addSerializer(Metadata.class, new PropertyCollectionSerializer(serConf, Metadata.class, "getMetas")));

        registerModule(new SimpleModule().addDeserializer(Metadata.class, new PropertyListDeserializer(Metadata.class, Meta.class, "getMetas")));
        registerModule(new SimpleModule().addDeserializer(AccessList.class, new PropertyListDeserializer(AccessList.class, NetworkItem.class, "getNetworkItems")));



        // Suppress null properties from being serialized.
//        enable(SerializationFeature.INDENT_OUTPUT);
//        this.configure(Serialization.Feature.WRITE_NULL_PROPERTIES, false);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        serConf.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }
}
