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

		article = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID).get();
		product = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_PRODUCT_PHARMA_ID).get();
		assertNotNull(article);
		assertNotNull(product);
	}

	@Test
	public void testFindAllNonDeletedPrescriptionsForPatient() {
		Prescription articlePres = new PrescriptionService.Builder(article, testPatients.get(0), "1-1-0-0").buildAndSave();
		Prescription productPres = new PrescriptionService.Builder(product, testPatients.get(0), "1-1-0-0").buildAndSave();
		Prescription deletedPres = new PrescriptionService.Builder(article, testPatients.get(0), "1-1-2-1").buildAndSave();
		PrescriptionService.delete(deletedPres);
		Prescription recipePres = new PrescriptionService.Builder(article, testPatients.get(0), "1-1-2-1").buildAndSave();
		recipePres.setRezeptID("nonExistRecipeId");
		recipePres.setPrescriptionType("2");
		PrescriptionService.save(recipePres);

		Prescription selfDispensePres = new PrescriptionService.Builder(article, testPatients.get(0), "1-1-2-3").buildAndSave();
		selfDispensePres.setPrescriptionType("3");
		PrescriptionService.save(selfDispensePres);

		assertNotNull(articlePres.getDateFrom());
		assertEquals("1-1-0-0", articlePres.getDosis());
		assertNotNull(productPres.getDateFrom());

		List<Prescription> prescList = PrescriptionService
				.findAllNonDeletedPrescriptionsForPatient(testPatients.get(0));
		assertEquals(2, prescList.size());

		assertEquals(EntryType.FIXED_MEDICATION, articlePres.getEntryType());
		assertEquals(EntryType.SELF_DISPENSED, selfDispensePres.getEntryType());
		assertEquals(EntryType.RECIPE, recipePres.getEntryType());

		PrescriptionService.remove(articlePres);
		PrescriptionService.remove(productPres);
		PrescriptionService.remove(deletedPres);
		PrescriptionService.remove(recipePres);
	}

}
