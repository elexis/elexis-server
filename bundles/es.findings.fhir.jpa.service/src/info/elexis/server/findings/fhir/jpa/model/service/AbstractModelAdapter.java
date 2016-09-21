package info.elexis.server.findings.fhir.jpa.model.service;

import info.elexis.server.findings.fhir.jpa.model.service.internal.FhirHelper;

public abstract class AbstractModelAdapter<T> {

	private T model;
	private FhirHelper fhirHelper;

	public AbstractModelAdapter(T model) {
		this.model = model;
		this.fhirHelper = new FhirHelper();
	}

	public T getModel() {
		return model;
	}

	protected FhirHelper getFhirHelper() {
		return fhirHelper;
	}
}
