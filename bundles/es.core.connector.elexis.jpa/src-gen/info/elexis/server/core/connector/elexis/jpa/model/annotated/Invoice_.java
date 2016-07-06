package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-07-06T10:55:30")
@StaticMetamodel(Invoice.class)
public class Invoice_ { 

    public static volatile SingularAttribute<Invoice, Kontakt> mandator;
    public static volatile SingularAttribute<Invoice, LocalDate> statusDate;
    public static volatile SingularAttribute<Invoice, String> number;
    public static volatile SingularAttribute<Invoice, LocalDate> invoiceDateTo;
    public static volatile SingularAttribute<Invoice, String> amount;
    public static volatile SingularAttribute<Invoice, Fall> fall;
    public static volatile SingularAttribute<Invoice, LocalDate> invoiceDate;
    public static volatile SingularAttribute<Invoice, LocalDate> invoiceDateFrom;
    public static volatile SingularAttribute<Invoice, String> status;

}