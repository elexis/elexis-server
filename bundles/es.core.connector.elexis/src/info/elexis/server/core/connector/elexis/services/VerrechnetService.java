package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.ICodeElement;
import info.elexis.server.core.connector.elexis.billable.IVerrechenbar;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarLabor2009Tarif;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;

public class VerrechnetService extends AbstractService<Verrechnet> {

	private static Logger log = LoggerFactory.getLogger(VerrechnetService.class);

	public static VerrechnetService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final VerrechnetService INSTANCE = new VerrechnetService();
	}

	private VerrechnetService() {
		super(Verrechnet.class);
	}

	public Verrechnet create(Behandlung kons, Kontakt userKontakt) {
		Verrechnet v = create(false);
		v.setBehandlung(kons);
		v.setUser(userKontakt);
		flush();
		return v;
	}

	public Optional<IVerrechenbar> getVerrechenbar(Verrechnet vr) {
		String klasse = vr.getKlasse();
		String leistungenCode = vr.getLeistungenCode();

		Optional<AbstractDBObjectIdDeleted> object = StoreToStringService.INSTANCE
				.createDetachedFromString(klasse + StringConstants.DOUBLECOLON + leistungenCode);
		if (object.isPresent()) {
			return Optional.ofNullable(createVerrechenbarForObject(object.get()));
		}
		return Optional.empty();
	}

	private IVerrechenbar createVerrechenbarForObject(AbstractDBObjectIdDeleted object) {
		if (object instanceof TarmedLeistung) {
			return new VerrechenbarTarmedLeistung((TarmedLeistung) object);
		} else if (object instanceof Labor2009Tarif) {
			return new VerrechenbarLabor2009Tarif((Labor2009Tarif) object);
		}

		log.warn("Unsupported object for create verrechenbar {}", object.getClass().getName());
		return null;
	}

	public Verrechnet create(IVerrechenbar ce, Behandlung kons, int count) {
		Verrechnet v = create(false);
		v.setLeistungenText(ce.getText());
		// TODO ElexisTypeMap
		v.setLeistungenCode(ce.getId());
		v.setBehandlung(kons);
		v.setZahl(count);
		flush();
		return v;
	}
	

	public void changeCount(Verrechnet foundVerrechnet, int i) {
		// TODO Auto-generated method stub

	}
}
