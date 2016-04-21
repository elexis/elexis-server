package info.elexis.server.core.connector.elexis.billable;

import java.util.EnumSet;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.ICodeElement;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public interface IBillable<T extends AbstractDBObjectIdDeleted> extends ICodeElement {
	
	/**
	 * Definition von Informationen zu der Leistung welche für die MWSt relevant sind.
	 * <p>
	 * Schweizer MWSt (at.medevit.medelexis.vat_ch):
	 * <li>VAT_DEFAULT ... Standard MWST Satz laut Einstellungsseite</li>
	 * <li>VAT_NONE ... Keine MWST</li>
	 * <li>VAT_CH_ISMEDICAMENT ... Artikel ist als Medikament anerkannt</li>
	 * <li>VAT_CH_NOTMEDICAMENT ... Artikel ist nicht als Medikament anerkannt</li>
	 * <li>VAT_CH_ISTREATMENT ... Leistung ist als Heilbehandlung anerkannt</li>
	 * <li>VAT_CH_NOTTREATMENT ... Leistung ist nicht als Heilbehandlung anerkannt</li>
	 * </p>
	 */
	public enum VatInfo {
		VAT_DEFAULT, VAT_NONE, VAT_CH_ISMEDICAMENT, VAT_CH_NOTMEDICAMENT, VAT_CH_ISTREATMENT,
			VAT_CH_NOTTREATMENT;
		
		/**
		 * Get a String representation of a set of {@link VatInfo} elements for persisting the
		 * information.
		 * 
		 * @param set
		 * @return
		 */
		public static String encodeAsString(EnumSet<VatInfo> set){
			StringBuilder sb = new StringBuilder();
			
			for (VatInfo info : set) {
				if (sb.length() == 0)
					sb.append(info.name());
				else
					sb.append("," + info.name());
			}
			return sb.toString();
		}
		
		/**
		 * Get an EnumSet of {@link VatInfo} from a String representation produced with
		 * {@link VatInfo#encodeAsString(EnumSet)}.
		 * 
		 * @param code
		 * @return
		 */
		public static EnumSet<VatInfo> decodeFromString(String code){
			String[] names = code.split(",");
			EnumSet<VatInfo> ret = EnumSet.noneOf(VatInfo.class);
			
			for (int i = 0; i < names.length; i++) {
				ret.add(VatInfo.valueOf(names[i]));
			}
			return ret;
		}
	};

	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact);

	public T getEntity();

	/**
	 * Betrag dieser Verrechenbar (in TP*100) an einem bestimmten Datum liefern
	 */
	public int getTP(TimeTool date, Fall fall);

	public double getFactor(TimeTool dat, Fall fall);

	default Money getCost(TimeTool dat) {
		return new Money(0);
	}
	
	/** Die MWSt Informationen zu dieser Leistung */
	public VatInfo getVatInfo();

}
