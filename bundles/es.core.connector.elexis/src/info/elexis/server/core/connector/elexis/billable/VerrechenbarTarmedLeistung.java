package info.elexis.server.core.connector.elexis.billable;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class VerrechenbarTarmedLeistung implements IBillable<TarmedLeistung> {

	private final TarmedLeistung tarmedLeistung;

	public VerrechenbarTarmedLeistung(TarmedLeistung tarmedLeistung) {
		this.tarmedLeistung = tarmedLeistung;
	}

	@Override
	public String getCodeSystemName() {
		return "Tarmed";
	}

	@Override
	public String getCodeSystemCode() {
		return "999";
	}

	@Override
	public String getId() {
		return tarmedLeistung.getId();
	}

	@Override
	public String getCode() {
		String code = tarmedLeistung.getCode();
		if (code != null && !code.isEmpty())
			return code;
		else
			return getId();
	}

	@Override
	public String getText() {
		return tarmedLeistung.getTx255();
	}

	@Override
	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact, float count) {
		return new TarmedOptifier().add(this, kons, userContact, mandatorContact, count);
	}

	@Override
	public IStatus removeFromConsultation(Verrechnet vr, Kontakt mandatorContact) {
		return new TarmedOptifier().remove(vr);
	}

	@Override
	public List<Object> getActions(Object context) {
		return null;
	}

	@Override
	public TarmedLeistung getEntity() {
		return tarmedLeistung;
	}

	public int getTP(final TimeTool date, final Fall fall) {
		return (tarmedLeistung.getTL() + tarmedLeistung.getAL());
	}
	
	@Override
	public int getTP(TimeTool date, Behandlung kons) {
		if (kons != null) {
			return (tarmedLeistung.getTL() + TarmedLeistungService.getAL(tarmedLeistung, kons.getMandant()));
		}
		return (tarmedLeistung.getTL() + tarmedLeistung.getAL());
	}

	@Override
	public double getFactor(TimeTool date, Fall fall) {
		String billingSystem = FallService.getAbrechnungsSystem(fall);
		return VerrechnetService.getVKMultiplikator(date, billingSystem);
	}

	@Override
	public VatInfo getVatInfo() {
		return VatInfo.VAT_CH_ISTREATMENT;
	}

}
