package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Medication;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.hapi.fhir.FhirUtil;
import ch.elexis.core.test.TestEntities;
import ch.elexis.core.types.ArticleTyp;
import es.fhir.rest.core.coding.MedicamentCoding;
import info.elexis.server.fhir.rest.core.test.AllTests;

public class MedicationTest {
	
	private static IGenericClient client;
	
	@BeforeClass
	public static void setupClass() throws IOException, SQLException{
		AllTests.getTestDatabaseInitializer().initializePrescription();
		AllTests.getTestDatabaseInitializer().initializeArtikelstammTable();
		
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}
	
	@Test
	public void loadArtikelstammMedicationById(){
		Medication artikelstamm = client.read().resource(Medication.class)
			.withId(ArticleTyp.ARTIKELSTAMM.getCodeSystemName() + "."
				+ TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID)
			.execute();
		assertNotNull(artikelstamm);
		System.out.println(FhirUtil.serializeToString(artikelstamm));
		Map<String, Coding> codeMap = parseCodeMap(artikelstamm);
		assertEquals("Dafalgan Filmtabl 1 g 100 Stk",
			codeMap.get(MedicamentCoding.NAME.getUrl()).getCode());
		assertEquals(ArticleTyp.ARTIKELSTAMM.getCodeSystemName(),
			codeMap.get(MedicamentCoding.TYPE.getUrl()).getCode());
		assertEquals("7680563180086", codeMap.get(MedicamentCoding.GTIN.getOid()).getCode());
		assertEquals("N02BE01", codeMap.get(MedicamentCoding.ATC.getOid()).getCode());
	}
	
	@Test
	public void loadArticleMedicationById(){
		Medication customArticle =
			client.read().resource(Medication.class).withId(ArticleTyp.EIGENARTIKEL + "."
				+ AllTests.getTestDatabaseInitializer().getArticle().getId()).execute();
		assertNotNull(customArticle);
		System.out.println(FhirUtil.serializeToString(customArticle));
		Map<String, Coding> codeMap = parseCodeMap(customArticle);
		assertEquals("test article", codeMap.get(MedicamentCoding.NAME.getUrl()).getCode());
		assertEquals("0000001111111", codeMap.get(MedicamentCoding.GTIN.getOid()).getCode());
	}
	
	@Test
	public void searchMedicationByGtin(){
		Bundle results = client
			.search().forResource(Medication.class).where(Medication.CODE.exactly()
				.systemAndCode(MedicamentCoding.GTIN.getUrl(), "0000001111111"))
			.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertEquals(1, entries.size());
		Map<String, Coding> codeMap = parseCodeMap((Medication) entries.get(0).getResource());
		assertEquals("test article", codeMap.get(MedicamentCoding.NAME.getUrl()).getCode());
		assertEquals("0000001111111", codeMap.get(MedicamentCoding.GTIN.getOid()).getCode());
		
		results = client
			.search().forResource(Medication.class).where(Medication.CODE.exactly()
				.systemAndCode(MedicamentCoding.GTIN.getUrl(), "7680563180086"))
			.returnBundle(Bundle.class).execute();
		entries = results.getEntry();
		assertEquals(1, entries.size());
		codeMap = parseCodeMap((Medication) entries.get(0).getResource());
		assertEquals("Dafalgan Filmtabl 1 g 100 Stk",
			codeMap.get(MedicamentCoding.NAME.getUrl()).getCode());
	}
	
	@Test
	public void searchMedicationByNameWildcard(){
		Bundle results = client
			.search().forResource(Medication.class).where(Medication.CODE.exactly()
				.systemAndCode(MedicamentCoding.NAME.getUrl(), "Dafalgan Odis 1000 "))
			.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertEquals(4, entries.size());
	}
	
	private Map<String, Coding> parseCodeMap(Medication medication){
		return medication.getCode().getCoding().stream()
			.collect(Collectors.toMap(Coding::getSystem, Function.identity()));
	}
	
}
