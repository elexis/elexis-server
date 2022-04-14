package es.fhir.rest.core.resources.codeelements;

import ch.elexis.core.model.ICodeElement;

public class TransientCodeElement implements ICodeElement {
	public String codeSystemName;
	public String code;
	public String text;
	
	public TransientCodeElement(String codeSystemname, String code, String text){
		this.codeSystemName = codeSystemname;
		this.code = code;
		this.text = text;
	}
	
	@Override
	public String getCodeSystemName(){
		return codeSystemName;
	}
	
	@Override
	public String getCode(){
		return code;
	}
	
	@Override
	public void setCode(String value){}
	
	@Override
	public String getText(){
		return text;
	}
	
	@Override
	public void setText(String value){}
}