package info.elexis.server.core.connector.elexis.jpa.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.common.DBConnection;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.services.ArtikelstammItemService;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.PrescriptionService;

public class TestDatabaseInitializer {

	private static Logger logger = LoggerFactory.getLogger(TestDatabaseInitializer.class);

	private static boolean isDbInitialized = false;

	private static boolean isPatientInitialized = false;
	private static Kontakt patient;

	private static boolean isPrescriptionInitialized = false;
	private static Prescription prescription;

	private static boolean isArtikelstammInitialized = false;
	private static ArtikelstammItem artikelstammitem;

	private static boolean isLaborTarif2009Initialized = false;
	private static boolean isTarmedInitialized = false;
	private static boolean isPhysioLeistungInitialized = false;

	public synchronized void initializeDb() {
		if (!isDbInitialized) {
			// initialize
			Optional<Connection> connection = getJdbcConnection(TestDatabase.getDBConnection());
			if (connection.isPresent()) {
				Connection jdbcConnection = connection.get();
				try {
					executeDbScript(jdbcConnection, "/rsc/createDB.script");
					executeDbScript(jdbcConnection, "/rsc/dbScripts/User.sql");
					executeDbScript(jdbcConnection, "/rsc/dbScripts/Role.sql");
					executeDbScript(jdbcConnection, "/rsc/dbScripts/ArtikelstammItem.sql");
					executeDbScript(jdbcConnection, "/rsc/dbScripts/sampleContacts.sql");
					executeDbScript(jdbcConnection, "/rsc/dbScripts/BillingVKPreise.sql");
					isDbInitialized = true;
				} catch (IOException | SQLException e) {
					logger.error("Faild to run sql script on test database.", e);
					return;
				} finally {
					try {
						jdbcConnection.close();
					} catch (SQLException e) {
						// ignore
					}
				}
			}
		}
	}

	public synchronized void initializeLaborTarif2009Tables() {
		initializeDb();
		if (!isLaborTarif2009Initialized) {
			isLaborTarif2009Initialized = initializeDbScript("/rsc/dbScripts/LaborTarif2009.sql");
		}
	}

	public synchronized void initializeTarmedTables() {
		initializeDb();
		if (!isTarmedInitialized) {
			isTarmedInitialized = initializeDbScript("/rsc/dbScripts/Tarmed.sql");
			isTarmedInitialized = initializeDbScript("/rsc/dbScripts/TarmedKumulation.sql");
		}
	}
	public synchronized void initializeArzttarifePhysioLeistungTables() {
		initializeDb();
		if (!isPhysioLeistungInitialized) {
			isPhysioLeistungInitialized = initializeDbScript("/rsc/dbScripts/ArzttarifePhysio.sql");
		}
	}

	private boolean initializeDbScript(String dbScript) {
		// initialize
		Optional<Connection> connection = getJdbcConnection(TestDatabase.getDBConnection());
		if (connection.isPresent()) {
			Connection jdbcConnection = connection.get();
			try {
				executeDbScript(jdbcConnection, dbScript);
				return true;
			} catch (IOException | SQLException e) {
				logger.error("Failed to run sql script on test database.", e);
				org.junit.Assert.fail("Failed to run sql script on test database:" + e.getMessage());
			} finally {
				try {
					jdbcConnection.close();
				} catch (SQLException e) {
					// ignore
				}
			}
		}
		return false;
	}

	private void executeDbScript(Connection jdbcConnection, String path) throws IOException, SQLException {
		try (InputStream is = TestDatabaseInitializer.class.getResourceAsStream(path)) {
			ScriptRunner runner = new ScriptRunner(jdbcConnection, true, true);
			runner.runScript(new InputStreamReader(is));
		}
	}

	private Optional<Connection> getJdbcConnection(DBConnection connection) {
		try {
			Driver driver = (Driver) Class.forName(connection.rdbmsType.driverName).newInstance();

			Properties properties = new Properties();
			properties.put("user", connection.username);
			properties.put("password", connection.password);

			Connection jdbcConnection = driver.connect(connection.connectionString, properties);

			return Optional.of(jdbcConnection);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			logger.error("Faild to create connection to test database.", e);
			return Optional.empty();
		}
	}

	/**
	 * Initialize a test Patient.
	 * 
	 * <li>Firstname: Test</li>
	 * <li>Lastname: Test</li>
	 * <li>DateofBirth: 1.1.1990</li>
	 * <li>Gender: FEMALE</li>
	 * 
	 */
	public synchronized void initializePatient() {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isPatientInitialized) {
			patient = KontaktService.INSTANCE.createPatient("Test", "Test", LocalDate.of(1990, 1, 1), Gender.FEMALE);

			isPatientInitialized = true;
		}
	}

	/**
	 * Initialize an test Prescription.
	 * 
	 * <li>Article: see {@link TestDatabaseInitializer#initializeArtikelstamm()}
	 * </li>
	 * <li>Patient: see {@link TestDatabaseInitializer#initializePatient()}</li>
	 * <li>Dosage: 1-1-1-1</li>
	 * 
	 */
	public synchronized void initializePrescription() {
		if (!isDbInitialized) {
			initializeDb();
		}
		if (!isPatientInitialized) {
			initializePatient();
		}
		if (!isArtikelstammInitialized) {
			initializeArtikelstamm();
		}
		if (!isPrescriptionInitialized) {
			prescription = PrescriptionService.INSTANCE.create(artikelstammitem, patient, "1-1-1-1");

			isPrescriptionInitialized = true;
		}
	}

	/**
	 * Initialize an test ArtikelstammItem.
	 * 
	 * <li>GTIN: 7680336700282</li>
	 * <li>Pharm: 58985</li>
	 * <li>Desc: ASPIRIN C Brausetabl 10 Stk</li>
	 * 
	 */
	public synchronized void initializeArtikelstamm() {
		if (!isDbInitialized) {
			initializeDb();
		}
		if (!isArtikelstammInitialized) {
			artikelstammitem = ArtikelstammItemService.INSTANCE.create(0, "7680336700282", BigInteger.valueOf(58985l),
					"ASPIRIN C Brausetabl 10 Stk");

			isArtikelstammInitialized = true;
		}
	}
}
