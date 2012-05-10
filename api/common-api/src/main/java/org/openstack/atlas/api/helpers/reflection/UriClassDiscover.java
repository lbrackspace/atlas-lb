package org.openstack.atlas.api.helpers.reflection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UriClassDiscover {

    private static final Log LOG = LogFactory.getLog(UriClassDiscover.class);

    private static final List<REFCN> rList;

    static {
        rList = new ArrayList<REFCN>();

        getrList().add(new REFCN(".*\\d+/loadbalancers(\\.json|\\.xml)?/?$", LoadBalancer.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+(\\.json|\\.xml)?/?$", LoadBalancer.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/accesslist(\\.json|\\.xml)?/?$", AccessList.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/accesslist/\\d+(\\.json|\\.xml)?/?$", NetworkItem.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/healthmonitor(\\.json|\\.xml)?/?$", HealthMonitor.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/nodes(\\.json|\\.xml)?/?$", Nodes.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/nodes/\\d+(\\.json|\\.xml)?/?$", Node.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/metadata(\\.json|\\.xml)?/?$", Metadata.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/metadata/\\d+(\\.json|\\.xml)?/?$", Meta.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/sessionpersistence(\\.json|\\.xml)?/?$", SessionPersistence.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/connectionthrottle(\\.json|\\.xml)?/?$", ConnectionThrottle.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/ssltermination(\\.json|\\.xml)?/?$", SslTermination.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/\\d+/errorpage(\\.json|\\.xml)?/?$", Errorpage.class));

        getrList().add(new REFCN(".*\\d+/loadbalancers/bounce/connectionthrottle(\\.json|\\.xml)?/?$", ConnectionThrottle.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/bounce/virtualip(\\.json|\\.xml)?/?$", VirtualIp.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/bounce/loadbalancer(\\.json|\\.xml)?/?$", LoadBalancer.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/bounce/updated(\\.json|\\.xml)?/?$", Updated.class));
        getrList().add(new REFCN(".*\\d+/loadbalancers/bounce/ssltermination(\\.json|\\.xml)?/?$", SslTermination.class));
        getrList().add(new REFCN(".*/management/hosts(\\.json|\\.xml)?/?$", Host.class));
    }

    public static Class getClassForUri(String uri) {
        Class classForUri = null;

        if (uri == null) {
            return classForUri;
        }
        for (REFCN refcn : getrList()) {
            Pattern m = refcn.getPattern();
            if (m.matcher(uri).find()) {
                classForUri = refcn.getSomeClass();
                break;
            }
        }
        return classForUri; // If we don't know how to validate it return null.
    }

    public static List<REFCN> getrList() {
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
        if(someClass == null) {
            return null;
        }
        xmlRootElementAnnotation = (XmlRootElement) someClass.getAnnotation(XmlRootElement.class);
        if (xmlRootElementAnnotation != null) {
            rootElement = xmlRootElementAnnotation.name();
        }
        return rootElement;
    }

    public static class REFCN {

        private String rootElement;
        private Pattern pattern;
        private Class someClass;

        public REFCN() {
        }

        public REFCN(String regEx, Class someClass) {
            this.pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
            this.someClass = someClass;
            this.rootElement = getRootElementNameForClass(someClass);
        }

        public Pattern getPattern() {
            return pattern;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public Class getSomeClass() {
            return this.someClass;
        }

        public void setSomeClass(Class someClass) {
            this.someClass = someClass;
        }
    }
}
