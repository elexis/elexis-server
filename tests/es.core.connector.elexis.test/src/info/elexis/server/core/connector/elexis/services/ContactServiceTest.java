package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.test.TestEntities;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;


public class ContactServiceTest extends AbstractServiceTest {
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
	}
		
	@Test
	public void testFindAllPatients(){
		List<IPatient> findAllPatients = ContactService.findAllPatients();
		assertTrue(findAllPatients.size() > 0);
	}
	
	//	@Test
	//	@Ignore
	//	public void testCreateAndDeleteIContactWithStringExceedsLimit()
	//			throws InstantiationException, IllegalAccessException {
	//		IPerson val = new IContactBuilder.PersonBuilder(modelService, "", "", LocalDate.of(2016, 12, 12), Gender.MALE).buildAndSave();
	//		val.setDescription1(
	//				"ThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255Characters");
	//		modelService.save(val);
	//	}
	
	//	@Test // MERGED INTO ANOTHER TEST
	//	public void testGetAgeInYears() {
	//		Optional<IContact> male = ContactService.load(TestEntities.PATIENT_MALE_ID);
	//		int ageInYears = ContactService.getAgeInYears(male.get());
	//		assertTrue(ageInYears >= 37);
	//	}
	
	@Test
	public void testFindPatientByPatientNumber(){
		Optional<IPatient> patient =
			ContactService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		assertTrue(patient.isPresent());
		Optional<IPatient> findById =
			modelService.load(TestEntities.PATIENT_MALE_ID, IPatient.class);
		assertTrue(findById.isPresent());
		assertEquals(findById.get().getId(), patient.get().getId());
		
		assertEquals("Testpatient", patient.get().getDescription1());
		assertEquals("Vorname", patient.get().getDescription2());
		assertEquals(LocalDate.of(1979, 3, 15).atStartOfDay(), patient.get().getDateOfBirth());
		assertEquals(Gender.MALE, patient.get().getGender());
		assertEquals("Teststrasse 15", patient.get().getStreet());
		assertEquals("Testort", patient.get().getCity());
		assertEquals("6840", patient.get().getZip());
	}
	
//	@Test
//	public void testLoadMandatorAndExtInfoValues(){
//		Optional<IMandator> findById = modelService.load(TestEntities.MANDATOR_ID, IMandator.class);
//		assertTrue(findById.isPresent());
//		assertEquals("Nachname Hauptanwender", findById.get().getExtInfo("Mandant"));
//	}
	
//	@Test
//	public void testFindLaboratory(){
//		Optional<ILaboratory> lab =
//			ContactService.findLaboratory(TestEntities.LABORATORY_IDENTIFIER);
//		assertTrue(lab.isPresent());
//		Optional<ILaboratory> findById =
//			modelService.load(TestEntities.LABORATORY_ID, ILaboratory.class);
//		assertTrue(findById.isPresent());
//		assertTrue(findById.get().isLaboratory());
//		assertEquals(findById.get().getId(), lab.get().getId());
//	}
	
