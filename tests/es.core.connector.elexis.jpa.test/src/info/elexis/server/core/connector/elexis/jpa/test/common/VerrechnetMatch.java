package info.elexis.server.core.connector.elexis.jpa.test.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet_;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class VerrechnetMatch {

	public final String code;
	public final int count;
	public final int scale1;
	public final int scale2;
	public final boolean deleted;
	public Integer vk_tp;
	public Integer vk_preis;

	public VerrechnetMatch(String code, int count) {
		this(code, count, false);
	}

	public VerrechnetMatch(String code, int count, boolean deleted) {
		this(code, count, 100, 100, deleted);
	}

	public VerrechnetMatch(String code, int count, int scale1, int scale2, boolean deleted) {
		this(code, count, scale1, scale2, null, null, deleted);
	}

	/**
	 * 
	 * @param code
	 * @param count
	 * @param scale1
	 * @param scale2
	 * @param vk_tp if <code>null</code> do not match the price, else price matching will be performed
	 * @param vk_preis if <code>null</code> do not match the price, else price matching will be performed
	 * @param deleted
	 */
	public VerrechnetMatch(String code, int count, int scale1, int scale2, Integer vk_tp, Integer vk_preis,
			boolean deleted) {
		super();
		this.code = code;
		this.scale1 = scale1;
		this.scale2 = scale2;
		this.count = count;
		this.vk_tp = vk_tp;
		this.vk_preis = vk_preis;
		this.deleted = deleted;
	}

	public String getCode() {
		return code;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + count;
		result = prime * result + (deleted ? 1231 : 1237);
		result = prime * result + scale1;
		result = prime * result + scale2;
		result = prime * result + ((vk_preis == null) ? 0 : vk_preis.hashCode());
		result = prime * result + ((vk_tp == null) ? 0 : vk_tp.hashCode());
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
		if (scale1 != other.scale1)
			return false;
		if (scale2 != other.scale2)
			return false;
		if (vk_preis == null) {
			if (other.vk_preis != null)
				return false;
		} else if (!vk_preis.equals(other.vk_preis))
			return false;
		if (vk_tp == null) {
			if (other.vk_tp != null)
				return false;
		} else if (!vk_tp.equals(other.vk_tp))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VerrechnetMatch [code=" + code + ", count=" + count + ", scale1=" + scale1 + ", scale2=" + scale2
				+ ", deleted=" + deleted + ", vk_tp=" + vk_tp + ", vk_preis=" + vk_preis + "]\n";
	}

	public static void assertVerrechnetMatch(Behandlung behandlung, List<VerrechnetMatch> matches) {
		JPAQuery<Verrechnet> qre = new JPAQuery<>(Verrechnet.class, true);
		qre.add(Verrechnet_.behandlung, QUERY.EQUALS, behandlung);
		List<Verrechnet> verrechnet = qre.execute();

		List<VerrechnetMatch> existingVerrechnet = verrechnet.stream()
				.map(v -> new VerrechnetMatch(v.getLeistungenCode(), v.getZahl(), v.getScale(), v.getScale2(),
						v.getVk_tp(), v.getVk_preis(), v.isDeleted()))
				.collect(Collectors.toList());
		
		Set<String> dontMatchPrice = new HashSet<>();
		for (VerrechnetMatch vm : matches) {
			if(vm.vk_tp == null || vm.vk_preis == null) {
				dontMatchPrice.add(vm.code);
			}
		}
		
		if(dontMatchPrice.size()>0) {
			for (VerrechnetMatch vm : existingVerrechnet) {
				if(dontMatchPrice.contains(vm.code)) {
					vm.vk_preis = null;
					vm.vk_tp = null;
				}
			}
		}
		
		Collection<VerrechnetMatch> disjunction = CollectionUtils.disjunction(existingVerrechnet, matches);
		if (disjunction.size() > 0) {
			throw new AssertionError(disjunction);
		}
	}

}
