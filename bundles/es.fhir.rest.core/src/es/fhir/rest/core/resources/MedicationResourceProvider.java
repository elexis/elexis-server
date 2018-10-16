package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import at.medevit.ch.artikelstamm.IArtikelstammItem;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.ArticleTyp;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.coding.MedicamentCoding;

@Component
public class MedicationResourceProvider implements IFhirResourceProvider {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=at.medevit.ch.artikelstamm.model)")
	private IModelService artikelstammModelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Medication.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Medication, IArticle> getTransformer(){
		return (IFhirTransformer<Medication, IArticle>) transformerRegistry
			.getTransformerFor(Medication.class, IArticle.class);
	}
	
	@Read
	public Medication getResourceById(@IdParam IdType theId){
		String idPart = theId.getIdPart();
		
		if (StringUtils.isNotEmpty(idPart)) {
			
			Optional<IArticle> article = Optional.empty();
			
			String realId = idPart.substring(idPart.indexOf('.') + 1);
			if (idPart.startsWith(ArticleTyp.ARTIKELSTAMM.getCodeSystemName())) {
				article = artikelstammModelService.load(realId, IArtikelstammItem.class)
					.map(IArticle.class::cast);
			} else {
				article = coreModelService.load(realId, IArticle.class);
			}
			
			if (article.isPresent()) {
				Optional<Medication> fhirMedication = getTransformer().getFhirObject(article.get());
				return fhirMedication.get();
			}
		}
		
		return null;
	}
	
	@Search()
	public List<Medication> findMedications(
		@RequiredParam(name = Medication.SP_CODE) TokenParam code){
	
		IQuery<IArticle> coreQuery = coreModelService.getQuery(IArticle.class);
		IQuery<IArtikelstammItem> artikelstammQuery =
			artikelstammModelService.getQuery(IArtikelstammItem.class);
		
		if (MedicamentCoding.GTIN.getUrl().equals(code.getSystem())) {
			coreQuery.and(ModelPackage.Literals.IARTICLE__GTIN, COMPARATOR.EQUALS, code.getValue(),
				true);
			artikelstammQuery.and("gtin", COMPARATOR.EQUALS, code.getValue(), true);
		} else if (MedicamentCoding.NAME.getUrl().equals(code.getSystem())) {
			coreQuery.and(ModelPackage.Literals.IARTICLE__NAME, COMPARATOR.LIKE,
				"%" + code.getValue() + "%", true);
			artikelstammQuery.and("dscr", COMPARATOR.LIKE, "%" + code.getValue() + "%", true);
		} else {
			return Collections.emptyList();
		}
		
		List<IArticle> results = new ArrayList<>();
		List<IArticle> coreResult = coreQuery.execute();
		results.addAll(coreResult);
		List<IArtikelstammItem> artikelstammResult = artikelstammQuery.execute();
		results.addAll(artikelstammResult);
		
		return results.parallelStream().map(a -> getTransformer().getFhirObject(a).get())
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
}