//	@Test
//	public void testSetImageForContact() throws IOException{
//		byte[] image =
//			IOUtils.toByteArray(this.getClass().getResourceAsStream("testPatientImage.jpg"));
//		Optional<IPatient> findById =
//			modelService.load(TestEntities.PATIENT_MALE_ID, IPatient.class);
//		assertTrue(findById.isPresent());
//		ContactService.setContactImage(findById.get(), image, MimeType.jpg);
//		assertArrayEquals(image, ContactService.getContactImage(findById.get()).get().getImage());
//	}
//	
//	/**
//	 * Test IContact#addresses configuration. INSERT is always carried out as DB operation, but a
//	 * subsequent entity UPDATE only if at lease CascadeType.MERGE is specified.
//	 */
//	@Test
//	public void testZusatzaddressInsertAndUpdate(){
//		IPatient patient =
//			ContactService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR).get();
//		
//		// INSERT does happen without specifying a CascadeType
//		ZusatzAdresse nursingHome = new ZusatzAdresse();
//		nursingHome.setContact(patient);
//		nursingHome.setAddressType(AddressType.NURSING_HOME);
//		nursingHome.setStreet2("Street2");
//		nursingHome.setZip("6840");
//		nursingHome.setCountry(Country.AT);
//		patient.getAddresses().put(nursingHome.getId(), nursingHome);
//		modelService.save(patient);
//		
//		// UPDATE does only happen if CascadeType.MERGE is specified
//		IContact pla = ContactService.load(patient.getId()).get();
//		ZusatzAdresse local = pla.getAddresses().get(nursingHome.getId());
//		local.setCountry(Country.CH);
//		pla.getAddress().put(nursingHome.getId(), local);
//		modelService.save(pla);
//		
//		IContact pl = ContactService.load(patient.getId()).get();
//		assertEquals(1, pl.getAddresses().size());
//		ZusatzAdresse nhv = pl.getAddresses().entrySet().iterator().next().getValue();
//		assertEquals(nhv.getId(), pl.getAddresses().get(nursingHome.getId()).getId());
//		assertNull(pl.getAddresses().get("invalidKeyObjectDoesNotExist"));
//		assertEquals(pl, nhv.getContact());
//		assertEquals(AddressType.NURSING_HOME, nhv.getAddressType());
//		assertEquals("Street2", nhv.getStreet2());
//		assertEquals("6840", nhv.getZip());
//		assertEquals(Country.CH, nhv.getCountry());
//		
//		ZusatzAdresse zusatzAdresse = pl.getAddresses().get(nursingHome.getId());
//		PersistenceService.remove(zusatzAdresse);
//	}
//	
//	@Test
//	public void testZusatzadressenMapModification(){
//		IContact patient =
//			ContactService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR).get();
//		
//		Map<String, ZusatzAdresse> stateA = new HashMap<>();
//		Map<String, ZusatzAdresse> stateB = new HashMap<>();
//		
//		ZusatzAdresse nursingHome = new ZusatzAdresse();
//		nursingHome.setContact(patient);
//		nursingHome.setAddressType(AddressType.NURSING_HOME);
//		nursingHome.setStreet2("Street2");
//		nursingHome.setZip("6840");
//		nursingHome.setCountry(Country.AT);
//		stateA.put(nursingHome.getId(), nursingHome);
//		stateB.put(nursingHome.getId(), nursingHome);
//		
//		ZusatzAdresse secondaryHome = new ZusatzAdresse();
//		secondaryHome.setContact(patient);
//		secondaryHome.setAddressType(AddressType.SECONDARY_RESIDENCE);
//		secondaryHome.setZip("6841");
//		stateB.put(secondaryHome.getId(), secondaryHome);
//		
//		patient.setAddresses(stateA);
//		modelService.save(patient);
//		assertThat(ContactService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR)
//			.get().getAddresses(), CoreMatchers.is(stateA));
//		
//		patient.setAddresses(stateB);
//		modelService.save(patient);
//		assertThat(ContactService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR)
//			.get().getAddresses(), CoreMatchers.is(stateB));
//		
//		patient.setAddresses(stateA);
//		modelService.save(patient);
//		assertThat(ContactService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR)
//			.get().getAddresses(), CoreMatchers.is(stateA));
//	}
	
//	@Test
//	public void testContactListElementsBehaviour(){
//		// we load the contact
//		IPatient findById = modelService.load(TestEntities.PATIENT_MALE_ID, IPatient.class).get();
//		assertEquals(0, findById.getFaelle().size());
//		// meanwhile a Fall is added
//		new ICoverageBuilder.Builder(modelService, findById, "label", "reason", "billingMethod")
//			.buildAndSave();
//		// we add a Fall via the contact
//		Fall fall = new Fall();
//		fall.setPatientIContact(findById);
//		fall.setBezeichnung("label2");
//		findById.getFaelle().add(fall);
//		modelService.save(findById);
//		// we reload the contact, and find now 2 faelle
//		findById = ContactService.load(TestEntities.PATIENT_MALE_ID).get();
//		assertEquals(2, findById.getFaelle().size());
//	}
	
