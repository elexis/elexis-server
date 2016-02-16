/*******************************************************************************
 * Copyright (c) 2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.FuzzyCountryToEnumConverter;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.FuzzyGenderToEnumConverter;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.Country;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.Gender;

/**
 * The persistent class for the Elexis KONTAKT database table. Valid from DB
 * Version 3.1.0
 * 
 * @author M. Descher, MEDEVIT, Austria
 */
@Entity
@Table(name = "KONTAKT")
@XmlRootElement(name = "contact")
@Cache(type = CacheType.NONE)
public class Kontakt extends AbstractDBObjectIdDeletedExtInfo implements Serializable {
	protected static final long serialVersionUID = 1L;

	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected String allergien;

	@Lob()
	protected String anschrift;

	@Lob()
	protected String bemerkung;

	@Column(length = 255)
	protected String bezeichnung1;

	@Column(length = 255)
	protected String bezeichnung2;

	/**
	 * Contains the following values in the respective instantiations of contact
	 * isIstPatient(): ? isIstPerson(): if medic: area of expertise
	 * isIstMandant(): username/mandant short name isIstAnwender():
	 * username/mandant short name isIstOrganisation(): contact person
	 * isIstLabor(): ?
	 */
	@Column(length = 255)
	protected String bezeichnung3;

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisDBCompressedStringConverter")
	protected String diagnosen;

	@Column(length = 255)
	protected String email;

	// @Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisDBCompressedStringConverter")
	protected String famAnamnese;

	@Column(length = 30)
	protected String fax;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	protected LocalDate geburtsdatum;

	@Converter(name = "FuzzyGenderToEnumConverter", converterClass = FuzzyGenderToEnumConverter.class)
	@Convert("FuzzyGenderToEnumConverter")
	protected Gender geschlecht;

	@Column(length = 10)
	protected String gruppe;

	@Convert("booleanStringConverter")
	protected boolean istPerson;

	@Convert("booleanStringConverter")
	protected boolean istPatient;

	@Convert("booleanStringConverter")
	protected boolean istAnwender;

	@Convert("booleanStringConverter")
	protected boolean istMandant;

	@Convert("booleanStringConverter")
	protected boolean istOrganisation;

	@Convert("booleanStringConverter")
	protected boolean istLabor;

	@Column(length = 3)
	@Converter(name = "FuzzyCountryToEnumConverter", converterClass = FuzzyCountryToEnumConverter.class)
	@Convert(value = "FuzzyCountryToEnumConverter")
	protected Country land;

	@Column(length = 30)
	protected String natelNr;

	@Column(length = 255)
	protected String ort;

	/**
	 * Contains according to contact-type manifestation:<br>
	 * isPatient: patientNr<br>
	 * isOrganization /<br>
	 * isPerson: ID
	 */
	@Column(length = 40)
	protected String patientNr;

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisDBCompressedStringConverter")
	protected String persAnamnese;

	@Column(length = 6)
	protected String plz;

	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected String risiken;

	@Column(length = 255)
	protected String strasse;

	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected byte[] sysAnamnese;

	@Column(length = 30)
	protected String telefon1;

	@Column(length = 30)
	protected String telefon2;

	@Column(length = 255)
	protected String titel;

	@Column(length = 255)
	protected String titelSuffix;

