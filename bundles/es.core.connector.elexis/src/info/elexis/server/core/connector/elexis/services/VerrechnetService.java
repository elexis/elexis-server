package info.elexis.server.core.connector.elexis.services;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.article.IArticle;
import ch.elexis.core.model.verrechnet.Constants;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.status.StatusUtil;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarArtikel;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarArtikelstammItem;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarEigenleistung;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarLabor2009Tarif;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.billable.adjuster.VatVerrechnetAdjuster;
import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
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

public class VerrechnetService extends PersistenceService {

	private static Logger log = LoggerFactory.getLogger(VerrechnetService.class);

	public static class Builder extends AbstractBuilder<Verrechnet> {
		@SuppressWarnings("rawtypes")
		private final IBillable iv;

		public Builder(@SuppressWarnings("rawtypes") IBillable iv, Behandlung kons, float count, Kontakt userContact) {
			object = new Verrechnet();

			this.iv = iv;

			object.setLeistungenText(iv.getText());
			String keyForObject = ElexisTypeMap.getKeyForObject((AbstractDBObjectIdDeleted) iv.getEntity());
			object.setKlasse(keyForObject);
			object.setLeistungenCode(iv.getId());
			object.setLeistungenText(iv.getText());
			object.setBehandlung(kons);
			object.setUser(userContact);

			TimeTool dat = new TimeTool(kons.getDatum());
			Fall fall = kons.getFall();
			int tp = iv.getTP(dat, kons);
			double factor = iv.getFactor(dat, fall);
			long preis = Math.round(tp * factor);

			object.setEk_kosten(Integer.parseInt(iv.getCost(dat).getCentsAsString()));
			object.setVk_tp(tp);
			object.setVk_scale(Double.toString(factor));
			object.setVk_preis((int) preis);

			object.setDerivedCountValue(count);
		}

