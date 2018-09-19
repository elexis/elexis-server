package info.elexis.server.findings.fhir.jpa.codes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.codes.ICodingContribution;
import ch.elexis.core.findings.codes.ILocalCodingContribution;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery.QUERY;
import info.elexis.server.findings.fhir.jpa.model.service.LocalCodingService;

@Component(service = ICodingContribution.class)
public class LocalCodingContribution implements ICodingContribution, ILocalCodingContribution {

	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private IModelService modelService;
	
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
		IQuery<ILocalCoding> query = modelService.getQuery(ILocalCoding.class);
		query.and(LocalCoding_.code, QUERY.EQUALS, coding.getCode());
		List<ILocalCoding> codes = query.execute();
		if (!codes.isEmpty()) {
			for (ILocalCoding localCoding : codes) {
				modelService.delete(localCoding);
			}
		}
	}

	@Override
	public Optional<ICoding> getCode(String code) {
		JPAQuery<ILocalCoding> query = modelService.getQuery(ILocalCoding.class);
		query.add(LocalCoding_.code, QUERY.EQUALS, code);
		List<ILocalCoding> codes = query.execute();
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
		JPAQuery<ILocalCoding> query = modelService.getQuery(ILocalCoding.class);
		query.add(LocalCoding_.id, QUERY.NOT_EQUALS, "VERSION");
		List<ILocalCoding> codes = query.execute();
		return codes.parallelStream().map(l -> new LocalCodingModelAdapter(l)).collect(Collectors.toList());
	}
}
