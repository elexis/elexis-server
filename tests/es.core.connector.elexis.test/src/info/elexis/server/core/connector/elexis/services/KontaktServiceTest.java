package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.CivilState;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.PatientConstants;
import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.ContactType;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.RelationshipType;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.KontaktAdressJoint;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Sticker;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ZusatzAdresse;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.MimeType;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;
import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;

public class KontaktServiceTest extends AbstractServiceTest {

	@Test
	public void testCreateAndRemoveKontakt() throws InstantiationException, IllegalAccessException {
		LocalDate dob = LocalDate.of(2016, 12, 12);
		Kontakt val = new KontaktService.PersonBuilder("", "", dob, Gender.MALE).buildAndSave();
		assertNotNull(val.getId());
		assertNotNull(val.getLastupdate());
		Kontakt findById = KontaktService.load(val.getId()).get();
		assertEquals(val.getId(), findById.getId());
		assertEquals(dob, val.getDateOfBirth().toLocalDate());
		KontaktService.remove(val);
		Optional<Kontakt> found = KontaktService.load(val.getId());
		assertFalse(found.isPresent());
	}

	@Test
	public void testFindAllPatients() {
		List<Kontakt> findAllPatients = KontaktService.findAllPatients();
		assertTrue(findAllPatients.size() > 0);
	}

	@Test
	@Ignore
	public void testCreateAndDeleteKontaktWithStringExceedsLimit()
			throws InstantiationException, IllegalAccessException {
		Kontakt val = new KontaktService.PersonBuilder("", "", LocalDate.of(2016, 12, 12), Gender.MALE).buildAndSave();

		val.setDescription1(
				"ThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255Characters");

		KontaktService.save(val);
	}

	@Test
	public void testCreatePatient() {
		Kontakt patient = new KontaktService.PersonBuilder("Vorname", "Nachname", LocalDate.now(), Gender.FEMALE)
				.patient().buildAndSave();
		patient.setExtInfoValue(PatientConstants.FLD_EXTINFO_BIRTHNAME, "Birthname");
		KontaktService.save(patient);
		String id = patient.getId();

		assertNotNull(id);
		assertNotNull(patient.getCode());
		Kontakt findById = KontaktService.load(id).get();
		assertNotNull(findById);
		assertEquals(findById, patient);
		assertEquals("Birthname", findById.getExtInfoAsString(PatientConstants.FLD_EXTINFO_BIRTHNAME));

		KontaktService.remove(patient);
	}

	@Test
	public void testGetAgeInYears() {
		Optional<Kontakt> male = KontaktService.load(TestEntities.PATIENT_MALE_ID);
		int ageInYears = KontaktService.getAgeInYears(male.get());
		assertTrue(ageInYears >= 37);
	}

	@Test
	public void testFindPatientByPatientNumber() {
		Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		assertTrue(patient.isPresent());
		Optional<Kontakt> findById = KontaktService.load(TestEntities.PATIENT_MALE_ID);
		assertTrue(findById.isPresent());
		assertEquals(findById.get().getId(), patient.get().getId());

		assertEquals("Testpatient", patient.get().getDescription1());
		assertEquals("Vorname", patient.get().getDescription2());
		assertEquals(new TimeTool(LocalDate.of(1979, 3, 15)), patient.get().getDateOfBirth());
		assertEquals(Gender.MALE, patient.get().getGender());
		assertEquals(ContactType.PATIENT, patient.get().getContactType());
		assertEquals("Teststrasse 15", patient.get().getStreet());
		assertEquals("Testort", patient.get().getCity());
		assertEquals("6840", patient.get().getZip());
	}

	@Test
	public void testLoadMandatorAndExtInfoValues() {
		Optional<Kontakt> findById = KontaktService.load(TestEntities.MANDATOR_ID);
		assertTrue(findById.isPresent());
		assertEquals("Nachname Hauptanwender", findById.get().getExtInfoAsString("Mandant"));
		assertEquals(ContactType.PERSON, findById.get().getContactType());
	}

