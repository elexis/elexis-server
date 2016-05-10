package info.elexis.server.core.connector.elexis.billable;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.optifier.DefaultOptifier;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Eigenleistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class VerrechenbarEigenleistung implements IBillable<Eigenleistung> {

	protected Logger log = LoggerFactory.getLogger(VerrechenbarEigenleistung.class);

	private final Eigenleistung eigenleistung;

	public VerrechenbarEigenleistung(Eigenleistung eigenleistung) {
		this.eigenleistung = eigenleistung;
	}

	@Override
	public List<Object> getActions(Object arg0) {
		return null;
	}

	@Override
	public String getCode() {
		return eigenleistung.getCode();
	}

	@Override
	public String getCodeSystemCode() {
		return "999";
	}

	@Override
	public String getCodeSystemName() {
		return "Eigenleistung";
	}

	@Override
	public String getId() {
		return eigenleistung.getId();
	}

	@Override
	public String getText() {
		return eigenleistung.getDescription();
	}

	@Override
	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		return new DefaultOptifier().add(this, kons, userContact, mandatorContact);
	}

	@Override
	public Eigenleistung getEntity() {
		return eigenleistung;
	}

	@Override
	public int getTP(TimeTool date, Fall fall) {
		String salePrice = eigenleistung.getSalePrice();
		Money m = new Money();
		m.addCent(salePrice);
		return m.getCents();
	}

	@Override
	public double getFactor(TimeTool dat, Fall fall) {
		return 1.0;
	}

	@Override
	public VatInfo getVatInfo() {
		return VatInfo.VAT_DEFAULT;
	}

}
