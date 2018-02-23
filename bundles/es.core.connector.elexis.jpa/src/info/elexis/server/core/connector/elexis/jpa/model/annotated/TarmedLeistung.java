package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.elexis.core.model.ICodeElement;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.POHelper;

@Entity
@Table(name = "TARMED")
public class TarmedLeistung extends AbstractDBObjectIdDeleted implements ICodeElement {

	public static final String CODESYSTEM_NAME = "Tarmed";

	private static String MANDANT_TYPE_EXTINFO_KEY = "ch.elexis.data.tarmed.mandant.type";
	
	public enum MandantType {
		SPECIALIST, PRACTITIONER
	}

	public static final String EXT_FLD_TP_TL = "TP_TL";
	public static final String EXT_FLD_TP_AL = "TP_AL";
	public static final String EXT_FLD_F_AL_R = "F_AL_R";
	public static final String EXT_FLD_HIERARCHY_SLAVES = "HierarchySlaves";
	public static final String EXT_FLD_SERVICE_GROUPS = "ServiceGroups";
	public static final String EXT_FLD_SERVICE_BLOCKS = "ServiceBlocks";
	public static final String EXT_FLD_SERVICE_AGE = "ServiceAge";

	@Column(length = 32)
	private String parent;

	@Column(length = 5)
	private String digniQuanti;

	@Column(length = 4)
	private String digniQuali;

	@Column(length = 4)
	private String sparte;

	@Column(length = 4)
	private LocalDate gueltigVon;

	@Column(length = 4)
	private LocalDate gueltigBis;

	@Column(length = 25)
	private String nickname;

	@Column(length = 255)
	private String tx255;

	@Column(length = 25, name = "code")
	private String code_;
	
	@Column(length = 3)
	private String law;
	
	@Column(length = 1)
	private boolean isChapter;

	@OneToOne
	@JoinColumn(name = "id", insertable = false, updatable = false)
	private TarmedExtension extension;

	/**
	 * Get the AL value of the {@link TarmedLeistung}.<br>
	 * <b>IMPORTANT:</b> No scaling according to the Dignität of the {@link Mandant}
	 * is performed. Use {@link TarmedLeistung#getAL(Mandant)} for AL value with
	 * scaling included.
	 * 
	 * @return
	 */
	@Transient
	public int getAL() {
		if (extension != null) {
			Object object = extension.getLimits().get(EXT_FLD_TP_AL);
			if (object != null) {
				try {
					double val = Double.parseDouble((String) object);
					return (int) Math.round(val * 100);
				} catch (NumberFormatException nfe) {
					/* ignore */
				}
			}
		}
		return 0;
	}
	
	/**
	 * Get the AL scaling value to be used when billing this {@link TarmedLeistung} for the provided
	 * {@link Mandant}.
	 * 
	 * @param mandant
	 * @return
	 */
	@Transient
	public double getALScaling(Kontakt mandant){
		double scaling = 100;
		if (mandant != null) {
			MandantType type = getMandantType(mandant);
			if (type == MandantType.PRACTITIONER) {
				double alScaling = POHelper.checkZeroDouble(getExtension().getLimits().get(EXT_FLD_F_AL_R));
				if (alScaling > 0.1) {
					scaling *= alScaling;
				}
			}
		}
		return scaling;
	}
	
	/**
	 * Get the {@link MandantType} of the {@link Mandant}. If not found the default value is
	 * {@link MandantType#SPECIALIST}.
	 * 
	 * @param mandant
	 * @return
	 */
	@Transient
	public static MandantType getMandantType(Kontakt mandant){
		Object typeObj = mandant.getExtInfoAsString(MANDANT_TYPE_EXTINFO_KEY);
		if (typeObj instanceof String) {
			return MandantType.valueOf((String) typeObj);
		}
		return MandantType.SPECIALIST;
	}

	@Transient
	public int getTL() {
		if (extension != null) {
			Object object = extension.getLimits().get(EXT_FLD_TP_TL);
			if (object != null) {
				try {
					double val = Double.parseDouble((String) object);
					return (int) Math.round(val * 100);
				} catch (NumberFormatException nfe) {
					/* ignore */
				}
			}
		}
		return 0;
	}