	@Test
	public void testFindLaboratory() {
		Optional<IContact> lab = KontaktService.findLaboratory(TestEntities.LABORATORY_IDENTIFIER);
		assertTrue(lab.isPresent());
		Optional<Kontakt> findById = KontaktService.load(TestEntities.LABORATORY_ID);
		assertTrue(findById.isPresent());
		assertEquals(findById.get().getId(), lab.get().getId());
		assertEquals(ContactType.LABORATORY, lab.get().getContactType());
	}

	@Test
	public void testSetImageForContact() throws IOException {
		byte[] image = IOUtils.toByteArray(this.getClass().getResourceAsStream("testPatientImage.jpg"));
		Optional<Kontakt> findById = KontaktService.load(TestEntities.PATIENT_MALE_ID);
		assertTrue(findById.isPresent());
		KontaktService.setContactImage(findById.get(), image, MimeType.jpg);
		assertArrayEquals(image, KontaktService.getContactImage(findById.get()).get().getImage());
	}

	@Test
	public void testContactListElementsBehaviour() {
		// we load the contact
		Kontakt findById = KontaktService.load(TestEntities.PATIENT_MALE_ID).get();
		assertEquals(0, findById.getFaelle().size());
		// meanwhile a Fall is added
		new FallService.Builder(findById, "label", "reason", "billingMethod").buildAndSave();
		// we add a Fall via the contact
		Fall fall = new Fall();
		fall.setPatientKontakt(findById);
		fall.setBezeichnung("label2");
		findById.getFaelle().add(fall);
		KontaktService.save(findById);
		// we reload the contact, and find now 2 faelle
		findById = KontaktService.load(TestEntities.PATIENT_MALE_ID).get();
		assertEquals(2, findById.getFaelle().size());
	}

