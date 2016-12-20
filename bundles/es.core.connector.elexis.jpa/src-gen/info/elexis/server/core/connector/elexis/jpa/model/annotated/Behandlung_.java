package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import ch.rgw.tools.VersionedResource;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ConsultationDiagnosis;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Invoice;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Behandlung.class)
public class Behandlung_ { 

    public static volatile SingularAttribute<Behandlung, LocalDate> datum;
    public static volatile SingularAttribute<Behandlung, Fall> fall;
    public static volatile SingularAttribute<Behandlung, Kontakt> mandant;
    public static volatile SingularAttribute<Behandlung, VersionedResource> eintrag;
    public static volatile SingularAttribute<Behandlung, String> leistungenId;
    public static volatile SingularAttribute<Behandlung, Invoice> invoice;
    public static volatile SetAttribute<Behandlung, ConsultationDiagnosis> diagnoses;

}