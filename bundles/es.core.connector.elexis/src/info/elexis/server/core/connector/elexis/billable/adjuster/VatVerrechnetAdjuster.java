package info.elexis.server.core.connector.elexis.billable.adjuster;

import static ch.elexis.core.constants.Preferences.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.rgw.tools.Money;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.IBillable.VatInfo;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ObjVatInfo;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ObjVatInfo_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.ConfigService;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

/**
 * from at.medevit.medelexis.vat_ch.VatVerrechnetAdjuster
 */
public class VatVerrechnetAdjuster implements IBillableAdjuster {

	private Logger log = LoggerFactory.getLogger(VatVerrechnetAdjuster.class);

	private final SettingsPreferenceStore globalConf = new SettingsPreferenceStore();

	@Override
	public void adjust(Verrechnet verrechnet) {
		Optional<IBillable> verrechenbar = VerrechnetService.INSTANCE.getVerrechenbar(verrechnet);

		if (!verrechenbar.isPresent()) {
			log.warn("IBillable could not be found for Verrechnet " + verrechnet.getId() + ", returning.");
			return;
		}

		if (verrechenbar.get() instanceof ArtikelstammItem) {
			handleArtikel(verrechenbar.get(), verrechnet);
		} else {
			handleLeistung(verrechenbar.get(), verrechnet);
		}
	}

	private void handleArtikel(IBillable artikel, Verrechnet verrechnet) {
		EnumSet<VatInfo> info = getInfo(artikel);
		// first match will determine value !
		if (info.contains(VatInfo.VAT_CH_ISMEDICAMENT)) {
			setReduced(verrechnet);
		} else if (info.contains(VatInfo.VAT_CH_NOTMEDICAMENT)) {
			setNormal(verrechnet);
		} else if (info.contains(VatInfo.VAT_DEFAULT)) {
			setDefault(verrechnet);
		} else if (info.contains(VatInfo.VAT_NONE)) {
			setNoVat(verrechnet);
		}
	}

	private void handleLeistung(IBillable leistung, Verrechnet verrechnet) {
		EnumSet<VatInfo> info = getInfo(leistung);
		// first match will determine value !
		if (info.contains(VatInfo.VAT_NONE) || info.contains(VatInfo.VAT_CH_ISTREATMENT)) {
			setNoVat(verrechnet);
		} else if (info.contains(VatInfo.VAT_CH_NOTTREATMENT)) {
			setNormal(verrechnet);
		} else if (info.contains(VatInfo.VAT_DEFAULT)) {
			setDefault(verrechnet);
		}
	}

	/**
	 * Get the {@link VatInfo} for the {@link IVerrechenbar}. If the
	 * {@link IVerrechenbar} returns {@link VatInfo#VAT_DEFAULT} a lookup is
	 * performed in the mapping {@link ObjVatInfo}.
	 * 
	 * @param verrechenbar
	 * @return
	 */
	private EnumSet<VatInfo> getInfo(IBillable verrechenbar) {
		EnumSet<VatInfo> info = EnumSet.of(verrechenbar.getVatInfo());
		// if default is returned check if there is a mapping available
		if (info.contains(VatInfo.VAT_DEFAULT)) {
			JPAQuery<ObjVatInfo> query = new JPAQuery<ObjVatInfo>(ObjVatInfo.class);
			query.add(ObjVatInfo_.objectId, QUERY.EQUALS, verrechenbar.getEntity().getId());
			List<ObjVatInfo> infos = query.execute();

			if (infos.size() > 0) {
				info.clear();
				info.addAll(VatInfo.decodeFromString(infos.get(0).getVatinfo()));
			}
		}
		return info;
	}

	private void setDefault(Verrechnet verrechnet) {
		String defaultValue = globalConf.getString(PreferenceConstants.VAT_DEFAULTVALUE);
		if (PreferenceConstants.VAT_NORMALVALUE.equalsIgnoreCase(defaultValue))
			setNormal(verrechnet);
		else if (PreferenceConstants.VAT_REDUCEDVALUE.equalsIgnoreCase(defaultValue))
			setReduced(verrechnet);
		else if (PreferenceConstants.VAT_NOVATVALUE.equalsIgnoreCase(defaultValue))
			setNoVat(verrechnet);
	}

	private void setNoVat(Verrechnet verrechnet) {
		verrechnet.setDetail("vat_scale", Double.toString(0.0));
	}

	private void setReduced(Verrechnet verrechnet) {
		String reduziert = globalConf.getString(PreferenceConstants.VAT_REDUCEDVALUE);
		verrechnet.setDetail("vat_scale", reduziert);
	}

	private void setNormal(Verrechnet verrechnet) {
		String normal = globalConf.getString(PreferenceConstants.VAT_NORMALVALUE);
		verrechnet.setDetail("vat_scale", normal);
	}

	@Override
	public void adjustGetNettoPreis(Verrechnet verrechnet, Money price) {
		// do not change netto price
	}

	private class SettingsPreferenceStore {

		public String getString(String field) {
			String z = ConfigService.INSTANCE.get(field, null);
			if (z == null) {
				z = ConfigService.INSTANCE.get(field + SETTINGS_PREFERENCE_STORE_DEFAULT, null);
				if (z == null) {
					z = StringConstants.EMPTY;
				}
			}
			return z;
		}

	}

	private class PreferenceConstants {
		/**
		 * from at.medevit.medelexis.vat_ch.preferences.PreferenceConstants
		 */
		public static final String VAT_NORMALVALUE = "at.medevit.medelexis.vat_ch/NormalValue";
		public static final String VAT_REDUCEDVALUE = "at.medevit.medelexis.vat_ch/ReducedValue";
		public static final String VAT_NOVATVALUE = "at.medevit.medelexis.vat_ch/NoVatValue";

		public static final String VAT_DEFAULTVALUE = "at.medevit.medelexis.vat_ch/DefaultValue";

		public static final String VAT_ISMANDANTVAT = "at.medevit.medelexis.vat_ch/IsMandantVat";
		public static final String VAT_MANDANTVATNUMBER = "at.medevit.medelexis.vat_ch/MandantVatNumber";
	}

}
