//package info.elexis.server.core.connector.elexis.billable.adjuster;
//
//import ch.rgw.tools.Money;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
//
///**
// * Implementations of {@link IVerrechnetAdjuster} can adjust a
// * {@link Verrechnet} as it is created.
// * 
// * @author thomas
// * 
// */
//public interface IBillableAdjuster {
//	/**
//	 * Adjust the created {@link Verrechnet}.
//	 * 
//	 * @param verrechnet
//	 *            the Verrechnet object to adjust
//	 */
//	public void adjust(Verrechnet verrechnet);
//
//	/**
//	 * Adjust netto price of {@link Verrechnet}.
//	 * 
//	 * @param verrechnet
//	 *            the Verrechnet object this price belongs to
//	 * @param price
//	 *            the price to adjust
//	 */
//	public void adjustGetNettoPreis(Verrechnet verrechnet, Money price);
//}
