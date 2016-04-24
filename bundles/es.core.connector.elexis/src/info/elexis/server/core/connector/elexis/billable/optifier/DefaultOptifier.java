package info.elexis.server.core.connector.elexis.billable.optifier;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.status.ObjectStatus;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class DefaultOptifier implements IOptifier {

	private Verrechnet newVerrechnet;
	private Logger log;

	public IStatus optify(final Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		return Status.OK_STATUS;
	}

	public IStatus add(final IBillable code, final Behandlung kons, final Kontakt userContact,
			final Kontakt mandatorContact) {
		Verrechnet foundVerrechnet = null;
		for (Verrechnet verrechnet : kons.getVerrechnet()) {
			Optional<IBillable> vrElement = VerrechnetService.INSTANCE.getVerrechenbar(verrechnet);
			if (!vrElement.isPresent()) {
				// #2454 This should not happen, may however if we have to
				// consider
				// elements where the responsible plugin is not available
				if (log == null) {
					log = LoggerFactory.getLogger(DefaultOptifier.class);
				}

				log.error("IVerrechenbar is not resolvable in " + verrechnet.getId() + " is " + verrechnet.getKlasse()
						+ " available?");
				continue;
			}

			if (vrElement.get().getId().equals(code.getId())) {
				if (verrechnet.getText().equals(code.getText())) {
					foundVerrechnet = verrechnet;
					break;
				}
			}
		}

		if (foundVerrechnet != null) {
			VerrechnetService.INSTANCE.changeCount(foundVerrechnet, foundVerrechnet.getZahl() + 1);
			log.trace("Changed count on existing Verrechnet entry ({}): {}", foundVerrechnet.getId(),
					foundVerrechnet.getZahl() + 1);
			return ObjectStatus.OK_STATUS(foundVerrechnet);
		} else {
			newVerrechnet = VerrechnetService.INSTANCE.create(code, kons, 1);
			log.trace("Created new Verrechnet entry ({})", newVerrechnet.getId());
			return ObjectStatus.OK_STATUS(newVerrechnet);
		}
	}

	public IStatus remove(final Verrechnet v, final Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		VerrechnetService.INSTANCE.delete(v);
		return Status.OK_STATUS;
	}

}
