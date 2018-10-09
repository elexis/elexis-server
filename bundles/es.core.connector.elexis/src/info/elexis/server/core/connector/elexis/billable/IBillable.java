//package info.elexis.server.core.connector.elexis.billable;
//
//import java.util.EnumSet;
//
//import org.eclipse.core.runtime.IStatus;
//
//import ch.elexis.core.model.ICodeElement;
//import ch.elexis.core.model.Identifiable;
//import ch.rgw.tools.Money;
//import ch.rgw.tools.TimeTool;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
//
//public interface IBillable<T extends Identifiable> extends ICodeElement {
//
//	/**
//	 * Definition von Informationen zu der Leistung welche für die MWSt relevant
//	 * sind.
//	 * <p>
//	 * Schweizer MWSt (at.medevit.medelexis.vat_ch):
//	 * <li>VAT_DEFAULT ... Standard MWST Satz laut Einstellungsseite</li>
//	 * <li>VAT_NONE ... Keine MWST</li>
//	 * <li>VAT_CH_ISMEDICAMENT ... Artikel ist als Medikament anerkannt</li>
//	 * <li>VAT_CH_NOTMEDICAMENT ... Artikel ist nicht als Medikament anerkannt</li>
//	 * <li>VAT_CH_ISTREATMENT ... Leistung ist als Heilbehandlung anerkannt</li>
//	 * <li>VAT_CH_NOTTREATMENT ... Leistung ist nicht als Heilbehandlung
//	 * anerkannt</li>
//	 * </p>
//	 */
//	public enum VatInfo {
//		VAT_DEFAULT, VAT_NONE, VAT_CH_ISMEDICAMENT, VAT_CH_NOTMEDICAMENT, VAT_CH_ISTREATMENT, VAT_CH_NOTTREATMENT;
//
//		/**
//		 * Get a String representation of a set of {@link VatInfo} elements for
//		 * persisting the information.
//		 * 
//		 * @param set
//		 * @return
//		 */
//		public static String encodeAsString(EnumSet<VatInfo> set) {
//			StringBuilder sb = new StringBuilder();
//
//			for (VatInfo info : set) {
//				if (sb.length() == 0)
//					sb.append(info.name());
//				else
//					sb.append("," + info.name());
//			}
//			return sb.toString();
//		}
//
//		/**
//		 * Get an EnumSet of {@link VatInfo} from a String representation produced with
//		 * {@link VatInfo#encodeAsString(EnumSet)}.
//		 * 
//		 * @param code
//		 * @return
//		 */
//		public static EnumSet<VatInfo> decodeFromString(String code) {
//			String[] names = code.split(",");
//			EnumSet<VatInfo> ret = EnumSet.noneOf(VatInfo.class);
//
//			for (int i = 0; i < names.length; i++) {
//				ret.add(VatInfo.valueOf(names[i]));
//			}
//			return ret;
//		}
//	};
//
//	/**
//	 * Add a single (count=1) service record to a consultation;
//	 * 
//	 * @param kons
//	 * @param userContact
//	 * @param mandatorContact
//	 * @return
//	 */
//	public default IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
//		return add(kons, userContact, mandatorContact, 1);
//	}
//
//	/**
//	 * Add a service record to a consultation.
//	 * 
//	 * @param kons
//	 * @param userContact
//	 * @param mandatorContact
//	 * @param count the number of times to add this element
//	 * @return
//	 * @since 1.5
//	 */
//	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact, float count);
//
//	/**
//	 * Uncharge the {@link IBillable} from the Behandlung it was charged (via #add)
//	 * to
//	 * 
//	 * @param vr
//	 * @param mandatorContact
//	 * @return
//	 */
//	public IStatus removeFromConsultation(Verrechnet vr, Kontakt mandatorContact);
//
//	public T getEntity();
//
//	/**
//	 * Get the (T)ax (P)oint value of the {@link IVerrechenbar}. Parameters are provided as context
//	 * to determine the correct value.
//	 * 
//	 * @param date
//	 * @param fall
//	 * @return
//	 */
//	public int getTP(TimeTool date, Fall fall);
//
//	/**
//	 * Get the (T)ax (P)oint value of the {@link IVerrechenbar}. Parameters are provided as context
//	 * to determine the correct value. This method was introduced because a context with
//	 * {@link Konsultation} was needed, as context with {@link IFall} was not specific enough. </br>
//	 * </br>
//	 * If parameter kons is null, value of {@link IVerrechenbar#getTP(TimeTool, IFall)} is returned.
//	 * 
//	 * @param date
//	 * @param kons
//	 * @return
//	 */
//	public default int getTP(TimeTool date,  Behandlung kons){
//		if (kons != null) {
//			return getTP(date, kons.getFall());
//		}
//		return getTP(date, (Fall) null);
//	}
//
//	public double getFactor(TimeTool dat, Fall fall);
//
//	default Money getCost(TimeTool dat) {
//		return new Money(0);
//	}
//
//	/** Die MWSt Informationen zu dieser Leistung */
//	public VatInfo getVatInfo();
//
//}
