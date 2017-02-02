package info.elexis.server.core.connector.elexis.billable.optifier;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.article.IArticle;
import ch.elexis.core.status.ObjectStatus;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarArtikel;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.StockService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class DefaultOptifier implements IOptifier {

	private Verrechnet newVerrechnet;
	private Logger log = LoggerFactory.getLogger(DefaultOptifier.class);

	public IStatus optify(final Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		return Status.OK_STATUS;
	}

	public IStatus add(final IBillable code, final Behandlung kons, final Kontakt userContact,
			final Kontakt mandatorContact) {
		Verrechnet foundVerrechnet = null;
		for (Verrechnet verrechnet : VerrechnetService.getAllVerrechnetForBehandlung(kons)) {
			Optional<IBillable> vrElement = VerrechnetService.getVerrechenbar(verrechnet);
			if (!vrElement.isPresent()) {
				// #2454 This should not happen, may however if we have to
				// consider
				// elements where the responsible plugin is not available
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
			DefaultOptifier.changeCount(foundVerrechnet, foundVerrechnet.getZahl() + 1);
			log.trace("Changed count on existing Verrechnet entry ({}): {}", foundVerrechnet.getId(),
					foundVerrechnet.getZahl() + 1);
			return ObjectStatus.OK_STATUS(foundVerrechnet);
		} else {
			newVerrechnet = new VerrechnetService.Builder(code, kons, 1, userContact).buildAndSave();
			log.trace("Created new Verrechnet entry ({})", newVerrechnet.getId());
			return ObjectStatus.OK_STATUS(newVerrechnet);
		}
	}

	public IStatus remove(final Verrechnet code) {
		DefaultOptifier.changeCount(code, code.getZahl() - 1);
		log.trace("Changed count on existing Verrechnet entry [{}]: {}", code.getId(), code.getZahl() - 1);
		return ObjectStatus.OK_STATUS(code);
	}

	protected static void changeCount(Verrechnet vr, int newCount) {
		int previous = vr.getZahl();
		int count = 1;

		if (newCount == Math.rint(newCount)) {
			// integer
			count = new Double(newCount).intValue();
			vr.setZahl(count);
			vr.setSecondaryScaleFactor(1.0);
		} else {
			vr.setZahl(count);
			vr.setSecondaryScaleFactor(newCount);
			vr.setLeistungenText(vr.getLeistungenText() + " (" + Double.toString(newCount) + ")");
		}
		vr = (Verrechnet) VerrechnetService.save(vr);

		Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(vr);
		if (verrechenbar.isPresent() && ((verrechenbar.get() instanceof VerrechenbarArtikelstammItem)
				|| (verrechenbar.get() instanceof VerrechenbarArtikel))) {
			int diff = newCount - previous;
			if (diff < 0) {
				new StockService().performSingleReturn((IArticle) verrechenbar.get().getEntity(), Math.abs(diff), null);
			} else if (diff > 0) {
				new StockService().performSingleDisposal((IArticle) verrechenbar.get().getEntity(), diff, null);
			}
		}
	}

}
