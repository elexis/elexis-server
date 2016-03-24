package info.elexis.server.core.internal;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	private static Logger logger = LoggerFactory.getLogger(Application.class);

	private boolean restart;
	private boolean shutdown;

	private static Application instance;
	private static final Date startTime = new Date();

	@Override
	public Object start(IApplicationContext context) throws Exception {
		logger.info("Starting " + Application.class.getName() + "...");
		instance = this;

		TimeZone tzone = TimeZone.getTimeZone("CET");
		TimeZone.setDefault(tzone);

		context.applicationRunning();
		while (!restart && !shutdown) {
			Thread.sleep(2000);
		}

		if (restart) {
			logger.info("Restarting " + Application.class.getName() + "...");
			// give all services time to shutdown
			Thread.sleep(2000);
			return IApplication.EXIT_RESTART;
		}

		logger.info("Stopping " + Application.class.getName() + "...");
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

	public static Application getInstance() {
		return instance;
	}

	public void restart() {
		restart = true;
	}

	public void shutdown() {
		shutdown = true;
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


		return "Uptime: " + String.format("%d days, %d hours, %d min, %d sec", days,
				hours, minutes, seconds);
	}
}
