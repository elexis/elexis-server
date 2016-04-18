package info.elexis.server.core.connector.elexis.jpa;

import java.util.HashMap;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Brief;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.DocHandle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.PhysioLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin;

public class ElexisTypeMap {
	
	private static final HashMap<String, Class<? extends AbstractDBObjectIdDeleted>> stsToClassMap;
	private static final HashMap<Class<? extends AbstractDBObjectIdDeleted>, String> classToStsMap;

	public static final String TYPE_ARTIKELSTAMM = "ch.artikelstamm.elexis.common.ArtikelstammItem";
	public static final String TYPE_TARMEDLEISTUNG = "ch.elexis.data.TarmedLeistung";
	public static final String TYPE_FALL = "ch.elexis.data.Fall";
	public static final String TYPE_PATIENT = "ch.elexis.data.Patient";
	public static final String TYPE_PERSON = "ch.elexis.data.Person";
	public static final String TYPE_ORGANISATION = "ch.elexis.data.Organisation";
	public static final String TYPE_KONTAKT = "ch.elexis.data.Kontakt";
	public static final String TYPE_TERMIN = "ch.elexis.agenda.data.Termin";
	public static final String TYPE_DOCHANDLE = "ch.elexis.omnivore.data.DocHandle";
	public static final String TYPE_BRIEF = "ch.elexis.data.Brief";
	public static final String TYPE_PRESCRIPTION = "ch.elexis.data.Prescription";
	public static final String TYPE_KONSULTATION = "ch.elexis.data.Konsultation";
	public static final String TYPE_PHYSIOLEISTUNG = "ch.elexis.data.PhysioLeistung";
	public static final String TYPE_LABOR2009TARIF = "ch.elexis.labortarif2009.data.Labor2009Tarif";
	public static final String TYPE_LABRESULT = "ch.elexis.data.LabResult";
	public static final String TYPE_TESSINER_CODE = "ch.elexis.data.TICode";
	
	static {
		stsToClassMap = new HashMap<String, Class<? extends AbstractDBObjectIdDeleted>>();
		classToStsMap = new HashMap<Class<? extends AbstractDBObjectIdDeleted>, String>();

		// bi-directional mappable
		stsToClassMap.put(TYPE_ARTIKELSTAMM, ArtikelstammItem.class);
		classToStsMap.put(ArtikelstammItem.class, TYPE_ARTIKELSTAMM);
		stsToClassMap.put(TYPE_TARMEDLEISTUNG, TarmedLeistung.class);
		classToStsMap.put(TarmedLeistung.class, TYPE_TARMEDLEISTUNG);
		stsToClassMap.put(TYPE_FALL, Fall.class);
		classToStsMap.put(Fall.class, TYPE_FALL);
		stsToClassMap.put(TYPE_TERMIN, Termin.class);
		classToStsMap.put(Termin.class, TYPE_TERMIN);
		stsToClassMap.put(TYPE_DOCHANDLE, DocHandle.class);
		classToStsMap.put(DocHandle.class, TYPE_DOCHANDLE);
		stsToClassMap.put(TYPE_BRIEF, Brief.class);
		classToStsMap.put(Brief.class, TYPE_BRIEF);
		stsToClassMap.put(TYPE_PRESCRIPTION, Prescription.class);
		classToStsMap.put(Prescription.class, TYPE_PRESCRIPTION);
		stsToClassMap.put(TYPE_KONSULTATION, Behandlung.class);
		classToStsMap.put(Behandlung.class, TYPE_KONSULTATION);
		stsToClassMap.put(TYPE_PHYSIOLEISTUNG, PhysioLeistung.class);
		classToStsMap.put(PhysioLeistung.class, TYPE_PHYSIOLEISTUNG);
		stsToClassMap.put(TYPE_LABOR2009TARIF, Labor2009Tarif.class);
		classToStsMap.put(Labor2009Tarif.class, TYPE_LABOR2009TARIF);
		stsToClassMap.put(TYPE_LABRESULT, LabResult.class);
		classToStsMap.put(LabResult.class, TYPE_LABRESULT);
		
		// uni-directional mappable
		stsToClassMap.put("ch.elexis.artikel_ch.data.Medikament", Artikel.class);
		stsToClassMap.put("ch.elexis.eigenartikel.Eigenartikel", Artikel.class);
		stsToClassMap.put("ch.elexis.artikel_ch.data.Medical", Artikel.class);
		stsToClassMap.put("ch.elexis.artikel_ch.data.MiGelArtikel", Artikel.class);
		stsToClassMap.put(TYPE_KONTAKT, Kontakt.class);
		stsToClassMap.put(TYPE_ORGANISATION, Kontakt.class);
		stsToClassMap.put(TYPE_PATIENT, Kontakt.class);	
		stsToClassMap.put(TYPE_PERSON, Kontakt.class);
		
		// TODO add other values
	}

	// TODO we can not deterministically map person to patient, anwender, mandant as
	// we do not know what was initially intended
	public static String getKeyForObject(AbstractDBObjectIdDeleted obj) {
		if (obj instanceof Kontakt) {
			Kontakt k = (Kontakt) obj;
			if (k.isPerson()) {
				if (k.isPatient()) {
					return TYPE_PATIENT;
				}
				return TYPE_PERSON;
			} else if (k.isOrganisation()) {
				return TYPE_ORGANISATION;
			}

			return TYPE_KONTAKT;
		} else if (obj instanceof Artikel) {
			// TODO
		}
		
		return classToStsMap.get(obj.getClass());
	}

	public static Class<? extends AbstractDBObjectIdDeleted> get(String value) {
		return stsToClassMap.get(value);
	}
}
