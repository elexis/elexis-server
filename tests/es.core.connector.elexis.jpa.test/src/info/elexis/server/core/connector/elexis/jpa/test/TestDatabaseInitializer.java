package info.elexis.server.core.connector.elexis.jpa.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.common.DBConnection;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;
import info.elexis.server.core.connector.elexis.services.ArtikelstammItemService;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.PrescriptionService;

public class TestDatabaseInitializer {

	private static Logger logger = LoggerFactory.getLogger(TestDatabaseInitializer.class);

	private static boolean isDbInitialized = false;

	private static boolean isPatientInitialized = false;
	private static Kontakt patient;

	private static boolean isOrganizationInitialized = false;
	private static Kontakt organization;

	private static boolean isMandantInitialized = false;
	private static Kontakt mandant;

	private static boolean isFallInitialized = false;
	private static Fall fall;

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
	 * <li>Lastname: Patient</li>
	 * <li>DateofBirth: 1.1.1990</li>
	 * <li>Gender: FEMALE</li>
	 * <li>Phone1: +01555123</li>
	 * <li>Mobile: +01444123</li>
	 * <li>City: City</li>
	 * <li>Zip: 123</li>
	 * <li>Street: Street 1</li>
	 * <li>Xid AHV: 756...</li>
	 * 
	 */
	public synchronized void initializePatient() {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isPatientInitialized) {
			patient = KontaktService.INSTANCE.createPatient("Test", "Patient", LocalDate.of(1990, 1, 1), Gender.FEMALE);
			patient.setPhone1("+01555123");
			patient.setMobile("+01444123");

			patient.setCity("City");
			patient.setZip("123");
			patient.setStreet("Street 1");

			KontaktService.INSTANCE.flush();
			addAHVNumber(patient, 1);
			isPatientInitialized = true;
		}
	}

	/**
	 * Get the initialized Patient
	 * 
	 * @return
	 */
	public static Kontakt getPatient() {
		return patient;
	}

	private void addAHVNumber(Kontakt kontakt, int index) {
		String country = "756";
		String number = String.format("%09d", index);
		StringBuilder ahvBuilder = new StringBuilder(country + number);
		ahvBuilder.append(getAHVCheckNumber(ahvBuilder.toString()));

		KontaktService.INSTANCE.setDomainId(kontakt, XidConstants.DOMAIN_AHV, ahvBuilder.toString(),
				XidQuality.ASSIGNMENT_REGIONAL);
	}

	private String getAHVCheckNumber(String string) {
		int sum = 0;
		for (int i = 0; i < string.length(); i++) {
			// reverse order
			char character = string.charAt((string.length() - 1) - i);
			int intValue = Character.getNumericValue(character);
			if (i % 2 == 0) {
				sum += intValue * 3;
			} else {
				sum += intValue;
			}
		}
		return Integer.toString(sum % 10);
	}

	/**
	 * Initialize a test Organization.
	 * 
	 * <li>Description1: Test Organization</li>
	 * <li>Lastname: Test</li>
	 * <li>Phone1: +01555345</li>
	 * <li>Mobile: +01444345</li>
	 * <li>City: City</li>
	 * <li>Zip: 123</li>
	 * <li>Street: Street 10</li>
	 * 
	 */
	public synchronized void initializeOrganization() {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isOrganizationInitialized) {
			organization = KontaktService.INSTANCE.create();
			organization.setOrganisation(true);
			organization.setDescription1("Test Organization");
			organization.setPhone1("+01555345");
			organization.setMobile("+01444345");

			organization.setCity("City");
			organization.setZip("123");
			organization.setStreet("Street 10");

			KontaktService.INSTANCE.flush();
			isOrganizationInitialized = true;
		}
	}

	public static Kontakt getOrganization() {
		return organization;
	}

	/**
	 * Initialize a test Mandant.
	 * 
	 * <li>Firstname: Test</li>
	 * <li>Lastname: Mandant</li>
	 * <li>DateofBirth: 1.1.1970</li>
	 * <li>Gender: MALE</li>
	 * <li>Phone1: +01555234</li>
	 * <li>Mobile: +01444234</li>
	 * <li>City: City</li>
	 * <li>Zip: 123</li>
	 * <li>Street: Street 100</li>
	 * <li>EAN: 2000000000002</li>
	 * <li>KSK: C000002</li>
	 * 
	 */
	public synchronized void initializeMandant() {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isMandantInitialized) {
			mandant = KontaktService.INSTANCE.create();
			mandant.setPerson(true);
			mandant.setMandator(true);
			mandant.setDescription1("Mandant");
			mandant.setDescription2("Test");

			mandant.setGender(Gender.MALE);
			mandant.setDateOfBirth(new TimeTool("01.01.1970"));

			mandant.setPhone1("+01555234");
			mandant.setMobile("+01444234");

			mandant.setCity("City");
			mandant.setZip("123");
			mandant.setStreet("Street 100");
			KontaktService.INSTANCE.flush();

			KontaktService.INSTANCE.setDomainId(mandant, XidConstants.DOMAIN_EAN, "2000000000002",
					XidQuality.ASSIGNMENT_GLOBAL);

			KontaktService.INSTANCE.setDomainId(mandant, "www.xid.ch/id/ksk", "C000002",
					XidQuality.ASSIGNMENT_REGIONAL);
			isMandantInitialized = true;
		}
	}

	public static Kontakt getMandant() {
		return mandant;
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
	 * Initialize a test Fall.
	 * 
	 * <li>Patient: {@link TestDatabaseInitializer#getPatient()}</li>
	 * <li>Label: "Test Fall"</li>
	 * <li>Reason: "reason"</li>
	 * <li>BillingMethod: "method"</li>
	 * <li>KostentrKontakt:
	 * {@link TestDatabaseInitializer#getOrganization()}</li>
	 * <li>VersNummer: 1234-5678</li>
	 * <li>DatumVon: 1.9.2016</li>
	 */
	public synchronized void initializeFall() {
		if (!isPatientInitialized) {
			initializePatient();
		}
		if (!isOrganizationInitialized) {
			initializeOrganization();
		}
		if (!isFallInitialized) {
			fall = FallService.INSTANCE.create(patient, "Test Fall", "reason", "method");
			fall.setKostentrKontakt(organization);
			fall.setVersNummer("1234-5678");
			fall.setDatumVon(LocalDate.of(2016, Month.SEPTEMBER, 1));
			FallService.INSTANCE.flush();

			KontaktService.INSTANCE.refresh(patient);
			isFallInitialized = true;
		}
	}

	public static Fall getFall() {
		return fall;
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
