package info.elexis.server.core.redmine.internal.jaxrs;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModelProperty;

@XmlRootElement
public class SendLogConfiguration {
	
	@ApiModelProperty(value = "Issue to append log file to. If 0 generates a new issue.")
	private Integer issue;
	@ApiModelProperty(value = "Maximum file size to transmit, defaults to 1 megabyte if 0")
	private Long maxsize;
	@ApiModelProperty(value = "The appender to retrieve the log file from", example = "ROLLING")
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
