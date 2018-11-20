//package info.elexis.server.core.connector.elexis.billable;
//
//import java.util.List;
//
//import org.eclipse.core.runtime.IStatus;
//
//import ch.rgw.tools.TimeTool;
//import info.elexis.server.core.connector.elexis.billable.optifier.DefaultOptifier;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.PhysioLeistung;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
//import info.elexis.server.core.connector.elexis.services.FallService;
//import info.elexis.server.core.connector.elexis.services.VerrechnetService;
//
//public class VerrechenbarPhysioLeistung implements IBillable<PhysioLeistung> {
//
//	private final PhysioLeistung physioLeistung;
//
//	public VerrechenbarPhysioLeistung(PhysioLeistung physioLeistung) {
//		this.physioLeistung = physioLeistung;
//	}
//
//	@Override
//	public String getCodeSystemName() {
//		return "Physiotherapie";
//	}
//
//	@Override
//	public String getCodeSystemCode() {
//		return "311";
//	}
//
//	@Override
//	public String getId() {
//		return physioLeistung.getId();
//	}
//
//	@Override
//	public String getCode() {
//		return physioLeistung.getZiffer();
//	}
//
//	@Override
//	public String getText() {
//		return physioLeistung.getTitel();
//	}
//
//	@Override
//	public List<Object> getActions(Object context) {
//		return null;
//	}
//
//	@Override
//	public PhysioLeistung getEntity() {
//		return physioLeistung;
//	}
//
//	@Override
//	public int getTP(TimeTool date, Fall fall) {
//		return Integer.parseInt(physioLeistung.getTp());
//	}
//
//	@Override
//	public double getFactor(TimeTool date, Fall fall) {
//		String billingSystem = FallService.getAbrechnungsSystem(fall);
//		return VerrechnetService.getVKMultiplikator(date, billingSystem);
//	}
//
//	@Override
//	public IStatus add(Behandlung kons, Kontakt userContact, Kontakt mandatorContact, float count) {
//		return new DefaultOptifier().add(this, kons, userContact, mandatorContact, count);
//	}
//
//	@Override
//	public IStatus removeFromConsultation(Verrechnet vr, Kontakt mandatorContact) {
//		return new DefaultOptifier().remove(vr);
//	}
//
//	@Override
//	public VatInfo getVatInfo() {
//		return VatInfo.VAT_DEFAULT;
//	}
//}
