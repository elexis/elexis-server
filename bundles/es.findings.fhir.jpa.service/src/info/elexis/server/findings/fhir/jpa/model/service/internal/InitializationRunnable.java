package info.elexis.server.findings.fhir.jpa.model.service.internal;

public interface InitializationRunnable extends Runnable {

	public boolean isBlocking();

	public void cancel();

	public boolean isCancelled();
}