	public boolean requiresSide() {
		if (extension != null) {
			Object object = extension.getLimits().get("SEITE");
			if (object != null && Integer.parseInt((String) object) == 1) {
				return true;
			}
		}
		return false;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getDigniQuanti() {
		return digniQuanti;
	}

	public void setDigniQuanti(String digniQuanti) {
		this.digniQuanti = digniQuanti;
	}

	public String getDigniQuali() {
		return digniQuali;
	}

	public void setDigniQuali(String digniQuali) {
		this.digniQuali = digniQuali;
	}

	public String getSparte() {
		return sparte;
	}

	public void setSparte(String sparte) {
		this.sparte = sparte;
	}

	public LocalDate getGueltigVon() {
		return gueltigVon;
	}

	public void setGueltigVon(LocalDate gueltigVon) {
		this.gueltigVon = gueltigVon;
	}

	public LocalDate getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(LocalDate gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getTx255() {
		return tx255;
	}

	public void setTx255(String tx255) {
		this.tx255 = tx255;
	}

	public void setCode_(String code_) {
		this.code_ = code_;
	}

	public String getCode_() {
		return code_;
	}

	public TarmedExtension getExtension() {
		return extension;
	}

	public void setExtension(TarmedExtension extension) {
		this.extension = extension;
	}

	public String getCode() {
		return (code_ != null) ? code_ : getId();
	}

	@Override
	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	@Override
	public String getText() {
		return getTx255();
	}
	
	public String getLaw() {
		return law;
	}
	
	public void setLaw(String law) {
		this.law = law;
	}

	public boolean isChapter() {
		return isChapter;
	}

	public void setChapter(boolean isChapter) {
		this.isChapter = isChapter;
	}
	
	@Transient
	public List<String> getExtStringListField(String extKey) {
		List<String> ret = new ArrayList<>();
		Map<String, String> map = getExtension().getLimits();
		String values = map.get(extKey);
		if (values != null && !values.isEmpty()) {
			String[] parts = values.split(", ");
			for (String string : parts) {
				ret.add(string);
			}
		}
		return ret;
	}
	
	@Transient
	public String getServiceTyp(){
		return getExtension().getLimits().get("LEISTUNG_TYP");
	}
	
	/**
	 * Get the list of service groups this service is part of.
	 * 
	 * @return
	 */
	@Transient
	public List<String> getServiceGroups(TimeTool date) {
		List<String> ret = new ArrayList<>();
		List<String> groups = getExtStringListField(TarmedLeistung.EXT_FLD_SERVICE_GROUPS);
		if (!groups.isEmpty()) {
			for (String string : groups) {
				int dateStart = string.indexOf('[');
				String datesString = string.substring(dateStart + 1, string.length() - 1);
				String groupString = string.substring(0, dateStart);
				if (isDateWithinDatesString(date, datesString)) {
					ret.add(groupString);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Get the list of service blocks this service is part of.
	 * 
	 * @return
	 */
	@Transient
	public List<String> getServiceBlocks(TimeTool date){
		List<String> ret = new ArrayList<>();
		List<String> blocks = getExtStringListField(TarmedLeistung.EXT_FLD_SERVICE_BLOCKS);
		if (!blocks.isEmpty()) {
			for (String string : blocks) {
				int dateStart = string.indexOf('[');
				String datesString = string.substring(dateStart + 1, string.length() - 1);
				String blockString = string.substring(0, dateStart);
				if (isDateWithinDatesString(date, datesString)) {
					ret.add(blockString);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Get the list of codes of the possible slave services allowed by tarmed.
	 * 
	 * @return
	 */
	public List<String> getHierarchy(TimeTool date){
		List<String> ret = new ArrayList<>();
		List<String> hierarchy = getExtStringListField(TarmedLeistung.EXT_FLD_HIERARCHY_SLAVES);
		if (!hierarchy.isEmpty()) {
			for (String string : hierarchy) {
				int dateStart = string.indexOf('[');
				String datesString = string.substring(dateStart + 1, string.length() - 1);
				String codeString = string.substring(0, dateStart);
				if (isDateWithinDatesString(date, datesString)) {
					ret.add(codeString);
				}
			}
		}
		return ret;
	}
	
	@Transient
	private boolean isDateWithinDatesString(TimeTool date, String datesString) {
		String[] parts = datesString.split("\\|");
		if (parts.length == 2) {
			LocalDate from = LocalDate.parse(parts[0]);
			LocalDate to = LocalDate.parse(parts[1]);
			LocalDate localDate = date.toLocalDate();
			return (from.isBefore(localDate) || from.isEqual(localDate))
					&& (to.isAfter(localDate) || to.isEqual(localDate));
		}
		return false;
	}

}
