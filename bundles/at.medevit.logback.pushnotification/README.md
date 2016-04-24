# Push Notification appender for Logback classic

This Logback <http://logback.qos.ch/> appender pushes log events via Prowl <http://www.prowlapp.com/> or Notify my Android <http://notifymyandroid.com/>.



##### Sample configuration

The following configuration filters all log events with Level ERROR

	<appender name="PushNotifications" class="at.medevit.logback.pushnotification.PushNotificationAppender">
		<application>My Service Instance @ Host</application>
		<apiKeys>40_char_apiKey_for_prowl_,48_char_apiKey_for_nma</apiKeys>
		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>ERROR</level>
		</filter>
	</appender>