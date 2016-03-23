package info.elexis.server.core.connector.elexis.billable;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.optifier.LaborTarif2009Optifier;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class VerrechenbarLabor2009Tarif implements IBillable<Labor2009Tarif> {

	private final Labor2009Tarif laborTarif;

	public VerrechenbarLabor2009Tarif(Labor2009Tarif laborTarif) {
		this.laborTarif = laborTarif;
	}

	@Override
	public String getCodeSystemName() {
		return "EAL 2009";
	}

	@Override
	public String getCodeSystemCode() {
		return "317";
	}

	@Override
	public String getId() {
		return laborTarif.getId();
	}

	@Override
	public String getCode() {
		return laborTarif.getCode();
	}

	@Override
	public String getText() {
		return StringTool.getFirstLine(laborTarif.getName(), 80);
	}

	@Override
	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		return new LaborTarif2009Optifier().add(this, kons, userContact, mandatorContact);
	}

	@Override
	public List<Object> getActions(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Labor2009Tarif getEntity() {
		return laborTarif;
	}

	@Override
	public int getTP(TimeTool date, Fall fall) {
		double tp = 0.0d;
		try {
			tp = Double.parseDouble(laborTarif.getTp());
		} catch (Exception e) {
		}
		return (int) Math.round(tp * 100.0);
	}

	@Override
	public double getFactor(TimeTool date, Fall fall) {
		return VerrechnetService.INSTANCE.getVKMultiplikator(date, "EAL2009");
	}

}