//	/**
//	 * Extended attribute setting test including all attributes defined in RocketHealth patient
//	 * model
//	 * 
//	 * @throws IOException
//	 */
//	@Test
//	public void testCreateExtendedPatientContact() throws IOException{
//		String[] s = new String[20];
//		for (int i = 0; i < s.length; i++) {
//			s[i] = RandomStringUtils.random(20, true, true);
//		}
//		
//		// patient
//		LocalDate dob = LocalDate.now();
//		LocalDate dod = LocalDate.now().plusDays(15);
//		IPatient p =
//			(IPatient) new IContactBuilder.PatientBuilder(modelService, s[0], s[1], dob, Gender.MALE)
//				.build();
//		p.setTitel(s[2]);
//		p.setExtInfo(PatientConstants.FLD_EXTINFO_BIRTHNAME, s[3]);
//		p.setExtInfo(PatientConstants.FLD_EXTINFO_DATE_OF_DEATH, dod.toString());
//		p.setExtInfo(PatientConstants.FLD_EXTINFO_CIVIL_STATE, CivilState.MARRIED.name());
//		p.setExtInfo(PatientConstants.FLD_EXTINFO_PROFESSION, s[4]);
//		p.addXid(XidConstants.DOMAIN_AHV, s[5], true);
//		// selfPayPatient?
//		p.setPhone1(s[6]);
//		p.setPhone2(s[7]);
//		p.setMobile(s[8]);
//		p.setEmail(s[9]);
//		Sticker testSticker =
//			new StickerService.StickerBuilder("testSticker", "", "", ElexisTypeMap.TYPE_PATIENT)
//				.buildAndSave();
//		boolean result = StickerService.applyStickerToObject(testSticker, p);
//		assertTrue(result);
//		
//		byte[] image =
//			IOUtils.toByteArray(this.getClass().getResourceAsStream("testPatientImage.jpg"));
//		ContactService.setContactImage(p, image, MimeType.jpg);
//		// run twice to test alternative execution (if image already exists)
//		ContactService.setContactImage(p, image, MimeType.jpg);
//		
//		// addresses
//		// PRINCIPAL_RESIDENCE (PRIMARY)
//		p.setStreet(s[10]);
//		p.setExtInfo("Street2", s[11]);
//		p.setZip(s[12].substring(0, 6));
//		p.setCity(s[13]);
//		p.setCountry(Country.CH);
//		
//		IContact familyDoctor =
//			new IContactBuilder.PersonBuilder(modelService, "family", "doctor", dob, Gender.FEMALE)
//				.buildAndSave();
//		p.setExtInfo(PatientConstants.FLD_EXTINFO_STAMMARZT, familyDoctor.getId());
//		
//		IContact laboratory = ContactService.load(TestEntities.LABORATORY_ID).get();
//		IContactAdressJoint relatedBusinessEmployeeContact = ContactService.setRelatedContact(p,
//			laboratory, RelationshipType.BUSINESS_EMPLOYER_VALUE,
//			RelationshipType.BUSINESS_EMPLOYEE_VALUE, "employer");
//		
//		modelService.save(p);
//		
//		/// ----- verify values
//		IContact pl = ContactService.load(p.getId()).get();
//		assertEquals(pl.getId(), p.getId());
//		
//		assertEquals(s[2], pl.getTitel());
//		assertEquals(s[3], pl.getExtInfo(PatientConstants.FLD_EXTINFO_BIRTHNAME));
//		assertEquals(dod.toString(), pl.getExtInfo(PatientConstants.FLD_EXTINFO_DATE_OF_DEATH));
//		assertEquals(CivilState.MARRIED.name(),
//			pl.getExtInfo(PatientConstants.FLD_EXTINFO_CIVIL_STATE));
//		assertEquals(s[4], pl.getExtInfo(PatientConstants.FLD_EXTINFO_PROFESSION));
//		assertEquals(s[5], pl.getXids().get(XidConstants.DOMAIN_AHV).getDomainId());
//		assertEquals(s[6], pl.getPhone1());
//		assertEquals(s[7], pl.getPhone2());
//		assertEquals(s[8], pl.getMobile());
//		assertEquals(s[9], pl.getEmail());
//		assertEquals("testSticker", StickerService.findStickersOnObject(pl).get(0).getName());
//		assertArrayEquals(image, ContactService.getContactImage(pl).get().getImage());
//		
//		assertEquals(s[10], pl.getStreet());
//		assertEquals(s[11], pl.getExtInfo("Street2"));
//		assertEquals(s[12].substring(0, 6), pl.getZip());
//		assertEquals(s[13], pl.getCity());
//		assertEquals(Country.CH, pl.getCountry());
//		
//		assertEquals(laboratory,
//			pl.getRelatedContacts().get(relatedBusinessEmployeeContact.getId()).getOtherIContact());
//		
//		assertEquals(familyDoctor.getId(), p.getExtInfo(PatientConstants.FLD_EXTINFO_STAMMARZT));
//		
//		laboratory = ContactService.reload(laboratory);
//		assertEquals(1, laboratory.getRelatedByContacts().size());
//		assertEquals(p.getId(),
//			laboratory.getRelatedByContacts().iterator().next().getMyIContact().getId());
//		
//		StickerService.remove(testSticker);
//	}
}
