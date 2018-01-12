package info.elexis.server.setup.test.internal;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import info.elexis.server.core.common.util.CoreUtil;

public class Activator implements BundleActivator {

	private File esConnectionFile = CoreUtil.getHomeDirectory().resolve("elexis-connection.xml").toFile();
	private File esTestTemp = CoreUtil.getHomeDirectory().resolve("elexis-connection.xml.testBackup").toFile();

	private static BundleContext bundleContext;

	@Override
	public void start(BundleContext context) throws Exception {
		Activator.bundleContext = context;

		if (esConnectionFile.exists()) {
			esConnectionFile.renameTo(esTestTemp);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Activator.bundleContext = null;

		if (esTestTemp.exists()) {
			esTestTemp.renameTo(esConnectionFile);
		}
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}

}
