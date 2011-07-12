package org.openstack.atlas.osgi.cfg;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.util.Properties;

public class ConfigurationServiceFactory implements ServiceFactory {

    private static final Log LOG = LogFactory.getLog(ConfigurationServiceFactory.class);
    private static final String DEFAULT_CONFIGURATION_LOCATION = "/etc/openstack/atlas/container.conf";

    @Override
    public Object getService(Bundle bundle, ServiceRegistration sr) {
        final Properties properties = new Properties();
        String fileLocation = DEFAULT_CONFIGURATION_LOCATION;

        try {
            properties.load(bundle.getResource("META-INF/rc-conf.properties").openStream());
            fileLocation = properties.getProperty("rc.config.location");
        } catch (Exception ioe) {
            LOG.warn(ioe);
        }

        return new ApacheCommonsConfiguration(fileLocation);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration sr, Object o) {
        //Ignored. No shared state here, sukkas.
    }
}
