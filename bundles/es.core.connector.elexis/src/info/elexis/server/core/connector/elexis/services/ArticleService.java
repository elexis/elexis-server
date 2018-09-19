package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.services.IArticleService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;

@Component
public class ArticleService implements IArticleService {

	@Override
	public Optional<? extends IArticle> findAnyByGTIN(String gtin) {
		Optional<ArtikelstammItem> artikelstammByGTIN = ArtikelstammItemService.findByGTIN(gtin);
		if (artikelstammByGTIN.isPresent()) {
			return artikelstammByGTIN;
		}
		return ArtikelService.findByGTIN(gtin);
	}

}
