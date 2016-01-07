package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "kontakt_adress_joint")
@NamedQueries({
		@NamedQuery(name = KontaktAdressJoint.QUERY_findAllIDisMyKontakt, query = "SELECT e FROM KontaktAdressJoint e WHERE e.myKontakt = :id"),
		@NamedQuery(name = KontaktAdressJoint.QUERY_findAllIDisOtherKontakt, query = "SELECT e FROM KontaktAdressJoint e WHERE e.otherKontakt = :id") })
public class KontaktAdressJoint extends AbstractDBObject {

	public static final String QUERY_findAllIDisMyKontakt = "QUERY_findAllIDisMyKontakt";
	public static final String QUERY_findAllIDisOtherKontakt = "QUERY_findAllIDisOtherKontakt";

	@Column(length = 80)
	private String bezug;

	@OneToOne
	@JoinColumn(name = "myID")
	private Kontakt myKontakt;

	@OneToOne
	@JoinColumn(name = "otherID")
	private Kontakt otherKontakt;

	@Column
	private int myRType;

	@Column
	private int otherRType;

	public Kontakt getMyKontakt() {
		return myKontakt;
	}

	public void setMyKontakt(Kontakt myKontakt) {
		this.myKontakt = myKontakt;
	}

	public Kontakt getOtherKontakt() {
		return otherKontakt;
	}

	public void setOtherKontakt(Kontakt otherKontakt) {
		this.otherKontakt = otherKontakt;
	}

	public String getBezug() {
		return this.bezug;
	}

	public void setBezug(String bezug) {
		this.bezug = bezug;
	}

	public int getMyRType() {
		return myRType;
	}

	public void setMyRType(int myRType) {
		this.myRType = myRType;
	}

	public int getOtherRType() {
		return otherRType;
	}

	public void setOtherRType(int otherRType) {
		this.otherRType = otherRType;
	}
}
