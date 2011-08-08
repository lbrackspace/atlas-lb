package org.openstack.atlas.adapter.Null;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		ReverseProxyLoadBalancerAdapter adapter = new NullAdapterImpl();

		// Register Null Adapter
		bundleContext.registerService(
				ReverseProxyLoadBalancerAdapter.class.getName(), adapter, null);

		Activator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
