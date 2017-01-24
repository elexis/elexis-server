package info.elexis.server.findings.fhir.jpa.service;

import java.util.HashMap;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import info.elexis.server.core.service.StoreToStringService;

@Component
public class StoreToStringContribution implements info.elexis.server.core.service.StoreToStringContribution {

	private enum ElexisTypeMap {
		INSTANCE;

		private HashMap<String, Class<? extends IFinding>> typeMap = new HashMap<>();
		private HashMap<Class<? extends IFinding>, String> nameMap = new HashMap<>();
		
		ElexisTypeMap() {
			typeMap.put("ch.elexis.core.findings.fhir.po.model.Encounter", IEncounter.class);
			nameMap.put(IEncounter.class, "ch.elexis.core.findings.fhir.po.model.Encounter");
			typeMap.put("ch.elexis.core.findings.fhir.po.model.Condition", ICondition.class);
			nameMap.put(ICondition.class, "ch.elexis.core.findings.fhir.po.model.Condition");
		}

		public Class<? extends IFinding> getType(String poClassName) {
			return typeMap.get(poClassName);
		}

		public String getName(Class<? extends IFinding> clazz) {
			return nameMap.get(clazz);
		}
	}

	private IFindingsService findingsService;

	@Reference(unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Optional<Object> createFromString(String storeToString) {
		if (storeToString == null) {
			return Optional.empty();
		}

		String[] split = StoreToStringService.splitIntoTypeAndId(storeToString);

		// map string to classname
		String className = split[0];
		String id = split[1];
		Class<? extends IFinding> clazz = ElexisTypeMap.INSTANCE.getType(className);
		if (clazz == null) {
			return Optional.empty();
		}
		return findingsService.findById(id, (Class<? extends IFinding>) clazz).map(f -> Optional.of(f));
	}

	@Override
	public Optional<String> storeToString(Object object) {
		if (object instanceof IFinding) {
			@SuppressWarnings("unchecked")
			String poClassName = ElexisTypeMap.INSTANCE.getName((Class<? extends IFinding>) object.getClass());
			return Optional.ofNullable(poClassName);
		}
		return Optional.empty();
	}
}
