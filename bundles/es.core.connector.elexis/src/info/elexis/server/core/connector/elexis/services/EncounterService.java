package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import info.elexis.server.core.connector.elexis.services.internal.CoreModelServiceHolder;

public class EncounterService extends PersistenceService2 {



//	public static Optional<IBillable<?>> findBillableByAbstractDBObjectIdDeleted(
//			AbstractDBObjectIdDeleted billableObject) {
//		if (billableObject instanceof TarmedLeistung) {
//			return Optional.of(new VerrechenbarTarmedLeistung((TarmedLeistung) billableObject));
//		} else if (billableObject instanceof Labor2009Tarif) {
//			return Optional.of(new VerrechenbarLabor2009Tarif((Labor2009Tarif) billableObject));
//		} else if (billableObject instanceof PhysioLeistung) {
//			return Optional.of(new VerrechenbarPhysioLeistung((PhysioLeistung) billableObject));
//		} else if (billableObject instanceof ArtikelstammItem) {
//			return Optional.of(new VerrechenbarArtikelstammItem((ArtikelstammItem) billableObject));
//		} else if (billableObject instanceof Eigenleistung) {
//			return Optional.of(new VerrechenbarEigenleistung((Eigenleistung) billableObject));
//		} else if (billableObject instanceof Artikel) {
//			return Optional.of(new VerrechenbarArtikel((Artikel) billableObject));
//		}
//		return Optional.empty();
//	}
//
//	public static IStatus chargeBillableOnBehandlung(Behandlung kons, IBillable<?> billableObject, Kontakt userContact,
//			Kontakt mandatorContact) {
//		return chargeBillableOnBehandlung(kons, billableObject, userContact, mandatorContact, 1);
//	}
//
//	/**
//	 * Convenience method, calling
//	 * {@link #chargeBillableOnBehandlung(Behandlung, IBillable, Kontakt, Kontakt, float)}
//	 * with mandator and user = kons.getMandant and count = 1
//	 * 
//	 * @param kons
//	 * @param billableObject
//	 * @return
//	 */
//	public static IStatus chargeBillableOnBehandlung(Behandlung kons, IBillable<?> billableObject) {
//		return chargeBillableOnBehandlung(kons, billableObject, kons.getMandant(), kons.getMandant(), 1);
//	}
//
//	/**
//	 * 
//	 * @param kons
//	 * @param billableObject
//	 * @param userContact
//	 * @param mandatorContact
//	 * @param count
//	 * @return
//	 * @since 1.5
//	 */
//	public static IStatus chargeBillableOnBehandlung(Behandlung kons, IBillable<?> billableObject, Kontakt userContact,
//			Kontakt mandatorContact, float count) {
//		IStatus editableStatus = BehandlungService.isEditable(kons, mandatorContact);
//		if (!editableStatus.isOK()) {
//			return editableStatus;
//		}
//		return billableObject.add(kons, userContact, mandatorContact, count);
//	}
//
//	public static IStatus isEditable(Behandlung kons, Kontakt mandator) {
//		boolean caseOk = false;
//		boolean mandatorOk = false;
//		boolean billOk = false;
//
//		// is the allocated case still open ?
//		Fall fall = kons.getFall();
//		if (fall == null || FallService.isOpen(fall)) {
//			caseOk = true;
//		}
//
//		// is the billing mandator the owner of this consultation
//		// or does he have the right to bill on all consultations?
//		Kontakt mandant = kons.getMandant();
//		if (mandant != null && mandator != null) {
//			if (mandant.getId().equals(mandator.getId())) {
//				mandatorOk = true;
//			} else {
//				Optional<User> user = UserService.findByKontakt(mandator);
//				if (user.isPresent()) {
//					if (user.get().isActive()) {
//						mandatorOk = true;
//						// TODO check for rights
//					}
//				}
//			}
//		} else {
//			mandatorOk = true;
//		}
//
//		// has the consultation already been billed ?
//		Invoice invoice = kons.getInvoice();
//		if (invoice == null) {
//			billOk = true;
//		} else {
//			InvoiceState state = invoice.getState();
//			if (state == InvoiceState.DEPRECIATED) {
//				billOk = true;
//			}
//		}
//
//		if (caseOk && mandatorOk && billOk) {
//			return Status.OK_STATUS;
//		} else {
//			MultiStatus ms = new MultiStatus(BundleConstants.BUNDLE_ID, Status.INFO,
//					"Konsultation ist nicht editierbar", null);
//			if (!caseOk) {
//				ms.add(new Status(Status.INFO, BundleConstants.BUNDLE_ID,
//						"Der Fall zu dieser Konsultation ist abgeschlosssen."));
//			}
//			if (!billOk) {
//				ms.add(new Status(Status.INFO, BundleConstants.BUNDLE_ID,
//						"Für diese Behandlung wurde bereits eine Rechnung erstellt."));
//			}
//			if (!mandatorOk) {
//				ms.add(new Status(Status.INFO, BundleConstants.BUNDLE_ID, "Diese Behandlung ist nicht von Ihnen."));
//			}
//			return ms;
//		}
//	}
//
//	/**
//	 * 
//	 * @param patient
//	 * @return all {@link Behandlung} for patient, ordered by date (newest first)
//	 */
	public static List<IEncounter> findAllEncountersForPatient(IPatient patient){
		IQuery<ICoverage> query = CoreModelServiceHolder.get().getQuery(ICoverage.class);
		query.and(ModelPackage.Literals.ICOVERAGE__PATIENT, COMPARATOR.EQUALS, patient);
		List<ICoverage> coverages = query.execute();
		List<IEncounter> collect = coverages.stream().flatMap(cv -> cv.getEncounters().stream())
			.sorted((c1, c2) -> c2.getDate().compareTo(c1.getDate())).collect(Collectors.toList());
		return collect;
	}
//
//	/**
//	 * Set a specific diagnosis on a consultation
//	 * 
//	 * @param cons
//	 * @param diag
//	 */
//	public static void setDiagnosisOnConsultation(Behandlung cons, Diagnosis diag) {
//		diag = new DiagnosisService().findExistingOrCreate(diag);
//		cons.getDiagnoses().add(diag);
//		BehandlungService.save(cons);
//	}
//
//	/**
//	 * Retrieve all user id strings for all versioned-entries of a consultation.
//	 * These entries may or may not be resolvable via
//	 * {@link UserService#load(String)}
//	 * 
//	 * @param cons
//	 * @return
//	 */
//	public static Set<String> getAllCareProviderIdsForConsultation(Behandlung cons) {
//		Set<String> userIdString = new HashSet<>();
//		VersionedResource vr = cons.getEintrag();
//		int version = 0;
//		ResourceItem item;
//		while ((item = vr.getVersion(version)) != null) {
//			userIdString.add(item.remark);
//			version++;
//		}
//		return userIdString;
//	}

}
