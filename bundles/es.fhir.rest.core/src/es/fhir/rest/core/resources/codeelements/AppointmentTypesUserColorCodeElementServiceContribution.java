package es.fhir.rest.core.resources.codeelements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

@Component
public class AppointmentTypesUserColorCodeElementServiceContribution implements ICodeElementServiceContribution {

	@Reference
	private IContextService contextService;

	@Reference
	private IConfigService configService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Override
	public String getSystem() {
		return "appointment-type-configured-color";
	}

	@Override
	public CodeElementTyp getTyp() {
		return CodeElementTyp.USERCONFIG;
	}

	@Override
	public Optional<ICodeElement> loadFromCode(String code, Map<Object, Object> context) {
		return null;
	}

	private final String PREFIX = "agenda/farben/typ/";

	@Override
	public List<ICodeElement> getElements(Map<Object, Object> context) {

		ArrayList<ICodeElement> elements = new ArrayList<ICodeElement>();

		Optional<IContact> activeUserContact = contextService.getActiveUserContact();
		if (activeUserContact.isPresent()) {
			IQuery<IUserConfig> query = modelService.getQuery(IUserConfig.class);
			query.and(ModelPackage.Literals.IUSER_CONFIG__OWNER, COMPARATOR.EQUALS, activeUserContact.get().getId());
			query.and("param", COMPARATOR.LIKE, PREFIX + "%");
			List<IUserConfig> result = query.execute();
			result.forEach(res -> {
				String code = res.getKey().toString().substring(PREFIX.length()).replaceAll(" ", "_").toUpperCase();
				elements.add(new TransientCodeElement(getSystem(), code, "#" + res.getValue()));
			});
		}

		return elements;
	}

}
