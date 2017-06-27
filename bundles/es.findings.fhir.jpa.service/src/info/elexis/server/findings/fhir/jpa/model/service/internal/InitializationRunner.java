package info.elexis.server.findings.fhir.jpa.model.service.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;

import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.findings.util.FindingsFormatUtil;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.services.ConfigService;
import info.elexis.server.findings.fhir.jpa.service.FindingsService;

public class InitializationRunner {

	private List<InitializationRunnable> runnalbes = new ArrayList<>();

	private volatile boolean initialized = false;
	private volatile boolean initializing = false;

	private ExecutorService executor;

	private final FindingsService findingsService;
	
	public InitializationRunner(FindingsService findingsService){
		this.findingsService = findingsService;
		addRunnables();

		executor = Executors.newSingleThreadExecutor();
	}

	private void addRunnables() {
		// database initialization
		runnalbes.add(new InitializationRunnable() {
			@Override
			public void run() {
				try {
					LoggerFactory.getLogger(getClass()).info("Starting findings database initialization");
					Optional<DBConnection> connectionOpt = ElexisDBConnection.getConnection();
					if (connectionOpt.isPresent()) {
						DbInitializer initializer = new DbInitializer(connectionOpt.get());
						initializer.init();
					}
					LoggerFactory.getLogger(getClass()).info("Finished findings database initialization");
				} catch (Exception e) {
					LoggerFactory.getLogger(FindingsService.class).debug("Error initializing database " + this, e);
				}
			}

			@Override
			public boolean isBlocking() {
				return true;
			}

			@Override
			public void cancel() {
				// ignore this should never block for a long time
			}

			@Override
			public boolean isCancelled() {
				// ignore this should never block for a long time
				return false;
			}
		});
		// FHIR format upgrade
		runnalbes.add(new InitializationRunnable() {



			private boolean stop = false;

			@Override
			public void run() {
				try {
					String currentVersion = ConfigService.INSTANCE.get(FindingsFormatUtil.CFG_HAPI_FHIR_VERSION, "");
					if (currentVersion.isEmpty()
							|| !FindingsFormatUtil.HAPI_FHIR_CURRENT_VERSION.equals(currentVersion)) {
						LoggerFactory.getLogger(getClass()).info("Running HAPI FHIR format update from ["
								+ currentVersion + "] to [" + FindingsFormatUtil.HAPI_FHIR_CURRENT_VERSION + "]");
						EntityManager available = null;
						while (available == null) {
							try {
								available = FindingsEntityManager.getEntityManager();
								if (stop) {
									return;
								}
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// ignore
							}
						}

						fhirFormatUpdate();
						if (!stop) {
							ConfigService.INSTANCE.set(FindingsFormatUtil.CFG_HAPI_FHIR_VERSION,
									FindingsFormatUtil.HAPI_FHIR_CURRENT_VERSION);
							LoggerFactory.getLogger(getClass()).info("Finished HAPI FHIR format update from ["
									+ currentVersion + "] to [" + FindingsFormatUtil.HAPI_FHIR_CURRENT_VERSION + "]");
						} else {
							LoggerFactory.getLogger(getClass()).info("Canceled HAPI FHIR format update from ["
									+ currentVersion + "] to [" + FindingsFormatUtil.HAPI_FHIR_CURRENT_VERSION + "]");
						}
					}
				} catch (Exception e) {
					LoggerFactory.getLogger(getClass()).error("Exception during update", e);
				}
			}

			@Override
			public boolean isBlocking() {
				return false;
			}

			private void fhirFormatUpdate() {
				if (FindingsFormatUtil.HAPI_FHIR_CURRENT_VERSION.equals("24")) {
					InitializationUpdate24 update =
						new InitializationUpdate24(this, findingsService);
					update.update();
				}
			}

			@Override
			public void cancel() {
				stop = true;
			}

			@Override
			public boolean isCancelled() {
				return stop;
			}
		});
	}

	public boolean shouldRun() {
		Optional<DBConnection> connection = ElexisDBConnection.getConnection();
		return connection.isPresent() && !initialized && !initializing;
	}

	public synchronized void run() {
		if (!initialized && !initializing) {
			initializing = true;

			for (InitializationRunnable initializationRunnable : runnalbes) {
				if (initializationRunnable.isBlocking()) {
					Future<?> future = executor.submit(initializationRunnable);
					try {
						future.get();
					} catch (InterruptedException | ExecutionException e) {
						LoggerFactory.getLogger(getClass()).error("Error executing initialization", e);
					}
				} else {
					executor.execute(initializationRunnable);
				}
			}
			initialized = true;
			initializing = false;
		}
	}

	public void cancel() {
		for (InitializationRunnable initializationRunnable : runnalbes) {
			initializationRunnable.cancel();
		}
		executor.shutdown();
	}
}
