package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.StickerClassLink;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StickerObjectLink;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Sticker.class)
public class Sticker_ { 

    public static volatile SingularAttribute<Sticker, String> image;
    public static volatile SetAttribute<Sticker, StickerObjectLink> stickerObjectLinks;
    public static volatile SingularAttribute<Sticker, String> importance;
    public static volatile SingularAttribute<Sticker, String> background;
    public static volatile SingularAttribute<Sticker, String> classes;
    public static volatile SingularAttribute<Sticker, String> name;
    public static volatile SingularAttribute<Sticker, String> foreground;
    public static volatile SetAttribute<Sticker, StickerClassLink> stickerClassLinks;

}