		@Override
		public Verrechnet build() {
			if (iv.getEntity() instanceof IArticle) {
				// only output for integer values
				// currently only RH bills, not considering the selling unit, hence we ignore it
				// here
				IStatus status = new StockService().performDisposal((IArticle) iv.getEntity(),
						object.getDerivedCountValue(), null);
				if (!status.isOK()) {
					StatusUtil.logStatus(log, status, true);
				}
			}
			// call the adjusters
			new VatVerrechnetAdjuster().adjust(object);
			return super.build();
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Verrechnet> load(String id) {
		return PersistenceService.load(Verrechnet.class, id).map(v -> (Verrechnet) v);
	}

	/**
	 * convenience method
	 * 
	 * @param includeElementsMarkedDeleted
	 * @return
	 */
	public static List<Verrechnet> findAll(boolean includeElementsMarkedDeleted) {
		return PersistenceService.findAll(Verrechnet.class, includeElementsMarkedDeleted).stream()
				.map(v -> (Verrechnet) v).collect(Collectors.toList());
	}

	private static IBillable<? extends AbstractDBObjectIdDeleted> createVerrechenbarForObject(
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

	/**
	 * The article or service this object was billed out of
	 * 
	 * @param vr
	 * @return
	 */
	public static Optional<AbstractDBObjectIdDeleted> getOriginService(Verrechnet vr) {
		String clazz = vr.getKlasse();
		String leistungenCode = vr.getLeistungenCode();

		return StoreToStringService.INSTANCE
				.createDetachedFromString(clazz + StringConstants.DOUBLECOLON + leistungenCode);
	}

	@SuppressWarnings("rawtypes")
	public static Optional<IBillable> getVerrechenbar(Verrechnet vr) {
		Optional<AbstractDBObjectIdDeleted> object = getOriginService(vr);
		if (object.isPresent()) {
			return Optional.ofNullable(createVerrechenbarForObject(object.get()));
		}
		return Optional.empty();
	}

	public static double getVKMultiplikator(TimeTool date, String billingSystem) {
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

	/**
	 * Perform a validated (returns Status error on fail) change on the count of the
	 * {@link Verrechnet}
	 * 
	 * @param verrechnet
	 * @param newCount
	 * @param mandatorContact
	 * @return {@link ObjectStatus} containing the refreshed {@link Verrechnet} if
	 *         {@link Status#OK_STATUS}, else an {@link IStatus}
	 */
	public static IStatus changeCountValidated(Verrechnet verrechnet, int newCount, Kontakt mandatorContact) {
		return changeCountValidated(verrechnet, (float) newCount, mandatorContact);
	}

	/**
	 * Perform a validated (returns Status error on fail) change on the count of the
	 * {@link Verrechnet}
	 * 
	 * @param verrechnet
	 * @param newCount
	 * @param mandatorContact
	 * @return {@link ObjectStatus} containing the refreshed {@link Verrechnet} if
	 *         {@link Status#OK_STATUS}, else an {@link IStatus}
	 */
	public static IStatus changeCountValidated(Verrechnet verrechnet, float newCount, Kontakt mandatorContact) {
		float previous = verrechnet.getDerivedCountValue();
		if (newCount == previous && verrechnet.getScale() == 100 && verrechnet.getScale2() == 100) {
			return Status.OK_STATUS;
		}

		@SuppressWarnings("rawtypes")
		Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(verrechnet);
		if (newCount == 0) {
			log.trace("Removing Verrechnet [{}] as count == 0", verrechnet.getId());
			IStatus ret = verrechenbar.get().removeFromConsultation(verrechnet, mandatorContact);
			if (!ret.isOK()) {
				return ret;
			}
		}

		if (newCount % 1 == 0) {
			// integer -> full package
			float difference = newCount - previous;
			if (difference > 0) {
				// addition
				IStatus ret = verrechenbar.get().add(verrechnet.getBehandlung(), verrechnet.getUser(), mandatorContact,
						difference);
				if (!ret.isOK()) {
					return ret;
				}

				return ObjectStatus.OK_STATUS(VerrechnetService.reload(verrechnet).get());
			} else {
				// subtraction, we assume that this will never fail
				verrechnet.setDerivedCountValue(newCount);
				return ObjectStatus.OK_STATUS(VerrechnetService.save(verrechnet));
			}
		} else {
			verrechnet.setDerivedCountValue(newCount);

			return ObjectStatus.OK_STATUS(VerrechnetService.save(verrechnet));
		}
	}

	public static List<Verrechnet> getAllVerrechnetForBehandlung(Behandlung behandlung) {
		JPAQuery<Verrechnet> qre = new JPAQuery<Verrechnet>(Verrechnet.class);
		qre.add(Verrechnet_.behandlung, QUERY.EQUALS, behandlung);
		return qre.execute();
	}

	public static Optional<Verrechnet> getVerrechnetForBehandlung(Behandlung kons, IBillable<?> billable) {
		if (billable != null && billable.getId() != null) {
			JPAQuery<Verrechnet> qbe = new JPAQuery<Verrechnet>(Verrechnet.class);
			qbe.add(Verrechnet_.behandlung, QUERY.EQUALS, kons);
			qbe.add(Verrechnet_.leistungenCode, QUERY.EQUALS, billable.getId());

			List<Verrechnet> verrechnets = qbe.execute();
			if (verrechnets.size() == 1) {
				return Optional.of(verrechnets.get(0));
			}
		}
		return Optional.empty();
	}
	
	public static boolean isChangedPrice(Verrechnet verrechnet){
		String value = verrechnet.getDetail("changedPrice");
		if (value != null) {
			return value.equalsIgnoreCase("true");
		}
		return false;
	}

	/**
	 * Get the AL points used to calculate the value of the {@link Verrechnet}. The
	 * value is set by the {@link TarmedOptifier}. If not found, the value of
	 * {@link TarmedLeistung#getAL()} is returned. If no information is found 0 is
	 * returned.
	 * 
	 * @param verrechnet
	 * @return
	 */
	public static int getAL(Verrechnet verrechnet) {
		// if price was changed, use TP as AL
		boolean changedPrice = VerrechnetService.isChangedPrice(verrechnet);
		if (changedPrice) {
			return verrechnet.getVk_tp();
		}
		String alString = (String) verrechnet.getDetail().get(Verrechnet.EXT_VERRRECHNET_AL);
		if (alString != null) {
			try {
				return Integer.parseInt(alString);
			} catch (NumberFormatException ne) {
				// ignore, try resolve from IVerrechenbar
			}
		}
		Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(verrechnet);
		if (verrechenbar.isPresent()) {
			Identifiable entity = verrechenbar.get().getEntity();
			if (entity instanceof TarmedLeistung) {
				TarmedLeistung tl = (TarmedLeistung) entity;
				Behandlung kons = verrechnet.getBehandlung();
				if (kons != null) {
					return TarmedLeistungService.getAL(tl, kons.getMandant());
				} else {
					tl.getAL();
				}
			}

		}
		return 0;
	}

	/**
	 * Den Preis nach Anwendung sämtlicher SKalierungsfaktoren zurückgeben
	 * 
	 * @return
	 */
	public static Money getNettoPreis(Verrechnet verrechnet) {
		Money brutto = getBruttoPreis(verrechnet);
		brutto.multiply(verrechnet.getPrimaryScaleFactor());
		brutto.multiply(verrechnet.getSecondaryScaleFactor());

		// call the adjusters
		// for (IVerrechnetAdjuster adjuster : adjusters) {
		// adjuster.adjustGetNettoPreis(this, brutto);
		// }

		return brutto;
	}

	/**
	 * Den Preis nach Anwendung des Taxpunktwerts (aber ohne sonstige Skalierungen)
	 * holen
	 */
	@SuppressWarnings("rawtypes")
	public static Money getBruttoPreis(Verrechnet verrechnet) {
		int tp = verrechnet.getVk_tp();
		Behandlung k = verrechnet.getBehandlung();
		Fall fall = k.getFall();
		TimeTool date = new TimeTool(k.getDatum());
		Optional<IBillable> v = VerrechnetService.getVerrechenbar(verrechnet);
		double tpw = 1.0;
		if (v.isPresent()) { // Unknown tax system
			tpw = v.get().getFactor(date, fall);
		}
		return new Money((int) Math.round(tpw * tp));
	}

	/**
	 * 
	 * @param verrechnet
	 * @return the {@link IBillable#getCode()} the provided {@link Verrechnet} is
	 *         based ond, ? if not resolvable
	 */
	@SuppressWarnings("rawtypes")
	public static String getCode(Verrechnet verrechnet) {
		Optional<IBillable> verrechenbar = getVerrechenbar(verrechnet);
		if (verrechenbar.isPresent()) {
			return verrechenbar.get().getCode();
		} else {
			return "?";
		}
	}
}
