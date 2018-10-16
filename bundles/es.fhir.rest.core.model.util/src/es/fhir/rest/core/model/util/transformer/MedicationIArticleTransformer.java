package es.fhir.rest.core.model.util.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Medication.MedicationPackageComponent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import at.medevit.ch.artikelstamm.IArtikelstammItem;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.ArticleTyp;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.model.util.transformer.helper.IMedicationHelper;

@Component
public class MedicationIArticleTransformer implements IFhirTransformer<Medication, IArticle> {
	
	@org.osgi.service.component.annotations.Reference(policy = ReferencePolicy.STATIC)
	private IFhirTransformerRegistry transformerRegistry;
	
	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
		+ "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=at.medevit.ch.artikelstamm.model)")
	private IModelService artikelstammModelService;
	
	private IMedicationHelper medicationHelper;
	
	@Activate
	private void activate(){
		medicationHelper = new IMedicationHelper();
	}
	
	@Override
	public Optional<Medication> getFhirObject(IArticle localObject, Set<Include> includes){
		Medication medication = new Medication();
		medication.setId(new IdDt("Medication", localObject.getTyp() + "." + localObject.getId()));
		
		CodeableConcept code = new CodeableConcept();
		
		code.addCoding(medicationHelper.getNameCoding(localObject.getName()));
		
		code.addCoding(medicationHelper.getTypeCoding(localObject));
		
		code.addCoding(medicationHelper.getGtinCoding(localObject.getGtin()));
		
		List<Coding> atcCodings = medicationHelper.getAtcCodings(localObject.getAtcCode());
		for (Coding atcCoding : atcCodings) {
			code.addCoding(atcCoding);
		}
		
		medication.setCode(code);
		
		if (!localObject.isProduct()) {
			medication.setPackage(new MedicationPackageComponent()
				.setContent(medicationHelper.getMedicationPackageContent(localObject)));
		}
		
		return Optional.of(medication);
	}
	
	@Override
	public Optional<IArticle> getLocalObject(Medication fhirObject){
		String id = fhirObject.getIdElement().getIdPart();
		if (StringUtils.isNotEmpty(id)) {
			String realId = id.substring(id.indexOf('.') + 1);
			if (id.startsWith(ArticleTyp.ARTIKELSTAMM.getCodeSystemName())) {
				return artikelstammModelService.load(realId, IArtikelstammItem.class)
					.map(IArticle.class::cast);
			} else {
				return coreModelService.load(realId, IArticle.class);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<IArticle> updateLocalObject(Medication fhirObject, IArticle localObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<IArticle> createLocalObject(Medication fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Medication.class.equals(fhirClazz) && IArticle.class.equals(localClazz);
	}
	
}
