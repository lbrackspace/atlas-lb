package org.openstack.atlas.api.helpers.reflection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.api.helpers.reflection.RegExForClassName;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UriClassDiscover {

    private static final Log LOG = LogFactory.getLog(UriClassDiscover.class);
    private static final List<RegExForClassName> rList;

    static {
        rList = new ArrayList<RegExForClassName>();

        // If you don't add new URI's to the following list then customers will get a 500 response on bad JSON.
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers(\\.json|\\.xml)?/?$", LoadBalancer.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+(\\.json|\\.xml)?/?$", LoadBalancer.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/accesslist(\\.json|\\.xml)?/?$", AccessList.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/accesslist/\\d+(\\.json|\\.xml)?/?$", NetworkItem.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/healthmonitor(\\.json|\\.xml)?/?$", HealthMonitor.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/nodes(\\.json|\\.xml)?/?$", Nodes.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/nodes/\\d+(\\.json|\\.xml)?/?$", Node.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/metadata(\\.json|\\.xml)?/?$", Metadata.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/metadata/\\d+(\\.json|\\.xml)?/?$", Meta.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/sessionpersistence(\\.json|\\.xml)?/?$", SessionPersistence.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/connectionthrottle(\\.json|\\.xml)?/?$", ConnectionThrottle.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/ssltermination(\\.json|\\.xml)?/?$", SslTermination.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/ssltermination/certificatemappings(\\.json|\\.xml)?/?$", CertificateMapping.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/ssltermination/certificatemappings/\\d+(\\.json|\\.xml)?/?$", CertificateMapping.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/\\d+/errorpage(\\.json|\\.xml)?/?$", Errorpage.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/bounce/certificatemappings(\\.json|\\.xml)?/?$", CertificateMapping.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/bounce/connectionthrottle(\\.json|\\.xml)?/?$", ConnectionThrottle.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/bounce/virtualip(\\.json|\\.xml)?/?$", VirtualIp.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/bounce/loadbalancer(\\.json|\\.xml)?/?$", LoadBalancer.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/bounce/updated(\\.json|\\.xml)?/?$", Updated.class));
        getrList().add(new RegExForClassName(".*\\d+/loadbalancers/bounce/ssltermination(\\.json|\\.xml)?/?$", SslTermination.class));
        getrList().add(new RegExForClassName(".*/management/hosts(\\.json|\\.xml)?/?$", Host.class));
    }

    public static Class getClassForUri(String uri) {
        Class classForUri = null;

        if (uri == null) {
            return classForUri;
        }
        for (RegExForClassName refcn : getrList()) {
            Pattern m = refcn.getPattern();
            if (m.matcher(uri).find()) {
                classForUri = refcn.getSomeClass();
                break;
            }
        }
        return classForUri; // If we don't know how to validate it return null.
    }

    public static List<RegExForClassName> getrList() {
        return rList;
    }

    public static String getRootElementNameForClass(String someClassName) {
        Class someClass;
        try {
            someClass = Class.forName(someClassName);
        } catch (ClassNotFoundException ex) {
            LOG.error(ex);
            return null;
        }
        return getRootElementNameForClass(someClass);
    }

    public static String getRootElementNameForClass(Class someClass) {
        String rootElement = null;
        XmlRootElement xmlRootElementAnnotation;
        if (someClass == null) {
            return null;
        }
        xmlRootElementAnnotation = (XmlRootElement) someClass.getAnnotation(XmlRootElement.class);
        if (xmlRootElementAnnotation != null) {
            rootElement = xmlRootElementAnnotation.name();
        }
        return rootElement;
    }


}
