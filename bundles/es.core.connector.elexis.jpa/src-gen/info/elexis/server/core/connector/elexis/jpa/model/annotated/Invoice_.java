package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import ch.elexis.core.model.InvoiceState;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Invoice.class)
public class Invoice_ { 

    public static volatile SingularAttribute<Invoice, Kontakt> mandator;
    public static volatile SingularAttribute<Invoice, LocalDate> statusDate;
    public static volatile SingularAttribute<Invoice, String> number;
    public static volatile SingularAttribute<Invoice, LocalDate> invoiceDateTo;
    public static volatile SingularAttribute<Invoice, String> amount;
    public static volatile SingularAttribute<Invoice, Fall> fall;
    public static volatile SingularAttribute<Invoice, InvoiceState> state;
    public static volatile SingularAttribute<Invoice, LocalDate> invoiceDate;
    public static volatile SingularAttribute<Invoice, LocalDate> invoiceDateFrom;

}