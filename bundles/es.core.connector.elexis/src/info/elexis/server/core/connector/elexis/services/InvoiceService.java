package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.Optional;

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
public class InvoiceService extends PersistenceService {
	public static class Builder extends AbstractBuilder<Invoice> {
		public Builder(String invoiceNumber, Kontakt mandator, Fall fall, LocalDate from, final LocalDate to,
				final Money amount, final InvoiceState state) {
			object = new Invoice();
			object.setNumber(invoiceNumber);
			object.setMandator(mandator);
			object.setFall(fall);
			object.setInvoiceDate(LocalDate.now());
			object.setInvoiceDateFrom(from);
			object.setInvoiceDateTo(to);
			object.setAmount(amount.toString());
			object.setState(state);
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Invoice> load(String id) {
		return PersistenceService.load(Invoice.class, id).map(v -> (Invoice) v);
	}

}
