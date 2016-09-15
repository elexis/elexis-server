package info.elexis.server.core.connector.elexis.billable;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.article.Constants;
import ch.elexis.core.model.eigenartikel.EigenartikelTyp;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.optifier.DefaultOptifier;
import info.elexis.server.core.connector.elexis.common.POHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class VerrechenbarArtikel implements IBillable<Artikel> {

	private Logger log = LoggerFactory.getLogger(VerrechenbarArtikel.class);

	private final Artikel article;

	public VerrechenbarArtikel(Artikel article) {
		this.article = article;
	}

	@Override
	public String getCodeSystemName() {
		return article.getTyp();
	}

	@Override
	public String getCodeSystemCode() {
		return "999";
	}

	@Override
	public String getId() {
		return article.getId();
	}

	@Override
	public String getCode() {
		if (Artikel.TYP_EIGENARTIKEL.equals(article.getTyp())) {
			article.getSubId();
		}
		return article.getId();
	}

	@Override
	public String getText() {
		return article.getLabel();
	}

	@Override
	public List<Object> getActions(Object context) {
		return null;
	}

	@Override
	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		return new DefaultOptifier().add(this, kons, userContact, mandatorContact);
	}

	@Override
	public Artikel getEntity() {
		return article;
	}

	@Override
	public int getTP(TimeTool date, Fall fall) {
		int vkt = 0;
		double vpe = 0.0;
		double vke = 0.0;

		try {
			vkt = new Money(article.getVkPreis()).getCents();
		} catch (Exception e) {
			log.warn("Error parsing public price: " + e.getMessage() + " @ " + article.getId());
		}

		try {
			vpe = Double.parseDouble(article.getExtInfoAsString(Constants.FLD_EXT_PACKAGE_UNIT_INT));
		} catch (Exception e) {
			log.warn("Error parsing package size: " + e.getMessage() + "@ " + article.getId());
		}

		try {
			vke = Double.parseDouble(article.getExtInfoAsString(Artikel.FLD_EXTINFO_SELLUNIT));
		} catch (Exception e) {
			log.warn("Error parsing sell unit: " + e.getMessage() + " @ " + article.getId());
		}

		return determineTP(date, fall, vpe, vke, vkt);
	}

	@Override
	public double getFactor(TimeTool dat, Fall fall) {
		return 1;
	}

	@Override
	public VatInfo getVatInfo() {
		if(Artikel.TYP_EIGENARTIKEL.equalsIgnoreCase(article.getTyp())) {
			EigenartikelTyp eat = EigenartikelTyp.byCharSafe(article.getCodeclass());
			switch (eat) {
			case PHARMA:
			case MAGISTERY:
				return VatInfo.VAT_CH_ISMEDICAMENT;
			case NONPHARMA:
				return VatInfo.VAT_CH_NOTMEDICAMENT;
			default:
				break;
			}
			return VatInfo.VAT_NONE;
		}
		
		return VatInfo.VAT_DEFAULT;
	}

	public static int determineTP(TimeTool date, Fall fall, double vpe, double vke, int vkt) {
		if ((vpe > 0.0) && (vke > 0.0) && (vpe != vke)) {
			return (int) Math.round(vke * (vkt / vpe));
		} else {
			return vkt;
		}
	}

	public void singleReturn(int n) {
		int anbruch = POHelper.checkZero(article.getExtInfoAsString(Constants.FLD_EXT_BEGINNING_PACKAGE));
		int ve = POHelper.checkZero(article.getExtInfoAsString(Constants.FLD_EXT_SELL_UNIT));
		int vk = POHelper.checkZero(article.getExtInfoAsString(Constants.FLD_EXT_PACKAGE_UNIT_INT));
		int num = n * ve;
		if (vk == ve) {
			article.setIstbestand(article.getIstbestand() + n);
		} else {
			int rest = anbruch + num;
			while (rest > vk) {
				rest = rest - vk;
				article.setIstbestand(article.getIstbestand() + 1);
			}
			article.setExtInfoValue(Constants.FLD_EXT_BEGINNING_PACKAGE, Integer.toString(rest));
		}

	}

	public void singleDisposal(int n) {
		int anbruch = POHelper.checkZero(article.getExtInfoAsString(Constants.FLD_EXT_BEGINNING_PACKAGE));
		int ve = POHelper.checkZero(article.getExtInfoAsString(Constants.FLD_EXT_SELL_UNIT));
		int vk = POHelper.checkZero(article.getExtInfoAsString(Constants.FLD_EXT_PACKAGE_UNIT_INT));
		if (vk == 0) {
			if (ve != 0) {
				vk = ve;
			}
		}
		if (ve == 0) {
			if (vk != 0) {
				ve = vk;
				article.setExtInfoValue(Constants.FLD_EXT_PACKAGE_UNIT_INT, Integer.toString(ve));
			}
		}
		int num = n * ve;
		if (vk == ve) {
			article.setIstbestand(article.getIstbestand() - n);
		} else {
			int rest = anbruch - num;
			while (rest < 0) {
				rest = rest + vk;
				article.setIstbestand(article.getIstbestand() - 1);
			}
			article.setExtInfoValue(Constants.FLD_EXT_BEGINNING_PACKAGE, Integer.toString(rest));
		}
	}
	
}
