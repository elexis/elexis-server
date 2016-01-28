package info.elexis.server.core.connector.elexis.jpa.model.annotated.meta;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlungen;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Faelle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.2.v20111125-r10461", date="2016-01-28T08:14:59")
@StaticMetamodel(Behandlungen.class)
public class Behandlungen_ { 

    public static volatile SingularAttribute<Behandlungen, LocalDate> datum;
    public static volatile SingularAttribute<Behandlungen, String> rechnungsId;
    public static volatile SingularAttribute<Behandlungen, Faelle> fall;
    public static volatile SingularAttribute<Behandlungen, Kontakt> mandant;
    public static volatile SingularAttribute<Behandlungen, byte[]> eintrag;
    public static volatile SingularAttribute<Behandlungen, String> leistungenId;
    public static volatile SingularAttribute<Behandlungen, String> diagnosenId;

}