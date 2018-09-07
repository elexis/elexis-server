package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.CodeSystem.ConceptDefinitionComponent;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.findings.codes.CodingSystem;
import info.elexis.server.hapi.fhir.FhirUtil;

public class CodesySystemTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() {
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void getCodeSystem() {
		// tessinercode
		CodeSystem readCodeSystem = client.read().resource(CodeSystem.class).withId("tessinercode").execute();
		assertNotNull(readCodeSystem);

		// search by system
		Bundle results = client.search().forResource(CodeSystem.class)
				.where(CodeSystem.SYSTEM.matches().value(CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE.getSystem()))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		assertEquals(readCodeSystem.getUrl(), ((CodeSystem) entries.get(0).getResource()).getUrl());

		// coveragetype
		readCodeSystem = client.read().resource(CodeSystem.class).withId("coveragetype").execute();
		assertNotNull(readCodeSystem);

		// search by system
		results = client.search().forResource(CodeSystem.class)
				.where(CodeSystem.SYSTEM.matches().value(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem()))
				.returnBundle(Bundle.class)
				.execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());
		assertEquals(readCodeSystem.getUrl(), ((CodeSystem) entries.get(0).getResource()).getUrl());
	}

	@Test
	public void getCodeSystemProperties() {
		// tessinercode
		CodeSystem readCodeSystem = client.read().resource(CodeSystem.class).withId("tessinercode").execute();
		assertNotNull(readCodeSystem);

		assertEquals(CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE.getSystem(), readCodeSystem.getUrl());
		Optional<ConceptDefinitionComponent> concept = getConceptFromSytem("A1", readCodeSystem);
		assertTrue(concept.isPresent());

		// coveragetype
		readCodeSystem = client.read().resource(CodeSystem.class).withId("coveragetype").execute();
		assertNotNull(readCodeSystem);
		assertEquals(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem(), readCodeSystem.getUrl());
		concept = getConceptFromSytem("KVG", readCodeSystem);
		assertTrue(concept.isPresent());

	}

	private Optional<ConceptDefinitionComponent> getConceptFromSytem(String string, CodeSystem codeSystem) {
		List<ConceptDefinitionComponent> concepts = codeSystem.getConcept();
		List<ConceptDefinitionComponent> matching = concepts.stream()
				.filter(concept -> isMatchingConcept(concept, string)).collect(Collectors.toList());
		if(matching.size() == 1){
			return Optional.of(matching.get(0));
		}
		return Optional.empty();
	}

	private boolean isMatchingConcept(ConceptDefinitionComponent concept, String string) {
		return concept.getCode().equals(string);
	}
}
