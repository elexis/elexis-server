package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(ProcedureRequest.class)
public class ProcedureRequest_ extends AbstractDBObjectIdDeleted_ {

    public static volatile SingularAttribute<ProcedureRequest, String> patientid;
    public static volatile SingularAttribute<ProcedureRequest, String> encounterid;
    public static volatile SingularAttribute<ProcedureRequest, String> content;

}