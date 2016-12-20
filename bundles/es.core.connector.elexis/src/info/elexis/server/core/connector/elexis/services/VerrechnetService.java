package info.elexis.server.core.connector.elexis.services;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.article.IArticle;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarArtikel;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarArtikelstammItem;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarEigenleistung;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarLabor2009Tarif;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.billable.adjuster.VatVerrechnetAdjuster;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Eigenleistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.VKPreis;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.VKPreis_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class VerrechnetService extends AbstractService<Verrechnet> {

	private static Logger log = LoggerFactory.getLogger(VerrechnetService.class);

	public static VerrechnetService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final VerrechnetService INSTANCE = new VerrechnetService();
	}

	private VerrechnetService() {
		super(Verrechnet.class);
	}

	private IBillable<? extends AbstractDBObjectIdDeleted> createVerrechenbarForObject(
			AbstractDBObjectIdDeleted object) {
		if (object instanceof TarmedLeistung) {
			return new VerrechenbarTarmedLeistung((TarmedLeistung) object);
		} else if (object instanceof Labor2009Tarif) {
			return new VerrechenbarLabor2009Tarif((Labor2009Tarif) object);
		} else if (object instanceof Eigenleistung) {
			return new VerrechenbarEigenleistung((Eigenleistung) object);
		} else if (object instanceof ArtikelstammItem) {
			return new VerrechenbarArtikelstammItem((ArtikelstammItem) object);
		} else if (object instanceof Artikel) {
			return new VerrechenbarArtikel((Artikel) object);
		}

		log.warn("Unsupported object for create verrechenbar {}", object.getClass().getName());
		return null;
	}

	public Verrechnet create(IBillable iv, Behandlung kons, int count, Kontakt userContact) {
		em.getTransaction().begin();

		Verrechnet v = create(false);
		em.merge(kons);
		v.setLeistungenText(iv.getText());
		String keyForObject = ElexisTypeMap.getKeyForObject((AbstractDBObjectIdDeleted) iv.getEntity());
		v.setKlasse(keyForObject);
		v.setLeistungenCode(iv.getId());
		v.setLeistungenText(iv.getText());
		v.setBehandlung(kons);
		v.setZahl(count);
		v.setUser(userContact);

		TimeTool dat = new TimeTool(kons.getDatum());
		Fall fall = kons.getFall();
		int tp = iv.getTP(dat, fall);
		double factor = iv.getFactor(dat, fall);
		long preis = Math.round(tp * factor);

		v.setEk_kosten(Integer.parseInt(iv.getCost(dat).getCentsAsString()));
		v.setVk_tp(tp);
		v.setVk_scale(Double.toString(factor));
		v.setVk_preis((int) preis);
		v.setScale(100);
		v.setScale2(100);

		em.getTransaction().commit();

		if (iv instanceof VerrechenbarArtikelstammItem || iv instanceof VerrechenbarArtikel) {
			StockService.INSTANCE.performSingleDisposal((IArticle) iv.getEntity(), count, null);
		}

		// call the adjusters
		new VatVerrechnetAdjuster().adjust(v);

		return v;
	}

	public Optional<IBillable> getVerrechenbar(Verrechnet vr) {
		String klasse = vr.getKlasse();
		String leistungenCode = vr.getLeistungenCode();

		Optional<AbstractDBObjectIdDeleted> object = StoreToStringService.INSTANCE
				.createDetachedFromString(klasse + StringConstants.DOUBLECOLON + leistungenCode);
		if (object.isPresent()) {
			return Optional.ofNullable(createVerrechenbarForObject(object.get()));
		}
		return Optional.empty();
	}

	public double getVKMultiplikator(TimeTool date, String billingSystem) {
		JPAQuery<VKPreis> vkPreise = new JPAQuery<VKPreis>(VKPreis.class);
		vkPreise.add(VKPreis_.typ, JPAQuery.QUERY.EQUALS, billingSystem);
		List<VKPreis> list = vkPreise.execute();

		Iterator<VKPreis> iter = list.iterator();
		while (iter.hasNext()) {
			VKPreis info = iter.next();
			TimeTool fromDate = new TimeTool(info.getDatum_von());
			TimeTool toDate = new TimeTool(info.getDatum_bis());
			if (date.isAfterOrEqual(fromDate) && date.isBeforeOrEqual(toDate)) {
				String value = info.getMultiplikator();
				if (value != null && !value.isEmpty()) {
					try {
						return Double.parseDouble(value);
					} catch (NumberFormatException nfe) {
						log.error("Exception handling multiplikator value " + value);
						return 0.0;
					}
				}
			}
		}
		return 1.0;
	}

	public static IStatus changeCountValidated(Verrechnet vr, int newCount, Kontakt mandatorContact) {
		int previous = vr.getZahl();
		if (newCount == previous) {
			return Status.OK_STATUS;
		}

		int difference = newCount - previous;
		Optional<IBillable> verrechenbar = VerrechnetService.INSTANCE.getVerrechenbar(vr);
		if (difference > 0) {
			for (int i = 0; i < difference; i++) {
				IStatus ret = verrechenbar.get().add(vr.getBehandlung(), vr.getUser(), mandatorContact);
				if (!ret.isOK()) {
					return ret;
				}
			}
		} else {
			int abs = Math.abs(difference);
			for (int i = 0; i < abs; i++) {
				IStatus ret = verrechenbar.get().removeFromConsultation(vr, mandatorContact);
				if (!ret.isOK()) {
					return ret;
				}
			}
		}
		return Status.OK_STATUS;
	}
	
	public static List<Verrechnet> getAllVerrechnetForBehandlung(Behandlung behandlung) {
		JPAQuery<Verrechnet> qre = new JPAQuery<Verrechnet>(Verrechnet.class);
		qre.add(Verrechnet_.behandlung, QUERY.EQUALS, behandlung);
		return qre.execute();
	}
}
