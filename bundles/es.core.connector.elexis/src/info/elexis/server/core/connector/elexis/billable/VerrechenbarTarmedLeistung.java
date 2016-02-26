package info.elexis.server.core.connector.elexis.billable;

import org.eclipse.core.runtime.IStatus;

import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;

public class VerrechenbarTarmedLeistung implements IVerrechenbar{

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
	public IStatus add(Behandlung kons, String userId, String mandatorId) {
		return new TarmedOptifier().add(this, kons, userId, mandatorId);
	}
	

}
