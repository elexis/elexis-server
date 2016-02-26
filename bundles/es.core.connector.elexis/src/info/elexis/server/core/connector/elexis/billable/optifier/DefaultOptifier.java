package info.elexis.server.core.connector.elexis.billable.optifier;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.ObjectStatus;
import info.elexis.server.core.connector.elexis.billable.IVerrechenbar;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class DefaultOptifier implements IOptifier {

	private Verrechnet newVerrechnet;
	private Logger log;

	public IStatus optify(final Behandlung kons, String userId, String mandatorId) {
		return Status.OK_STATUS;
	}

	public IStatus add(final IVerrechenbar code, final Behandlung kons, final String userId, final String mandatorId) {
		Verrechnet foundVerrechnet = null;
		for (Verrechnet verrechnet : kons.getVerrechnet()) {
			 Optional<IVerrechenbar> vrElement = VerrechnetService.INSTANCE.getVerrechenbar(verrechnet);
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
		} else {
			newVerrechnet = VerrechnetService.INSTANCE.create(code, kons, 1);
		}
		return ObjectStatus.OK_STATUS(newVerrechnet);
	}

	public IStatus remove(final Verrechnet v, final Behandlung kons) {
		List<Verrechnet> old = kons.getVerrechnet();
		old.remove(v);
		VerrechnetService.INSTANCE.delete(v);
		return Status.OK_STATUS;
	}

}
