package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "laborwerte")
public class LabResult extends AbstractDBObjectIdDeletedExtInfo {

	@OneToOne
	@JoinColumn(name = "PatientID")
	private Kontakt patient;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private LocalDate datum;

	@Column(length = 6)
	private String zeit;

	@OneToOne
	@JoinColumn(name = "ItemID")
	private LabItem item;
	
	@Column(length = 255)
	private String resultat;
	
	@Column(length = 10)
	private String flags;
	
	@Column(length = 30)
	private String origin;
	
	@Lob
	private String comment;
	
	@Column(length = 255)
	private String unit;
	
	@Column(length = 24)
	private String analysetime;
	
	@Column(length = 24)
	private String observationtime;
	
	@Column(length = 24)
	private String transmissiontime;
	
	@Column(length = 255)
	private String refMale;
	
	@Column(length = 255)
	private String refFemale;
	
	@Column(length = 25)
	private String originId;
}
