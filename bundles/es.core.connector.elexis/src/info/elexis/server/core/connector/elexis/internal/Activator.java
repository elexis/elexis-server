package info.elexis.server.core.connector.elexis.internal;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import info.elexis.server.core.connector.elexis.services.ConfigService;
import info.elexis.server.core.contrib.ApplicationShutdownRegistrar;
import info.elexis.server.core.contrib.IApplicationShutdownListener;

public class Activator implements BundleActivator {

	private Logger log = LoggerFactory.getLogger(Activator.class);

	private static BundleContext context;
	private static IApplicationShutdownListener iasl = new ApplicationShutdownListener();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		ApplicationShutdownRegistrar.addShutdownListener(iasl);
		
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				Locale locale = Locale.getDefault();
				String dbStoredLocale = ConfigService.INSTANCE.get(Preferences.CFG_LOCALE, null);
				if (dbStoredLocale == null || !locale.toString().equals(dbStoredLocale)) {
					log.error("System locale [{}] does not match required database locale [{}].", locale.toString(),
							dbStoredLocale);
					System.out.println("System locale does not match required database locale!");
				}
				// TOOD verify db version?
			}
		}, 1500);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		ApplicationShutdownRegistrar.removeShutdownListener(iasl);

		Activator.context = null;
	}

}
