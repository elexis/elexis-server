package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.prescription.EntryType;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;

public class PrescriptionServiceTest extends AbstractServiceTest {

	private ArtikelstammItem article;
	private ArtikelstammItem product;

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();

		article = ArtikelstammItemService.INSTANCE.findById(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID).get();
		product = ArtikelstammItemService.INSTANCE.findById(TestEntities.ARTIKELSTAMM_PRODUCT_PHARMA_ID).get();
		assertNotNull(article);
		assertNotNull(product);
	}

	@Test
	public void testFindAllNonDeletedPrescriptionsForPatient() {
		Prescription articlePres = PrescriptionService.INSTANCE.create(article, testPatients.get(0), "1-1-0-0");
		Prescription productPres = PrescriptionService.INSTANCE.create(product, testPatients.get(0), "1-1-0-0");
		Prescription deletedPres = PrescriptionService.INSTANCE.create(article, testPatients.get(0), "1-1-2-1");
		deletedPres.setDeleted(true);
		Prescription recipePres = PrescriptionService.INSTANCE.create(article, testPatients.get(0), "1-1-2-1");
		recipePres.setRezeptID("nonExistRecipeId");
		recipePres.setPrescriptionType("2");
		PrescriptionService.INSTANCE.flush();
		
		Prescription selfDispensePres = PrescriptionService.INSTANCE.create(article, testPatients.get(0), "1-1-2-3");
		selfDispensePres.setPrescriptionType("3");
		PrescriptionService.INSTANCE.flush();

		assertNotNull(articlePres.getDateFrom());
		assertEquals("1-1-0-0", articlePres.getDosis());
		assertNotNull(productPres.getDateFrom());

		List<Prescription> prescList = PrescriptionService
				.findAllNonDeletedPrescriptionsForPatient(testPatients.get(0));
		assertEquals(2, prescList.size());
		
		assertEquals(EntryType.FIXED_MEDICATION, articlePres.getEntryType());
		assertEquals(EntryType.SELF_DISPENSED, selfDispensePres.getEntryType());
		assertEquals(EntryType.RECIPE, recipePres.getEntryType());

		PrescriptionService.INSTANCE.remove(articlePres);
		PrescriptionService.INSTANCE.remove(productPres);
		PrescriptionService.INSTANCE.remove(deletedPres);
		PrescriptionService.INSTANCE.remove(recipePres);
	}
	
}
