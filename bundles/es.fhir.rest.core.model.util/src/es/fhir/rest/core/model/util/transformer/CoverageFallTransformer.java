package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Coverage;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.transformer.helper.FallHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;

@Component
public class CoverageFallTransformer implements IFhirTransformer<Coverage, Fall> {

	private FallHelper fallHelper = new FallHelper();

	@Override
	public Optional<Coverage> getFhirObject(Fall localObject) {
		Coverage coverage = new Coverage();

		coverage.setId(new IdDt("Coverage", localObject.getId()));
		coverage.addIdentifier(getElexisObjectIdentifier(localObject));

		coverage.setBin(fallHelper.getBin(localObject));
		coverage.setBeneficiary(fallHelper.getBeneficiaryReference(localObject));
		coverage.setIssuer(fallHelper.getIssuerReference(localObject));
		coverage.setPeriod(fallHelper.getPeriod(localObject));

		return Optional.of(coverage);
	}

	@Override
	public Optional<Fall> getLocalObject(Coverage fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Fall> updateLocalObject(Coverage fhirObject, Fall localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Fall> createLocalObject(Coverage fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Coverage.class.equals(fhirClazz) && Fall.class.equals(localClazz);
	}

}
