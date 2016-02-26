package info.elexis.server.core.connector.elexis.billable;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.ICodeElement;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;

public interface IVerrechenbar extends ICodeElement {

	public IStatus add(Behandlung kons, String userId, String mandatorId);
	
}
