package info.elexis.server.core.connector.elexis.billable;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class VerrechenbarTarmedLeistung implements IVerrechenbar<TarmedLeistung> {

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
	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		return new TarmedOptifier().add(this, kons, userContact, mandatorContact);
	}

	@Override
	public List<Object> getActions(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TarmedLeistung getEntity() {
		return tarmedLeistung;
	}
	
	public int getTP(final TimeTool date, final Fall fall){
		String t = (String) tarmedLeistung.getExtension().getLimits().get("TP_TL");
		String a = (String) tarmedLeistung.getExtension().getLimits().get("TP_AL");
		double tl = 0.0;
		double al = 0.0;
		try {
			tl = (t == null) ? 0.0 : Double.parseDouble(t);
		} catch (NumberFormatException ex) {
			tl = 0.0;
		}
		try {
			al = (a == null) ? 0.0 : Double.parseDouble(a);
		} catch (NumberFormatException ex) {
			al = 0.0;
		}
		return (int) Math.round((tl + al) * 100.0);
	}

	@Override
	public double getFactor(TimeTool date, Fall fall) {
		String billingSystem = FallService.getAbrechnungsSystem(fall);
		return VerrechnetService.INSTANCE.getVKMultiplikator(date, billingSystem);
	}

}
