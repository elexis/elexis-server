package info.elexis.server.core.redmine.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.internal.comm.BaseCommunicator;

import ch.elexis.core.constants.ElexisEnvironmentPropertyConstants;
import ch.elexis.core.model.MimeType;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;

public enum RedmineUtil {
		
		INSTANCE;
	
	private ch.qos.logback.classic.Logger ROOT_LOGGER;
	
	private Logger logger;
	
	private RedmineUtil(){
		ROOT_LOGGER =
			(ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseCommunicator.class))
			.setLevel(Level.INFO);
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RedmineManager.class))
			.setLevel(Level.INFO);
		logger = LoggerFactory.getLogger(getClass());
	}
	
	/**
	 * 
	 * @param appenderName
	 *            <code>null</code> or the appender to use
	 * @param apiKey
	 *            <code>null</code> or the apiKey to connect to redmine with
	 * @param issueId
	 *            <code>null</code> or the issue number to append the logfile to. If
	 *            <code>null</code> a new issue is created. For invalid values (<=0)
	 *            <code>null</code> will be set
	 * @param sizeLimit
	 *            <code>null</code> defaults to 1 megabyte, no more than 10mb allowed. For invalid
	 *            values (<=0 or > 10mb) <code>null</code> 1 mb will be set
	 * @throws RedmineException
	 * @throws IOException
	 * @return issueUrl the logfile was attached to
	 */
	String sendLogToRedmine(String appenderName, String apiKey, Integer issueId, Long sizeLimit)
		throws RedmineException, IOException{
		
		if (appenderName == null) {
			appenderName = "ROLLING";
		}
		
		if (sizeLimit == null || sizeLimit <= 0 || sizeLimit > (10 * 1024 * 1024)) {
			sizeLimit = Long.valueOf(1024 * 1024);
		}
		
		if (issueId != null && issueId <= 0) {
			issueId = null;
		}
		
		Appender<ILoggingEvent> appender = ROOT_LOGGER.getAppender(appenderName);
		if (!(appender instanceof FileAppender)) {
			throw new IllegalArgumentException(
				"Appender [" + appenderName + "] not found or not of type FileAppender");
		}
		
		String logFile = ((FileAppender<ILoggingEvent>) appender).getFile();
		File elexisLog = new File(logFile);
		if (!elexisLog.exists() || !elexisLog.canRead()) {
			throw new IOException("Error accessing [" + logFile + "]");
		}
		if (elexisLog.length() <= 0) {
			throw new IOException("File [" + logFile + "] is length 0");
		}
		
		RedmineManager mgr = getRedmineManager(apiKey);
		Issue issue = getOrCreateIssue(mgr, issueId);
		
		byte[] readFileLengthMax = readFileLengthMax(elexisLog, sizeLimit);
		
		Attachment attachment = mgr.getAttachmentManager().uploadAttachment(
			"elexis_server_" + appenderName + "_log.txt", MimeType.txt.getContentType(),
			readFileLengthMax);
		issue.addAttachment(attachment);
		mgr.getIssueManager().update(issue);
		
		String issueUrl = getRedmineBaseUrl() + "/issues/" + issue.getId();
		logger.info("Uploaded [{}] to [{}]", attachment.getFileName(), issueUrl);
		return issueUrl;
		
	}
	
	public String sendLogToRedmine(Integer issueId) throws RedmineException, IOException{
		return sendLogToRedmine(null, null, issueId, null);
	}
	
	public String sendLogToRedmine(String appenderName, Integer issueId, Long sizeLimit)
		throws RedmineException, IOException{
		return sendLogToRedmine(appenderName, null, issueId, sizeLimit);
	}
	
	private Issue getOrCreateIssue(RedmineManager mgr, Integer issueId) throws RedmineException{
		Issue issue;
		if (issueId == null) {
			Project project =
				mgr.getProjectManager().getProjectByKey(Constants.DEFAULT_REDMINE_PROJECT);
			Issue _issue = IssueFactory.create(project.getId(), getIssueSubject());
			issue = mgr.getIssueManager().createIssue(_issue);
		} else {
			issue = mgr.getIssueManager().getIssueById(issueId, Include.watchers);
		}
		return issue;
	}
	
	/**
	 * @return
	 * @see https://github.com/taskadapter/redmine-java-api
	 */
	private RedmineManager getRedmineManager(String apiKey){
		return RedmineManagerFactory.createWithApiKey(getRedmineBaseUrl(), getRedmineApiKey());
	}
	
	private byte[] readFileLengthMax(File file, Long maxLength) throws IOException{
		RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
		long fileLength = file.length() - 1;
		
		if (fileLength > maxLength) {
			fileHandler.seek(fileLength - maxLength);
			// move to end of line
			for (int readByte = 0; readByte != 0xA;)
				readByte = fileHandler.readByte();
		}
		
		ByteArrayOutputStream attachment = new ByteArrayOutputStream();
		// write the file to the byte array of the output stream
		int count;
		byte[] buffer = new byte[2048];
		while ((count = fileHandler.read(buffer, 0, buffer.length)) != -1) {
			attachment.write(buffer, 0, count);
		}
		fileHandler.close();
		attachment.flush();
		return attachment.toByteArray();
	}
	
	String getIssueSubject(){
		String eeHostname = System.getenv(ElexisEnvironmentPropertyConstants.EE_HOSTNAME);
		String organizationName =
			System.getenv(ElexisEnvironmentPropertyConstants.ORGANISATION_NAME);
		if (organizationName != null) {
			organizationName = organizationName.replace("__", " ");
		}
		
		return "ES " + eeHostname + " (" + organizationName + ")";
		
	}
	
	String getRedmineApiKey(){
		String apiKey = System.getenv(Constants.ENV_VAR_REDMINE_API_KEY);
		if (apiKey == null) {
			throw new IllegalStateException("No apiKey provided");
		}
		return apiKey;
	}
	
	String getRedmineBaseUrl(){
		String redmineBaseUrl = System.getenv(Constants.ENV_VAR_REDMINE_BASE_URL);
		if (redmineBaseUrl == null) {
			redmineBaseUrl = Constants.DEFAULT_REDMINE_BASE_URL;
		}
		return redmineBaseUrl;
	}
	
}
