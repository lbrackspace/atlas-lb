package org.openstack.atlas.api.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.openstack.atlas.api.helpers.JsonDeserializer.*;
import org.openstack.atlas.api.helpers.JsonSerializer.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.ContentCaching;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.w3.atom.Link;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class JsonObjectMapper extends ObjectMapper {

    public void init() {
        SerializationConfig serConf = this.getSerializationConfig();

        // Register our Custom Date (de)serializers
        registerModule(new GregorianDateSerializerModule(serConf, null));
        registerModule(new SimpleModule().addSerializer(GregorianCalendar.class,  new DateTimeSerializer(serConf, null)));
        registerModule(new SimpleModule().addDeserializer(Calendar.class,  new DateTimeDeserializer(Calendar.class)));

        // Register our Custom deserializers
        Class[] deserializerWrapperClasses = new Class[]{HostMachineDetails.class, AccountRecord.class, Node.class, HealthMonitor.class,
            SessionPersistence.class, ConnectionLogging.class, Meta.class, ConnectionThrottle.class, LoadBalancer.class,
            NetworkItem.class, RateLimit.class, Errorpage.class, SslTermination.class, CertificateMapping.class,
            Host.class, Link.class, AllowedDomain.class, ContentCaching.class};
//
        for (Class wrapperClass : deserializerWrapperClasses) {
            registerModule(new SimpleModule().addDeserializer(wrapperClass, new ObjectWrapperDeserializer(wrapperClass)));
        }

        // Load balancer is a bit of a special case since we want loadbalancer
        // wrapped, but none of the collections within loadbalancer. Jackson will properly unwrap arrays
        // by utilizing the WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED and associated configurations
        registerModule(new SimpleModule().addSerializer(LoadBalancer.class,  new ObjectWrapperSerializer(this.getSerializationConfig(), LoadBalancer.class)));
        registerModule(new SimpleModule().addSerializer(LoadBalancers.class,  new PropertyCollectionSerializer(serConf, LoadBalancers.class, "getLoadBalancers", true)));
        registerModule(new SimpleModule().addSerializer(org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers.class,
                new PropertyCollectionSerializer(serConf, org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers.class, "getLoadBalancers", false)));

        registerModules(new SimpleModule().addDeserializer(LoadBalancer.class, new ObjectWrapperDeserializer(LoadBalancer.class)));

        registerModule(new SimpleModule().addSerializer(AccessList.class, new PropertyCollectionSerializer(serConf, AccessList.class, "getNetworkItems")));
        registerModule(new SimpleModule().addSerializer(Nodes.class, new PropertyCollectionSerializer(serConf, Nodes.class, "getNodes")));
        registerModule(new SimpleModule().addSerializer(Metadata.class, new PropertyCollectionSerializer(serConf, Metadata.class, "getMetas")));
        registerModule(new SimpleModule().addDeserializer(Metadata.class, new PropertyListDeserializer(Metadata.class, Meta.class, "getMetas")));
        registerModule(new SimpleModule().addDeserializer(AccessList.class, new PropertyListDeserializer(AccessList.class, NetworkItem.class, "getNetworkItems")));


        // Suppress null properties from being serialized and indent output.
        this.enable(SerializationFeature.INDENT_OUTPUT);
        this.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
        this.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        this.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }
}
