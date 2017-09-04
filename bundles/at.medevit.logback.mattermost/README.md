
# Mattermost Logback Appender


##### Sample configuration

The following sample logback configuration includes this appender. It is not recommended to use the appender for log levels < WARN. 

	<sample>
		<appender name="Mattermost"
			class="at.medevit.logback.mattermost.MattermostAppender">
			<identification>stationIdentification</identification>
			<integrationUrl>https://mattermost.server.com/hooks/hookUrl
			</integrationUrl>
		</appender>
		<root level="WARN">
			<appender-ref ref="Mattermost" />
		</root>
	</sample>