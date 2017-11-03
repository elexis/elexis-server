package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(ClinicalImpression.class)
public class ClinicalImpression_ extends AbstractDBObjectIdDeleted_ {

    public static volatile SingularAttribute<ClinicalImpression, String> patientid;
    public static volatile SingularAttribute<ClinicalImpression, String> encounterid;
    public static volatile SingularAttribute<ClinicalImpression, String> content;

}