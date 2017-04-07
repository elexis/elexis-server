package info.elexis.server.core.connector.elexis.billable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet_;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class VerrechnetMatch {

	public String code;
	public int count;
	public boolean deleted = false;

	public VerrechnetMatch(String code, int count) {
		this(code, count, false);
	}

	public VerrechnetMatch(String code, int count, boolean deleted) {
		super();
		this.code = code;
		this.count = count;
		this.deleted = deleted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + count;
		result = prime * result + (deleted ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VerrechnetMatch other = (VerrechnetMatch) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (count != other.count)
			return false;
		if (deleted != other.deleted)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VerrechnetMatch [code=" + code + ", count=" + count + ", deleted=" + deleted + "]";
	}
	
	public static void assertVerrechnetMatch(Behandlung behandlung, List<VerrechnetMatch> matches) {
		JPAQuery<Verrechnet> qre = new JPAQuery<>(Verrechnet.class, true);
		qre.add(Verrechnet_.behandlung, QUERY.EQUALS, behandlung);
		List<Verrechnet> verrechnet = qre.execute();

		List<VerrechnetMatch> existingVerrechnet = verrechnet.stream()
				.map(v -> new VerrechnetMatch(v.getLeistungenCode(), v.getZahl(), v.isDeleted()))
				.collect(Collectors.toList());
		Collection<VerrechnetMatch> disjunction = CollectionUtils.disjunction(existingVerrechnet, matches);
		if (disjunction.size() > 0) {
			throw new AssertionError(disjunction);
		}
	}

}
