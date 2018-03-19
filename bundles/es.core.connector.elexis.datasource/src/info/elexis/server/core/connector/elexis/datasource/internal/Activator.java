package info.elexis.server.core.connector.elexis.datasource.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.status.StatusUtil;

public class Activator implements BundleActivator {

	public static final String BUNDLE_ID = "info.elexis.server.core.connector.elexis.datasource";

	private static Logger log = LoggerFactory.getLogger(Activator.class);

	private static BundleContext context;
	private static ServiceRegistration<DataSource> servReg;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		refreshDataSource();
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

	/**
	 * Register the DataSource as OSGI Service
	 */
	public static IStatus refreshDataSource() {
		Dictionary<String, String> properties = new Hashtable<>();
		properties.put("osgi.jndi.service.name", "jdbc/poolable");

		if (servReg != null) {
			servReg.unregister();
		}

		ElexisPoolingDataSource elexisPoolingDataSource = new ElexisPoolingDataSource();
		IStatus activate = elexisPoolingDataSource.activate();
		if (activate.isOK()) {
			servReg = context.registerService(DataSource.class, elexisPoolingDataSource, properties);
		} else {
			StatusUtil.logStatus(log, activate, true);
		}
		return activate;
	}

}