	/**
	 * Extended attribute setting test including all attributes defined in
	 * RocketHealth patient model
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateExtendedPatientContact() throws IOException {
		String[] s = new String[20];
		for (int i = 0; i < s.length; i++) {
			s[i] = RandomStringUtils.random(20, true, true);
		}

		// patient
		LocalDate dob = LocalDate.now();
		LocalDate dod = LocalDate.now().plusDays(15);
		Kontakt p = new KontaktService.PersonBuilder(s[0], s[1], dob, Gender.MALE).patient().build();
		p.setTitel(s[2]);
		p.setExtInfoValue(PatientConstants.FLD_EXTINFO_BIRTHNAME, s[3]);
		p.setExtInfoValue(PatientConstants.FLD_EXTINFO_DATE_OF_DEATH, dod.toString());
		p.setExtInfoValue(PatientConstants.FLD_EXTINFO_CIVIL_STATE, CivilState.MARRIED.name());
		p.setExtInfoValue(PatientConstants.FLD_EXTINFO_PROFESSION, s[4]);
		XidService.setDomainId(p, XidConstants.DOMAIN_AHV, s[5], XidQuality.ASSIGNMENT_REGIONAL);
		// selfPayPatient?
		p.setPhone1(s[6]);
		p.setPhone2(s[7]);
		p.setMobile(s[8]);
		p.setEmail(s[9]);
		Sticker testSticker = new StickerService.StickerBuilder("testSticker", "", "", ElexisTypeMap.TYPE_PATIENT)
				.buildAndSave();
		boolean result = StickerService.applyStickerToObject(testSticker, p);
		assertTrue(result);

		byte[] image = IOUtils.toByteArray(this.getClass().getResourceAsStream("testPatientImage.jpg"));
		KontaktService.setContactImage(p, image, MimeType.jpg);
		// run twice to test alternative execution (if image already exists)
		KontaktService.setContactImage(p, image, MimeType.jpg);

		// addresses
		// PRINCIPAL_RESIDENCE (PRIMARY)
		p.setStreet(s[10]);
		p.setExtInfoValue("Street2", s[11]);
		p.setZip(s[12].substring(0, 6));
		p.setCity(s[13]);
		p.setCountry(Country.CH);

		ZusatzAdresse nursingHome = new ZusatzAdresse();
		nursingHome.setContact(p);
		nursingHome.setAddressType(AddressType.NURSING_HOME);
		nursingHome.setStreet2(s[16]);
		nursingHome.setZip(s[17].substring(0, 6));
		nursingHome.setCountry(Country.AT);
		p.getAddresses().put(nursingHome.getId(), nursingHome);

		Kontakt familyDoctor = new KontaktService.PersonBuilder("family", "doctor", dob, Gender.FEMALE).buildAndSave();
		p.setExtInfoValue(PatientConstants.FLD_EXTINFO_STAMMARZT, familyDoctor.getId());

		Kontakt laboratory = KontaktService.load(TestEntities.LABORATORY_ID).get();
		KontaktAdressJoint relatedBusinessEmployeeContact = KontaktService.setRelatedContact(p, laboratory,
				RelationshipType.BUSINESS_EMPLOYER_VALUE, RelationshipType.BUSINESS_EMPLOYEE_VALUE, "employer");

		KontaktService.save(p);

		/// ----- verify values
		Kontakt pl = KontaktService.load(p.getId()).get();
		assertEquals(pl.getId(), p.getId());

		assertEquals(s[2], pl.getTitel());
		assertEquals(s[3], pl.getExtInfoAsString(PatientConstants.FLD_EXTINFO_BIRTHNAME));
		assertEquals(dod.toString(), pl.getExtInfoAsString(PatientConstants.FLD_EXTINFO_DATE_OF_DEATH));
		assertEquals(CivilState.MARRIED.name(), pl.getExtInfoAsString(PatientConstants.FLD_EXTINFO_CIVIL_STATE));
		assertEquals(s[4], pl.getExtInfoAsString(PatientConstants.FLD_EXTINFO_PROFESSION));
		assertEquals(s[5], pl.getXids().get(XidConstants.DOMAIN_AHV).getDomainId());
		assertEquals(s[6], pl.getPhone1());
		assertEquals(s[7], pl.getPhone2());
		assertEquals(s[8], pl.getMobile());
		assertEquals(s[9], pl.getEmail());
		assertEquals("testSticker", StickerService.findStickersOnObject(pl).get(0).getName());
		assertArrayEquals(image, KontaktService.getContactImage(pl).get().getImage());

		assertEquals(s[10], pl.getStreet());
		assertEquals(s[11], pl.getExtInfoAsString("Street2"));
		assertEquals(s[12].substring(0, 6), pl.getZip());
		assertEquals(s[13], pl.getCity());
		assertEquals(Country.CH, pl.getCountry());

		assertEquals(1, pl.getAddresses().size());
		ZusatzAdresse nhv = pl.getAddresses().entrySet().iterator().next().getValue();
		assertEquals(nhv.getId(), pl.getAddresses().get(nursingHome.getId()).getId());
		assertNull(pl.getAddresses().get("invalidKeyObjectDoesNotExist"));
		assertEquals(pl, nhv.getContact());
		assertEquals(AddressType.NURSING_HOME, nhv.getAddressType());
		assertEquals(s[16], nhv.getStreet2());
		assertEquals(s[17].substring(0, 6), nhv.getZip());
		assertEquals(Country.AT, nhv.getCountry());

		assertEquals(laboratory, pl.getRelatedContacts().get(relatedBusinessEmployeeContact.getId()).getOtherKontakt());
		
		assertEquals(familyDoctor.getId(), p.getExtInfoAsString(PatientConstants.FLD_EXTINFO_STAMMARZT));
		
		laboratory = KontaktService.reload(laboratory);
		assertEquals(1, laboratory.getRelatedByContacts().size());
		assertEquals(p.getId(), laboratory.getRelatedByContacts().iterator().next().getMyKontakt().getId());

		StickerService.remove(testSticker);
	}
}
