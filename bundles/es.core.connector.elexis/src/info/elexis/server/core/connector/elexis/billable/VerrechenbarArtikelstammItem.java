package info.elexis.server.core.connector.elexis.billable;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.optifier.DefaultOptifier;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class VerrechenbarArtikelstammItem implements IBillable<ArtikelstammItem> {

	private final ArtikelstammItem artikelstammItem;

	public VerrechenbarArtikelstammItem(ArtikelstammItem artikelstammItem) {
		this.artikelstammItem = artikelstammItem;
	}

	@Override
	public String getCodeSystemName() {
		return "Artikelstamm";
	}

	@Override
	public String getCodeSystemCode() {
		String gtin = artikelstammItem.getGtin();
		if (gtin != null && gtin.length() > 3) {
			return "402";
		}
		return "999";
	}

	@Override
	public String getId() {
		return artikelstammItem.getId();
	}

	@Override
	public String getCode() {
		return artikelstammItem.getPhar();
	}

	@Override
	public String getText() {
		return artikelstammItem.getLabel();
	}

	@Override
	public List<Object> getActions(Object context) {
		return null;
	}

	@Override
	public ArtikelstammItem getEntity() {
		return artikelstammItem;
	}

	@Override
	public int getTP(TimeTool date, Fall fall) {
		double vkt = 0.0;
		double vpe = 0.0;
		double vke = 0.0;

		try {
			vkt = Double.parseDouble(artikelstammItem.getPexf());
		} catch (Exception e) {
		}

		try {
			vpe = Double.parseDouble(artikelstammItem.getExtInfoAsString("Verpackungseinheit"));
		} catch (Exception e) {
		}

		try {
			vke = Double.parseDouble(artikelstammItem.getExtInfoAsString("Verkaufseinheit"));
		} catch (Exception e) {
		}

		if ((vpe > 0.0) && (vke > 0.0) && (vpe != vke)) {
			return (int) Math.round(vke * (vkt / vpe));
		} else {
			return (int) Math.round(vkt);
		}
	}

	@Override
	public double getFactor(TimeTool dat, Fall fall) {
		return 1;
	}

	@Override
	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		// VatInfo vatInfo = getVatInfo();
		// if (!vatInfo.equals(VatInfo.VAT_CH_ISMEDICAMENT))
		// return noObligationOptifier;
		// return defaultOptifier;
		return new DefaultOptifier().add(this, kons, userContact, mandatorContact);
	}

}
