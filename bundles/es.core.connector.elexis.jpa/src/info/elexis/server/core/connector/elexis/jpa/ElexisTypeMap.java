package info.elexis.server.core.connector.elexis.jpa;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Faelle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;

public class ElexisTypeMap {
	private static final BidiMap<String, Class<? extends AbstractDBObject>> stsClassBidiMap;

	static {
		stsClassBidiMap = new DualHashBidiMap<String, Class<? extends AbstractDBObject>>();
		stsClassBidiMap.put("ch.artikelstamm.elexis.common.ArtikelstammItem", ArtikelstammItem.class);
		stsClassBidiMap.put("ch.elexis.data.TarmedLeistung", TarmedLeistung.class);
		stsClassBidiMap.put("ch.elexis.data.Fall", Faelle.class);
		// TODO add other values
	}

	// TODO we can not deterministically map person to patient, anwender, mandant as
	// we do not know what was initially intended
	public static String getKeyForObject(AbstractDBObject obj) {
		if (obj instanceof Kontakt) {
			Kontakt k = (Kontakt) obj;
			if (k.isIstPerson()) {
				if (k.isIstPatient()) {
					return "ch.elexis.data.Patient";
				}
				return "ch.elexis.data.Person";
			} else if (k.isIstOrganisation()) {
				return "ch.elexis.data.Organisation";
			}

			return "ch.elexis.data.Kontakt";
		}
		return stsClassBidiMap.getKey(obj.getClass());
	}

	public static Class<? extends AbstractDBObject> get(String value) {
		return stsClassBidiMap.get(value);
	}
}