	@Column(length = 255)
	protected String website;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "object", insertable = false)
	@MapKey(name = "domain")
	protected Map<String, Xid> xids;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "patientID", insertable = false)
	protected List<Fall> faelle;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "UserID", insertable = false)
	protected List<Userconfig> userconfig;

	// ---------------------------------------------
	public Kontakt() {
	}

	public String getAllergien() {
		return allergien;
	}

	public void setAllergien(String allergien) {
		this.allergien = allergien;
	}

	public String getAnschrift() {
		return anschrift;
	}

	public void setAnschrift(String anschrift) {
		this.anschrift = anschrift;
	}

	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(String bemerkung) {
		this.bemerkung = bemerkung;
	}

	public String getBezeichnung1() {
		return bezeichnung1;
	}

	public void setBezeichnung1(String bezeichnung1) {
		this.bezeichnung1 = bezeichnung1;
	}

	public String getBezeichnung2() {
		return bezeichnung2;
	}

	public void setBezeichnung2(String bezeichnung2) {
		this.bezeichnung2 = bezeichnung2;
	}

	public String getBezeichnung3() {
		return bezeichnung3;
	}

	public void setBezeichnung3(String bezeichnung3) {
		this.bezeichnung3 = bezeichnung3;
	}

	public String getDiagnosen() {
		return diagnosen;
	}

	public void setDiagnosen(String diagnosen) {
		this.diagnosen = diagnosen;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFamAnamnese() {
		return famAnamnese;
	}

	public void setFamAnamnese(String famAnamnese) {
		this.famAnamnese = famAnamnese;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	/**
	 * @return date if value is set, else <code>null</code>
	 */
	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	public Gender getGeschlecht() {
		return geschlecht;
	}

	public void setGeschlecht(Gender geschlecht) {
		this.geschlecht = geschlecht;
	}

	public String getGruppe() {
		return gruppe;
	}

	public void setGruppe(String gruppe) {
		this.gruppe = gruppe;
	}

	public boolean isIstAnwender() {
		return istAnwender;
	}

	public void setIstAnwender(boolean istAnwender) {
		this.istAnwender = istAnwender;
	}

	public boolean isIstLabor() {
		return istLabor;
	}

	public void setIstLabor(boolean istLabor) {
		this.istLabor = istLabor;
	}

	public boolean isIstMandant() {
		return istMandant;
	}

	public void setIstMandant(boolean istMandant) {
		this.istMandant = istMandant;
	}

	public boolean isIstPatient() {
		return istPatient;
	}

	public void setIstPatient(boolean istPatient) {
		this.istPatient = istPatient;
	}

	public Country getLand() {
		return land;
	}

	public void setLand(Country land) {
		this.land = land;
	}

	public String getNatelNr() {
		return natelNr;
	}

	public void setNatelNr(String natelNr) {
		this.natelNr = natelNr;
	}

	public String getOrt() {
		return ort;
	}

	public void setOrt(String ort) {
		this.ort = ort;
	}

	public String getPatientNr() {
		return patientNr;
	}

	public void setPatientNr(String patientNr) {
		this.patientNr = patientNr;
	}

	public String getPersAnamnese() {
		return persAnamnese;
	}

	public void setPersAnamnese(String persAnamnese) {
		this.persAnamnese = persAnamnese;
	}

	public String getPlz() {
		return plz;
	}

	public void setPlz(String plz) {
		this.plz = plz;
	}

	public String getRisiken() {
		return risiken;
	}

	public void setRisiken(String risiken) {
		this.risiken = risiken;
	}

	public String getStrasse() {
		return strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}

	public byte[] getSysAnamnese() {
		return sysAnamnese;
	}

	public void setSysAnamnese(byte[] sysAnamnese) {
		this.sysAnamnese = sysAnamnese;
	}

	public String getTelefon1() {
		return telefon1;
	}

	public void setTelefon1(String telefon1) {
		this.telefon1 = telefon1;
	}

	public String getTelefon2() {
		return telefon2;
	}

	public void setTelefon2(String telefon2) {
		this.telefon2 = telefon2;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getTitelSuffix() {
		return titelSuffix;
	}

	public void setTitelSuffix(String titelSuffix) {
		this.titelSuffix = titelSuffix;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public boolean isIstPerson() {
		return istPerson;
	}

	public void setIstPerson(boolean istPerson) {
		this.istPerson = istPerson;
	}

	public boolean isIstOrganisation() {
		return istOrganisation;
	}

	public void setIstOrganisation(boolean istOrganisation) {
		this.istOrganisation = istOrganisation;
	}

	public Map<String, Xid> getXids() {
		return xids;
	}

	public void setXids(Map<String, Xid> xids) {
		this.xids = xids;
	}

	public List<Fall> getFaelle() {
		return faelle;
	}

	public void setFaelle(List<Fall> faelle) {
		this.faelle = faelle;
	}

	public List<Userconfig> getUserconfig() {
		return userconfig;
	}

	public void setUserconfig(List<Userconfig> userconfig) {
		this.userconfig = userconfig;
	}
}
