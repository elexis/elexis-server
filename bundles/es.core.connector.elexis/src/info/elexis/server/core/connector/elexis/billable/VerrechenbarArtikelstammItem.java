package info.elexis.server.core.connector.elexis.billable;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.optifier.DefaultOptifier;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;

public class VerrechenbarArtikelstammItem implements IBillable<ArtikelstammItem> {

	protected Logger log = LoggerFactory.getLogger(VerrechenbarArtikelstammItem.class);

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
			String type = artikelstammItem.getType();
			if (type != null && type.length() > 0) {
				String t = type.substring(0, 1);
				if (t.equalsIgnoreCase("P")) {
					return "402";
				} else if (t.equalsIgnoreCase("N")) {
					return "406";
				}
			}
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
		int vkt = 0;
		double vpe = artikelstammItem.getPackageUnit();
		double vke = artikelstammItem.getSellingUnit();

		String ppub = artikelstammItem.getPpub();
		try {
			vkt = Math.abs(new Money(ppub).getCents());
		} catch (Exception e) {
			log.warn("ArtikelstammItem [{}] error parsing public price [{}]", artikelstammItem.getId(), ppub);
		}

		return VerrechenbarArtikel.determineTP(date, fall, vpe, vke, vkt);
	}

	@Override
	public double getFactor(TimeTool dat, Fall fall) {
		return 1;
	}

	@Override
	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		// if(!artikelstammItem.isSl_entry()) {
		// return new NoObligationOptifier().add(this, kons, userContact,
		// mandatorContact);
		// }
		return new DefaultOptifier().add(this, kons, userContact, mandatorContact);
	}

	@Override
	public IStatus removeFromConsultation(Verrechnet vr, Kontakt mandatorContact) {
		return new DefaultOptifier().remove(vr);
	}

	@Override
	public VatInfo getVatInfo() {
		String overridenVat = (String) artikelstammItem.getExtInfoAsString("VAT_OVERRIDE");
		if (overridenVat != null) {
			return VatInfo.valueOf(overridenVat);
		}

		switch (artikelstammItem.getType().trim()) {
		case "P":
			return VatInfo.VAT_CH_ISMEDICAMENT;
		case "N":
			return VatInfo.VAT_CH_NOTMEDICAMENT;
		}
		return VatInfo.VAT_NONE;
	}

}
