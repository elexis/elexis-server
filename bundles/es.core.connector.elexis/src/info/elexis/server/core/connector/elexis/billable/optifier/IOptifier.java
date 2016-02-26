package info.elexis.server.core.connector.elexis.billable.optifier;

import org.eclipse.core.runtime.IStatus;

import info.elexis.server.core.connector.elexis.billable.IVerrechenbar;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;

public interface IOptifier {
	
	public IStatus optify(Behandlung kons, String userId, String mandatorId);

	/**
	 * Eine Leistung einer Konsultation hinzufügen; die anderen Leistungen der
	 * Kons ggf. anpassen
	 * 
	 * @param code
	 *            der hinzuzufügende code
	 * @param kons
	 *            die Konsultation
	 * @param userId
	 *            the user that this operation is executed for
	 * @param mandatorId
	 *            the mandator this operation is executed for
	 * @return Result mit der möglicherweise veränderten Liste
	 */
	public IStatus add(IVerrechenbar code, Behandlung kons, String userId, String mandatorId);

	/**
	 * Eine Leistung aus einer Konsultation entfernen; die Liste ggf. anpassen
	 * 
	 * @param code
	 *            der zu enfternende code
	 * @param kons
	 *            die KOnsultation
	 * @return 
	 */
	public IStatus remove(Verrechnet code, Behandlung kons);
}
