package info.elexis.server.core.connector.elexis.billable.optifier;

import org.eclipse.core.runtime.IStatus;

import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;

public interface IOptifier<T extends AbstractDBObjectIdDeleted> {
	
	public IStatus optify(Behandlung kons, Kontakt userContact, Kontakt mandatorContact);

	/**
	 * Eine Leistung einer Konsultation hinzufügen; die anderen Leistungen der
	 * Kons ggf. anpassen
	 * 
	 * @param code
	 *            der hinzuzufügende code
	 * @param kons
	 *            die Konsultation
	 * @param userContact
	 *            the user that this operation is executed for
	 * @param mandatorId
	 *            the mandator this operation is executed for
	 * @return Result mit der möglicherweise veränderten Liste
	 */
	public IStatus add(IBillable<T> code, Behandlung kons, Kontakt userContact, Kontakt mandatorContact);

	/**
	 * Eine Leistung aus einer Konsultation entfernen; die Liste ggf. anpassen
	 * 
	 * @param code
	 *            der zu enfternende code
	 * @return 
	 */
	public IStatus remove(Verrechnet code);
	
	
}
