package info.elexis.server.core.connector.elexis.jpa.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.VersionedResource;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;
import info.elexis.server.core.connector.elexis.services.ArtikelstammItemService;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.LabItemService;
import info.elexis.server.core.connector.elexis.services.LabResultService;
import info.elexis.server.core.connector.elexis.services.PersistenceService;
import info.elexis.server.core.connector.elexis.services.PrescriptionService;
import info.elexis.server.core.connector.elexis.services.UserService;
import info.elexis.server.core.connector.elexis.services.XidService;

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

	private static boolean isBehandlungInitialized = false;
	private static Behandlung behandlung;

	private static boolean isLabResultInitialized = false;
	private static List<LabResult> labResults = new ArrayList<>();
	private static LabItem labItem;

	private static boolean isPrescriptionInitialized = false;
	private static Prescription prescription;

	private static boolean isArtikelstammInitialized = false;
	private static ArtikelstammItem artikelstammitem;

	private static boolean isLaborItemsOrdersResultsInitialized = false;

	private static boolean isLaborTarif2009Initialized = false;
	private static boolean isTarmedInitialized = false;
	private static boolean isPhysioLeistungInitialized = false;

	private static boolean isAgendaInitialized = false;
	private static boolean isRemindersInitialized = false;
	private static boolean isLeistungsblockInitialized = false;
	
	public void initializeDb(DBConnection connection, boolean sqlOnly) throws IOException, SQLException {
		Connection jdbcConnection = getJdbcConnection(connection).get();
		try {
			executeDbScript(jdbcConnection, "/rsc/createDB.script");
			executeDbScript(jdbcConnection, "/rsc/dbScripts/User.sql");
			executeDbScript(jdbcConnection, "/rsc/dbScripts/Role.sql");
			executeDbScript(jdbcConnection, "/rsc/dbScripts/ArtikelstammItem.sql");
			executeDbScript(jdbcConnection, "/rsc/dbScripts/sampleContacts.sql");
			executeDbScript(jdbcConnection, "/rsc/dbScripts/BillingVKPreise.sql");
			if(!sqlOnly) {
				ConfigInitializer.initializeConfiguration();
			}
			isDbInitialized = true;
		} finally {
			try {
				jdbcConnection.close();
			} catch (SQLException e) {
				// ignore
			}
		}
	}

	public synchronized void initializeDb() throws IOException, SQLException {
		if (!isDbInitialized) {
			initializeDb(TestDatabase.getDBConnection(), false);
		}
	}

	public synchronized void initializeAgendaTable() throws IOException, SQLException {
		initializeDb();
		if (!isAgendaInitialized) {
			isAgendaInitialized = initializeDbScript("/rsc/dbScripts/Agenda.sql");
		}
	}

	public synchronized void initializeLaborTarif2009Tables() throws IOException, SQLException {
		initializeDb();
		if (!isLaborTarif2009Initialized) {
			isLaborTarif2009Initialized = initializeDbScript("/rsc/dbScripts/LaborTarif2009.sql");
		}
	}

	public synchronized void initializeTarmedTables() throws IOException, SQLException {
		initializeDb();
		if (!isTarmedInitialized) {
			isTarmedInitialized = initializeDbScript("/rsc/dbScripts/Tarmed.sql");
			isTarmedInitialized = initializeDbScript("/rsc/dbScripts/TarmedKumulation.sql");
			isTarmedInitialized = initializeDbScript("/rsc/dbScripts/TarmedExtension.sql");
			isTarmedInitialized = initializeDbScript("/rsc/dbScripts/TarmedGroup.sql");
		}
	}

	public synchronized void initializeArzttarifePhysioLeistungTables() throws IOException, SQLException {
		initializeDb();
		if (!isPhysioLeistungInitialized) {
			isPhysioLeistungInitialized = initializeDbScript("/rsc/dbScripts/ArzttarifePhysio.sql");
		}
	}

	public synchronized void initializeLeistungsblockTables() throws IOException, SQLException {
		initializeDb();
		if (!isLeistungsblockInitialized) {
			isLeistungsblockInitialized = initializeDbScript("/rsc/dbScripts/Leistungsblock.sql");
		}
	}
	
	/**
	 * Initializes an intrinsic consistent set of LabItems, LabResults and
	 * LabOrders
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public synchronized void initializeLaborItemsOrdersResults() throws IOException, SQLException {
		initializeDb();
		if (!isLaborItemsOrdersResultsInitialized) {
			isLaborItemsOrdersResultsInitialized = initializeDbScript("/rsc/dbScripts/LaborItemsWerteResults.sql");
		}
	}
	
	public synchronized void initializeReminders() throws IOException, SQLException {
		initializeDb();
		if(!isRemindersInitialized) {
			isRemindersInitialized = initializeDbScript("/rsc/dbScripts/Reminder.sql");
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
		} else {
			fail("No connection present");
		}
		return false;
	}

	private void executeDbScript(Connection jdbcConnection, String path) throws IOException, SQLException {
		try (InputStream is = TestDatabaseInitializer.class.getResourceAsStream(path)) {
			ScriptRunner runner = new ScriptRunner(jdbcConnection, true, true);
			runner.runScript(new InputStreamReader(is));
		}
	}

	/**
	 * 
	 * @param connection
	 *            may be <code>null</code> in which case refers to
	 *            {@link TestDatabase#getDBConnection()}
	 * @return
	 */
	public Optional<Connection> getJdbcConnection(DBConnection connection) {
		if (connection == null) {
			connection = TestDatabase.getDBConnection();
		}

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
	 * <li>Diagnosen: Test Diagnose 1\nTest Diagnose 2</li>
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public synchronized void initializePatient() throws IOException, SQLException {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isPatientInitialized) {
			patient = new KontaktService.PersonBuilder("Test", "Patient", LocalDate.of(1990, 1, 1), Gender.FEMALE)
					.patient().build();
			patient.setPhone1("+01555123");
			patient.setMobile("+01444123");
			patient.setCity("City");
			patient.setZip("123");
			patient.setStreet("Street 1");
			patient.setDiagnosen("Test Diagnose 1\nTest Diagnose 2");
			PersistenceService.save(patient);

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

		XidService.setDomainId(kontakt, XidConstants.DOMAIN_AHV, ahvBuilder.toString(), XidQuality.ASSIGNMENT_REGIONAL);
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
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public synchronized void initializeOrganization() throws IOException, SQLException {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isOrganizationInitialized) {
			organization = new KontaktService.OrganizationBuilder("Test Organization").build();
			organization.setPhone1("+01555345");
			organization.setMobile("+01444345");

			organization.setCity("City");
			organization.setZip("123");
			organization.setStreet("Street 10");

			PersistenceService.save(organization);
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
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public synchronized void initializeMandant() throws IOException, SQLException {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isMandantInitialized) {
			mandant = new KontaktService.PersonBuilder("Test", "Mandant", LocalDate.of(1970, 1, 1), Gender.MALE)
					.mandator().build();
			mandant.setPhone1("+01555234");
			mandant.setMobile("+01444234");

			mandant.setCity("City");
			mandant.setZip("123");
			mandant.setStreet("Street 100");
			mandant.setUser(true);
			PersistenceService.save(mandant);

			User user = new UserService.Builder("tst", mandant).buildAndSave();
			Optional<Role> doctorRole = getRoleWithId("doctor");
			if (doctorRole.isPresent()) {
				user.setRoles(Collections.singletonList(doctorRole.get()));
			}
			UserService.save(user);

			XidService.setDomainId(mandant, XidConstants.DOMAIN_EAN, "2000000000002", XidQuality.ASSIGNMENT_GLOBAL);

			XidService.setDomainId(mandant, "www.xid.ch/id/ksk", "C000002", XidQuality.ASSIGNMENT_REGIONAL);
			isMandantInitialized = true;
		}
	}

	public Optional<Role> getRoleWithId(String id) {
		JPAQuery<Role> query = new JPAQuery<>(Role.class);
		List<Role> roles = query.execute();
		for (Role role : roles) {
			if (id.equals(role.getId())) {
				return Optional.of(role);
			}
		}
		return Optional.empty();
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
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public synchronized void initializePrescription() throws IOException, SQLException {
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
			prescription = new PrescriptionService.Builder(artikelstammitem, patient, "1-1-1-1").buildAndSave();

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
	 * <li>KostentrKontakt: {@link TestDatabaseInitializer#getOrganization()}
	 * </li>
	 * <li>VersNummer: 1234-5678</li>
	 * <li>DatumVon: 1.9.2016</li>
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public synchronized void initializeFall() throws IOException, SQLException {
		if (!isPatientInitialized) {
			initializePatient();
		}
		if (!isOrganizationInitialized) {
			initializeOrganization();
		}
		if (!isFallInitialized) {
			fall = new FallService.Builder(patient, "Test Fall", "reason", "method").build();
			fall.setKostentrKontakt(organization);
			fall.setVersNummer("1234-5678");
			fall.setDatumVon(LocalDate.of(2016, Month.SEPTEMBER, 1));
			FallService.save(fall);
			patient = KontaktService.reload(patient);
			isFallInitialized = true;
		}
	}

	public static Fall getFall() {
		return fall;
	}

	/**
	 * Initialize a test Behandlung.
	 * 
	 * <li>Patient: {@link TestDatabaseInitializer#getPatient()}</li>
	 * <li>ServiceProvider: {@link TestDatabaseInitializer#getMandant()}</li>
	 * <li>Datum: 21.9.2016</li>
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void initializeBehandlung() throws IOException, SQLException {
		if (!isMandantInitialized) {
			initializeMandant();
		}
		if (!isFallInitialized) {
			initializeFall();
		}
		if (!isBehandlungInitialized) {
			behandlung = new BehandlungService.Builder(getFall(), getMandant()).buildAndSave();
			behandlung.setDatum(LocalDate.of(2016, Month.SEPTEMBER, 21));
			VersionedResource vr = VersionedResource.load(null);
			vr.update("Test consultation\nWith some test text.", "test remark");
			behandlung.setEintrag(vr);
			BehandlungService.save(behandlung);
			isBehandlungInitialized = true;
		}
	}

	public static Behandlung getBehandlung() {
		return behandlung;
	}

	/**
	 * Initialize a test LabResults.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public synchronized void initializeLabResult() throws IOException, SQLException {
		if (!isPatientInitialized) {
			initializePatient();
		}
		if (!isLabResultInitialized) {
			Kontakt laboratory = new KontaktService.OrganizationBuilder("Labor Test").laboratory().build();
			laboratory.setDescription2("Test");
			PersistenceService.save(laboratory);

			labItem = (LabItem) new LabItemService.Builder("TEST NUMERIC", "Test Laboratory", laboratory, ">1", "3-3.5",
					"unit",
					LabItemTyp.NUMERIC, "group", 1).build();
			labItem.setExport("vitolabkey:1,2");
			LabItemService.save(labItem);

			LabItem textLabItem = (LabItem) new LabItemService.Builder("TEST TEXT", "Test Laboratory", laboratory, null,
					null, "unit", LabItemTyp.TEXT, "group", 2).build();
			LabItemService.save(labItem);

			LabResult labResult = new LabResultService.Builder(labItem, patient).build();

			labResult.setObservationtime(LocalDateTime.of(2016, Month.DECEMBER, 14, 17, 44, 25));
			labResult.setUnit("u");
			labResult.setRefMale("<1");
			labResult.setRefFemale("1-1.5");
			labResult.setOriginId(laboratory.getId());
			labResult.setResult("2");
			labResult.setComment("no comment");
			LabResultService.save(labResult);
			labResults.add(labResult);

			labResult = new LabResultService.Builder(labItem, patient).build();
			labResult.setObservationtime(LocalDateTime.of(2016, Month.DECEMBER, 15, 10, 10, 30));
			labResult.setOriginId(laboratory.getId());
			labResult.setResult("2");
			labResult.setComment("no comment");
			LabResultService.save(labResult);
			labResults.add(labResult);

			labResult = new LabResultService.Builder(labItem, patient).build();
			labResult.setObservationtime(LocalDateTime.of(2017, Month.FEBRUARY, 28, 12, 59, 23));
			labResult.setOriginId(laboratory.getId());
			labResult.setResult("124/79");
			labResult.setUnit("Bloodpressure");
			LabResultService.save(labResult);
			labResults.add(labResult);

			labResult = new LabResultService.Builder(textLabItem, patient).build();
			labResult.setObservationtime(LocalDateTime.of(2017, Month.FEBRUARY, 28, 10, 02, 23));
			labResult.setOriginId(laboratory.getId());
			labResult.setResult("(Text)");
			labResult.setComment("The Text Result ...");
			LabResultService.save(labResult);
			labResults.add(labResult);

			isLabResultInitialized = true;
		}
	}

	public static List<LabResult> getLabResults() {
		return labResults;
	}

	/**
	 * Initialize an test ArtikelstammItem.
	 * 
	 * <li>GTIN: 7680336700282</li>
	 * <li>Pharm: 58985</li>
	 * <li>Desc: ASPIRIN C Brausetabl 10 Stk</li>
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public synchronized void initializeArtikelstamm() throws IOException, SQLException {
		if (!isDbInitialized) {
			initializeDb();
		}
		if (!isArtikelstammInitialized) {
			artikelstammitem = new ArtikelstammItemService.Builder(0, "7680336700282", BigInteger.valueOf(58985l),
					"ASPIRIN C Brausetabl 10 Stk").buildAndSave();

			isArtikelstammInitialized = true;
		}
	}

}
