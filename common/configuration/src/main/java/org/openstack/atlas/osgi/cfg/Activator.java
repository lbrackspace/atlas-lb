package org.openstack.atlas.osgi.cfg;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(
                new String[]{"org.openstack.atlas.cfg.Configuration"},
                new ConfigurationServiceFactory(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        //Nothing to see here...
    }
}
