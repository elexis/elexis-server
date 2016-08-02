package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Diagnosis;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(ConsultationDiagnosis.class)
public class ConsultationDiagnosis_ { 

    public static volatile SingularAttribute<ConsultationDiagnosis, Diagnosis> diagnosis;
    public static volatile SingularAttribute<ConsultationDiagnosis, Behandlung> consultation;

}