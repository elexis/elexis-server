package info.elexis.server.core.connector.elexis.billable;

import org.eclipse.core.runtime.IStatus;

import ch.rgw.tools.StringTool;
import info.elexis.server.core.connector.elexis.billable.optifier.LaborTarif2009Optifier;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;

public class VerrechenbarLabor2009Tarif implements IVerrechenbar{

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
	public IStatus add(Behandlung kons, String userId, String mandatorId) {
		return new LaborTarif2009Optifier().add(this, kons, userId, mandatorId);
	}

}
