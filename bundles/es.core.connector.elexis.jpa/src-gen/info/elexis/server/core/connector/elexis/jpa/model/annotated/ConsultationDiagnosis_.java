package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Diagnosis;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-04-14T14:24:49")
@StaticMetamodel(ConsultationDiagnosis.class)
public class ConsultationDiagnosis_ { 

    public static volatile SingularAttribute<ConsultationDiagnosis, Diagnosis> diagnosis;
    public static volatile SingularAttribute<ConsultationDiagnosis, Behandlung> consultation;

}