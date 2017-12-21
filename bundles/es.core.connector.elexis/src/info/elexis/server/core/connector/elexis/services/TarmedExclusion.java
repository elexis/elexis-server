package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.tarmed.TarmedKumulationType;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedGroup;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedKumulation;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;

public class TarmedExclusion {

	private String slaveCode;
	private TarmedKumulationType slaveType;

	public TarmedExclusion(TarmedKumulation kumulation) {
		slaveCode = kumulation.getSlaveCode();
		slaveType = TarmedKumulationType.ofArt(kumulation.getSlaveArt());
	}

	public boolean isMatching(TarmedLeistung tarmedLeistung, TimeTool date) {
		if (slaveType == TarmedKumulationType.CHAPTER) {
			return isMatchingChapter(tarmedLeistung);
		} else if (slaveType == TarmedKumulationType.SERVICE) {
			return slaveCode.equals(tarmedLeistung.getCode());
		} else if (slaveType == TarmedKumulationType.GROUP) {
			List<String> groups = tarmedLeistung.getServiceGroups(date);
			return groups.contains(slaveCode);
		}
		return false;
	}

	private boolean isMatchingChapter(TarmedLeistung tarmedLeistung) {
		if (slaveCode.equals(tarmedLeistung.getCode())) {
			return true;
		} else {
			String parentId = tarmedLeistung.getParent();
			if (parentId != null && !parentId.equals("NIL")) {
				Optional<TarmedLeistung> parent = TarmedLeistungService.load(parentId);
				if (parent.isPresent()) {
					return isMatchingChapter(parent.get());
				} else {
					LoggerFactory.getLogger(TarmedExclusion.class)
							.error("Parent [{}] for TarmedLeistung [{}] not resolvable, returning false.", parentId, tarmedLeistung);
				}
			}
			return false;
		}
	}

	public boolean isMatching(TarmedGroup tarmedGroup) {
		if (slaveType != TarmedKumulationType.GROUP) {
			return false;
		}
		return slaveCode.equals(tarmedGroup.getGroupName());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(TarmedKumulationType.toString(slaveType)).append(" ").append(slaveCode);
		return sb.toString();
	}
}
