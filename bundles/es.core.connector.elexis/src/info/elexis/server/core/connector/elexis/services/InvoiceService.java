package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;

import ch.elexis.core.model.InvoiceState;
import ch.rgw.tools.Money;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Invoice;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

/**
 * This class does merely allow to create instances of Invoice elements. In
 * terms of supporting billing of consultations etc. however, this class is not
 * usable; the resp. code has not yet been ported.
 *
 */
public class InvoiceService extends AbstractService<Invoice> {
	public static InvoiceService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final InvoiceService INSTANCE = new InvoiceService();
	}

	private InvoiceService() {
		super(Invoice.class);
	}

	public Invoice create(String invoiceNumber, Kontakt mandator, Fall fall, LocalDate from, final LocalDate to,
			final Money amount, final InvoiceState state) {
		em.getTransaction().begin();
		Invoice invoice = create(false);
		invoice.setNumber(invoiceNumber);
		em.merge(mandator);
		invoice.setMandator(mandator);
		em.merge(fall);
		invoice.setFall(fall);
		invoice.setInvoiceDate(LocalDate.now());
		invoice.setInvoiceDateFrom(from);
		invoice.setInvoiceDateTo(to);
		invoice.setAmount(amount.toString());
		invoice.setState(state);
		em.getTransaction().commit();
		return invoice;
	}
}
