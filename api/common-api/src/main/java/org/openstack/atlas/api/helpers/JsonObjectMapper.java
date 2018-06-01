package org.openstack.atlas.api.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.openstack.atlas.api.helpers.JsonDeserializer.DateTimeDeserializer;
import org.openstack.atlas.api.helpers.JsonDeserializer.ObjectWrapperDeserializer;
import org.openstack.atlas.api.helpers.JsonDeserializer.PropertyListDeserializer;
import org.openstack.atlas.api.helpers.JsonSerializer.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostMachineDetails;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.w3.atom.Link;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class JsonObjectMapper extends ObjectMapper {

    public void init() {
        // Suppress null properties from being serialized and indent output.
        this.enable(SerializationFeature.INDENT_OUTPUT);
        this.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //this.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        this.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        this.setDateFormat(new ISO8601DateFormat());
        SerializationConfig serConf = this.getSerializationConfig();

        // Register our Custom Date (de)serializers
        registerModule(new GregorianDateSerializerModule(serConf, null));
        registerModule(new SimpleModule().addSerializer(GregorianCalendar.class,  new DateTimeSerializer(serConf, null)));
        registerModule(new SimpleModule().addDeserializer(Calendar.class,  new DateTimeDeserializer(Calendar.class)));


        Class[] serializerWrapperClasses = new Class[]{HostMachineDetails.class, AccountRecord.class, HealthMonitor.class,
                SessionPersistence.class, ConnectionLogging.class, ConnectionThrottle.class, Meta.class, Node.class,
                RateLimit.class, Errorpage.class, SslTermination.class, CertificateMapping.class,
                Link.class, AllowedDomain.class, ContentCaching.class};

        // Register our Custom deserializers
        Class[] deserializerWrapperClasses = new Class[]{HostMachineDetails.class, AccountRecord.class, Node.class, HealthMonitor.class,
            SessionPersistence.class, ConnectionLogging.class, Meta.class, ConnectionThrottle.class, LoadBalancer.class,
            NetworkItem.class, RateLimit.class, Errorpage.class, SslTermination.class, CertificateMapping.class,
            Host.class, Link.class, AllowedDomain.class, ContentCaching.class};
//
        for (Class wrapperClass : deserializerWrapperClasses) {
            registerModule(new SimpleModule().addDeserializer(wrapperClass, new ObjectWrapperDeserializer(wrapperClass)));
        }

        for (Class wrapperClass : serializerWrapperClasses) {
            registerModule(new SimpleModule().addSerializer(wrapperClass, new ObjectWrapperSerializer(serConf, wrapperClass)));
        }

        // Load balancer is a bit of a special case since we want loadbalancer
        // wrapped, but none of the collections within loadbalancer.
        registerModule(new SimpleModule().addSerializer(LoadBalancer.class,  new LoadbalancerWrapperSerializer(serConf, LoadBalancer.class)));
        registerModule(new SimpleModule().addSerializer(LoadBalancers.class,  new PropertyCollectionSerializer(serConf, LoadBalancers.class, "getLoadBalancers", true)));

        registerModules(new SimpleModule().addDeserializer(LoadBalancer.class, new ObjectWrapperDeserializer(LoadBalancer.class)));

        registerModule(new SimpleModule().addSerializer(AccessList.class, new PropertyCollectionSerializer(serConf, AccessList.class, "getNetworkItems")));
        registerModule(new SimpleModule().addDeserializer(AccessList.class, new PropertyListDeserializer(AccessList.class, NetworkItem.class, "getNetworkItems")));

        registerModule(new SimpleModule().addSerializer(Nodes.class, new PropertyCollectionSerializer(serConf, Nodes.class, "getNodes")));
        registerModule(new SimpleModule().addSerializer(Metadata.class, new PropertyCollectionSerializer(serConf, Metadata.class, "getMetas")));
        registerModule(new SimpleModule().addDeserializer(Metadata.class, new PropertyListDeserializer(Metadata.class, Meta.class, "getMetas")));


        registerModule(new SimpleModule().addSerializer(VirtualIps.class, new PropertyCollectionSerializer(serConf, VirtualIps.class, "getVirtualIps")));
        registerModule(new SimpleModule().addSerializer(AllowedDomains.class, new PropertyCollectionSerializer(serConf, AllowedDomains.class, "getAllowedDomains")));
        registerModule(new SimpleModule().addSerializer(CertificateMappings.class, new PropertyCollectionSerializer(serConf, CertificateMappings.class, "getCertificateMappings")));
    }
}
