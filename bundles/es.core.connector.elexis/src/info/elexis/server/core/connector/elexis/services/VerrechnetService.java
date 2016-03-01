package info.elexis.server.core.connector.elexis.services;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.IVerrechenbar;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarLabor2009Tarif;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.VKPreis;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.VKPreis_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;

public class VerrechnetService extends AbstractService<Verrechnet> {

	private static Logger log = LoggerFactory.getLogger(VerrechnetService.class);

	public static VerrechnetService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final VerrechnetService INSTANCE = new VerrechnetService();
	}

	private VerrechnetService() {
		super(Verrechnet.class);
	}

	@Deprecated
	public Verrechnet create(Behandlung kons, Kontakt userKontakt) {
		Verrechnet v = create(false);
		v.setBehandlung(kons);
		v.setUser(userKontakt);
		flush();
		return v;
	}

	private IVerrechenbar createVerrechenbarForObject(AbstractDBObjectIdDeleted object) {
		if (object instanceof TarmedLeistung) {
			return new VerrechenbarTarmedLeistung((TarmedLeistung) object);
		} else if (object instanceof Labor2009Tarif) {
			return new VerrechenbarLabor2009Tarif((Labor2009Tarif) object);
		}

		log.warn("Unsupported object for create verrechenbar {}", object.getClass().getName());
		return null;
	}

	public Verrechnet create(IVerrechenbar iv, Behandlung kons, int count) {
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

		return v;

		// if (iv instanceof Artikel) {
		// ((Artikel) iv).einzelAbgabe(1);
		// }
		// // call the adjusters
		// for (IVerrechnetAdjuster adjuster : adjusters) {
		// adjuster.adjust(this);
		// }
	}

	public Optional<IVerrechenbar> getVerrechenbar(Verrechnet vr) {
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

	public void changeCount(Verrechnet foundVerrechnet, int i) {
		throw new UnsupportedOperationException();
		// TODO Auto-generated method stub

	}
}
