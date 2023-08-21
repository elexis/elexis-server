package es.fhir.rest.core.resources.codeelements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.services.IDocumentStore;

@Component
public class LetterDocumentCategoriesCodeElementServiceContribution implements ICodeElementServiceContribution {

	@Reference(target = "(storeid=ch.elexis.data.store.brief)")
	private IDocumentStore letterDocumentStore;

	@Override
	public String getSystem() {
		return "letter-categories";
	}

	@Override
	public CodeElementTyp getTyp() {
		return CodeElementTyp.CONFIG;
	}

	@Override
	public Optional<ICodeElement> loadFromCode(String code, Map<Object, Object> context) {
		return null;
	}

	@Override
	public List<ICodeElement> getElements(Map<Object, Object> context) {
		ArrayList<ICodeElement> elements = new ArrayList<ICodeElement>();

		List<ICategory> categories = letterDocumentStore.getCategories();
		categories.forEach(category -> elements.add(new TransientCodeElement(getSystem(),
				category.getName().replaceAll(" ", "_").toUpperCase(), category.getName())));

		return elements;
	}

}
