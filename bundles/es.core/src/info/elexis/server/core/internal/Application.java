package info.elexis.server.core.internal;

import java.io.File;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.util.CoreUtil;
import info.elexis.server.core.contrib.ApplicationShutdownRegistrar;
import info.elexis.server.core.contrib.IApplicationShutdownListener;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	private static Logger log = LoggerFactory.getLogger(Application.class);

	private boolean restart;
	private boolean shutdown;
	private boolean force;

	private static Application instance;
	private static final Date startTime = new Date();

	private File singleInstanceLockFile;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		log.info("Starting " + Application.class.getName() + "...");
		instance = this;

		TimeZone tzone = TimeZone.getTimeZone("CET");
		TimeZone.setDefault(tzone);

		singleInstanceLockFile = new File(CoreUtil.getHomeDirectory().toString(), "elexis-server.lock");
		if (!singleInstanceLockFile.createNewFile()) {
			log.error("Found existing lock-file {}, shutting down.", singleInstanceLockFile.toString());
			return IApplication.EXIT_OK;
		}
		singleInstanceLockFile.deleteOnExit();

		context.applicationRunning();
		while (!restart && !shutdown) {
			Thread.sleep(2000);
			if (restart || shutdown) {
				checkVeto();
			}
		}

		if (restart) {
			log.info("Restarting " + Application.class.getName() + "...");
			// give all services time to shutdown
			Thread.sleep(2000);
			return IApplication.EXIT_RESTART;
		}

		log.info("Stopping " + Application.class.getName() + "...");
		return IApplication.EXIT_OK;
	}

	private void checkVeto() {
		Set<IApplicationShutdownListener> shutdownListeners = ApplicationShutdownRegistrar
				.getApplicationShutdownListeners();
		for (IApplicationShutdownListener ias : shutdownListeners) {
			if (force) {
				ias.performShutdown(true);
			} else {
				String reason = ias.performShutdown(false);
				if (reason != null) {
					shutdown = false;
					restart = false;
					log.info("[{}] shutdown/restart veto:  {}", ias.getClass().getName(), reason);
					break;
				}
			}
		}
	}

	@Override
	public void stop() {
	}

	public static Application getInstance() {
		return instance;
	}

	public void restart(boolean force) {
		restart = true;
		this.force = force;
	}

	public void shutdown(boolean force) {
		shutdown = true;
		this.force = force;
	}

	public static Date getStarttime() {
		return startTime;
	}

	public static String getStatus() {
		long millis = new Date().getTime() - Application.getStarttime().getTime();

		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		return "Uptime: " + String.format("%d days, %d hours, %d min, %d sec", days, hours, minutes, seconds);
	}
}
