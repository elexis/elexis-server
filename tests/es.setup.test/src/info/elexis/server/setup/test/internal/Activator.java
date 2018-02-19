package info.elexis.server.setup.test.internal;

import java.io.File;

import org.h2.tools.DeleteDbFiles;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import info.elexis.server.core.common.util.CoreUtil;

public class Activator implements BundleActivator {

	private File esConnectionFile = CoreUtil.getHomeDirectory().resolve("elexis-connection.xml").toFile();
	private File esConnectionFileTestBackup = CoreUtil.getHomeDirectory().resolve("elexis-connection.xml.testBackup")
			.toFile();

	private static BundleContext bundleContext;

	@Override
	public void start(BundleContext context) throws Exception {
		Activator.bundleContext = context;

		if (esConnectionFileTestBackup.exists()) {
			esConnectionFileTestBackup.delete();
		}

		if (esConnectionFile.exists()) {
			boolean renameTo = esConnectionFile.renameTo(esConnectionFileTestBackup);
			if (!renameTo) {
				throw new IllegalStateException("Could not rename elexis-connection.xml");
			}
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Activator.bundleContext = null;

		if (esConnectionFileTestBackup.exists()) {
			esConnectionFileTestBackup.renameTo(esConnectionFile);
		}
		
		DeleteDbFiles.execute("~/elexis-server", "elexisTest", true);
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}

}
