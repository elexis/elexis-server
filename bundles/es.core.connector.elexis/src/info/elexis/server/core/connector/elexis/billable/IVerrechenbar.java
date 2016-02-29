package info.elexis.server.core.connector.elexis.billable;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.ICodeElement;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public interface IVerrechenbar<T> extends ICodeElement {

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

}
