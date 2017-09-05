
# Mattermost Logback Appender

This fragment connects logback to mattermost.

### Configuration

The following sample logback configuration includes this appender. It is not recommended to use the appender for log levels lower WARN. 

	<sample>
		<appender name="Mattermost"
			class="at.medevit.logback.mattermost.MattermostAppender">
			<identification>[TEST] DEVELOPMENT</identification>
			<integrationUrl>https://mattermost.server.com/hooks/hookUrl</integrationUrl>
		</appender>
		<root level="WARN">
			<appender-ref ref="Mattermost" />
		</root>
	</sample>
	
	
#### Attachment based transmission

Log messages can be transmitted as [message attachment](url "https://docs.mattermost.com/developer/message-attachments.html") or [simple incoming webhook](url "https://docs.mattermost.com/developer/webhooks-incoming.html"). By default the simple incoming webhook method is used,
as these messages may be searched within the channel. The attachment based method can be activated by adding `<attachmentBased>true</attachmentBased>`
to the appender configuration. 

![Attachment based vs. native](doc/attachmentBased.png "Attachment based transmission vs. "native" transmission") 