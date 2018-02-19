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
	default IStatus add(IBillable<T> code, Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		return add(code, kons, userContact, mandatorContact, 1);
	}
	
	public IStatus add(IBillable<T> code, Behandlung kons, Kontakt userContact, Kontakt mandatorContact, float count);

	/**
	 * Eine Leistung aus einer Konsultation entfernen; die Liste ggf. anpassen
	 * 
	 * @param code
	 *            der zu enfternende code
	 * @return 
	 */
	public IStatus remove(Verrechnet code);
	
	/**
	 * Add an object to the context of the {@link IOptifier} implementation. If a object for the
	 * provided key already exists, the value is replaced.
	 * 
	 * @param key
	 * @param value
	 */
	default void putContext(String key, Object value){
		// default do nothing implement in subclass
	}
	
	/**
	 * Add an implementation specific context object. If a object for the provided key already
	 * exists, the value is replaced.
	 */
	default void clearContext(){
		// default do nothing implement in subclass
	}
}
