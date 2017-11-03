package info.elexis.server.findings.fhir.jpa.codes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.codes.ICodingContribution;
import ch.elexis.core.findings.codes.ILocalCodingContribution;
import info.elexis.server.findings.fhir.jpa.model.annotated.LocalCoding;
import info.elexis.server.findings.fhir.jpa.model.annotated.LocalCoding_;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery.QUERY;
import info.elexis.server.findings.fhir.jpa.model.service.LocalCodingService;

@Component(service = ICodingContribution.class)
public class LocalCodingContribution implements ICodingContribution, ILocalCodingContribution {

	private LocalCodingService localCodingService;

	public LocalCodingContribution() {
		localCodingService = new LocalCodingService();
	}

	@Override
	public void addCoding(ICoding coding) {
		Optional<ICoding> existing = getCode(coding.getCode());
		if (!existing.isPresent()) {
			LocalCodingModelAdapter createdLocalCoding = new LocalCodingModelAdapter(localCodingService.create());
			createdLocalCoding.setCode(coding.getCode());
			createdLocalCoding.setDisplay(coding.getDisplay());
			if (coding instanceof ILocalCoding) {
				createdLocalCoding.setMappedCodes(((ILocalCoding) coding).getMappedCodes());
			}
			localCodingService.write(createdLocalCoding.getModel());
		}
	}

	@Override
	public void removeCoding(ICoding coding) {
		JPAQuery<LocalCoding> query = new JPAQuery<>(LocalCoding.class);
		query.add(LocalCoding_.code, QUERY.EQUALS, coding.getCode());
		List<LocalCoding> codes = query.execute();
		if (!codes.isEmpty()) {
			for (LocalCoding localCoding : codes) {
				localCodingService.delete(localCoding);
			}
		}
	}

	@Override
	public Optional<ICoding> getCode(String code) {
		JPAQuery<LocalCoding> query = new JPAQuery<>(LocalCoding.class);
		query.add(LocalCoding_.code, QUERY.EQUALS, code);
		List<LocalCoding> codes = query.execute();
		if (!codes.isEmpty()) {
			return Optional.of(new LocalCodingModelAdapter(codes.get(0)));
		}
		return Optional.empty();
	}

	@Override
	public String getCodeSystem() {
		return CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem();
	}

	@Override
	public List<ICoding> getCodes() {
		JPAQuery<LocalCoding> query = new JPAQuery<>(LocalCoding.class);
		query.add(LocalCoding_.id, QUERY.NOT_EQUALS, "VERSION");
		List<LocalCoding> codes = query.execute();
		return codes.parallelStream().map(l -> new LocalCodingModelAdapter(l)).collect(Collectors.toList());
	}
}
