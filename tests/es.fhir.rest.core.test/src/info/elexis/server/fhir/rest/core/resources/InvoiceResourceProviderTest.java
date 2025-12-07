package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Invoice.InvoiceStatus;
import org.hl7.fhir.r4.model.Money;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IFreeTextDiagnosis;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IInvoiceService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Result;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class InvoiceResourceProviderTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeBehandlung();
		AllTests.getTestDatabaseInitializer().initializePrescription();
		IContextService contextService = OsgiServiceUtil.getService(IContextService.class).orElseThrow();
		contextService.setActiveMandator(AllTests.getTestDatabaseInitializer().getMandant());
		contextService.setActiveUser(AllTests.getTestDatabaseInitializer().getUser());

		ConfigServiceHolder.setUser(Preferences.LEISTUNGSCODES_BILLING_STRICT, false);

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getInvoiceById() {
		IBillingService billingService = OsgiServiceUtil.getService(IBillingService.class).orElseThrow();
		IInvoiceService invoiceService = OsgiServiceUtil.getService(IInvoiceService.class).orElseThrow();

		// create testdata
		Object[] testSet = AllTests.getTestDatabaseInitializer().createTestMandantPatientFallBehandlung();
		IArticle article = AllTests.getTestDatabaseInitializer().getArticle();
		Result<IBilled> result = billingService.bill(article, (IEncounter) testSet[3], 5);
		assertTrue(result.getMessages().toString(), result.isOK());

		IFreeTextDiagnosis diagnosis = AllTests.getModelService().create(IFreeTextDiagnosis.class);
		diagnosis.setDescription("test");
		diagnosis.setText("testText");
		AllTests.getModelService().save(diagnosis);
		((IEncounter) testSet[3]).addDiagnosis(diagnosis);

		Result<IInvoice> invoice = invoiceService.invoice(Collections.singletonList((IEncounter) testSet[3]));
		assertTrue(invoice.getMessages().toString(), invoice.isOK());

		// read from fhir endpoint
		Invoice readInvoice = client.read().resource(Invoice.class).withId(invoice.get().getId()).execute();

		assertEquals(InvoiceStatus.ISSUED, readInvoice.getStatus());
		assertEquals(readInvoice.getDate(), TimeUtil.toDate(invoice.get().getDate()));
		assertEquals(BigDecimal.valueOf(7.5), readInvoice.getTotalGross().getValue());
		assertEquals("CHF", readInvoice.getLineItem().get(0).getPriceComponent().get(0).getAmount().getCurrency());
		System.out.println(FhirUtil.serializeToString(readInvoice));
	}

	@Test
	public void getInvoiceForEncounter() {
		IBillingService billingService = OsgiServiceUtil.getService(IBillingService.class).orElseThrow();

		IArticle article = AllTests.getTestDatabaseInitializer().getArticle();
		assertNotNull(article);
		Result result = billingService.bill(article, AllTests.getTestDatabaseInitializer().getBehandlung(), 5);
		assertTrue(result.getMessages().toString(), result.isOK());

		String searchUrl = "Invoice?_query=by-encounter&encounter=Encounter/"
				+ AllTests.getTestDatabaseInitializer().getBehandlung().getId();

		Bundle results = client.search().byUrl(searchUrl).returnBundle(Bundle.class).execute();
		assertNotNull(results);

		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Invoice invoice = (Invoice) entries.get(0).getResource();
		assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
		assertEquals("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId(),
				invoice.getSubject().getReference());
		assertEquals(1, invoice.getLineItem().size());
		assertEquals(BigDecimal.valueOf(7.5), invoice.getTotalGross().getValue());

		System.out.println(FhirUtil.serializeToString(results));
	}

}
