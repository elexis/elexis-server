package info.elexis.server.core.redmine.internal.jaxrs;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SendLogConfiguration {
	
	private Integer issue;
	private Long maxsize;
	private String appender;
	
	public SendLogConfiguration(){
		appender = "ROLLING";
	}
	
	public Integer getIssue(){
		return issue;
	}
	
	public void setIssue(Integer issue){
		this.issue = issue;
	}
	
	public Long getMaxsize(){
		return maxsize;
	}
	
	public void setMaxsize(Long maxsize){
		this.maxsize = maxsize;
	}
	
	public String getAppender(){
		return appender;
	}
	
	public void setAppender(String appender){
		this.appender = appender;
	}
	
}
