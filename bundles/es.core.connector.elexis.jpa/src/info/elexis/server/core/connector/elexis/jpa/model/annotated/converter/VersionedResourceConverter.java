package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

import ch.rgw.tools.VersionedResource;

public class VersionedResourceConverter implements Converter {

	private static final long serialVersionUID = -8666863860943745367L;

	@Override
	public byte[] convertObjectValueToDataValue(Object objectValue, Session session) {
		if(objectValue instanceof VersionedResource) {
			VersionedResource vr = (VersionedResource) objectValue;
			return vr.serialize();
		}
		return null;
	}

	@Override
	public VersionedResource convertDataValueToObjectValue(Object dataValue, Session session) {
		return VersionedResource.load((byte[]) dataValue);
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {}

}
