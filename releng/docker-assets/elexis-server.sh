#!/bin/bash
set -x
set -e

trap 'shut_down' TERM INT

# register a termination handler, that cleans up
# on exiting the container
shut_down(){
	# we have to fetch the child java task and send
	# SIGTERM to it for correct app shutdown, the
	# eclipse launcher will then follow and exit $PID
	JAVAPID=$(cat /proc/$PID/task/$PID/children)
    echo "Sending SIGTERM to PID [$JAVAPID]"
    kill -TERM $JAVAPID
 
    wait $PID 
}

# dependencies
mkdir -p /elexis/elexis-server/logs

#
# Start Elexis-Server
#
# If the container was started with -e DEMO_MODE='true' we start a demo-instance
if [ ! -z $DEMO_MODE ]; then
	echo "Activating demo database"

	if [ ! -d "/elexis/elexis/demoDB" ]; then
		# fetch the demo database
		echo "Downloading demo database to /elexis/elexis/demoDB"
		mkdir -p /elexis/elexis # writable user dir for user elexis
		cd /elexis/elexis
		wget https://download.elexis.info/elexis/demoDB/demoDB_elexis_3.9.0.zip
		unzip demoDB_elexis_3.9.0.zip
		rm demoDB_elexis_3.9.0.zip
	fi
fi

# Handle ES properties
EFFECTIVE_JAVA_PROPERTIES="--add-opens=java.base/java.util=ALL-UNNAMED"

if [ ! -z $DISABLE_WEB_SECURITY ]; then
	echo "Starting with disabled web security"
	EFFECTIVE_JAVA_PROPERTIES+=" -Ddisable.web.security=true"
fi

EFFECTIVE_JAVA_PROPERTIES+=" $ES_JAVA_PROPERTIES"

echo "Effective Java properties: '"$EFFECTIVE_JAVA_PROPERTIES"'"

# Start-up the elexis-server
# https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fmisc%2Fruntime-options.html
/opt/elexis-server/elexis-server -console 7234 -XX:-OmitStackTraceInFastThrow --launcher.appendVmargs -vmargs ${EFFECTIVE_JAVA_PROPERTIES} &
PID=$!

echo "Started Elexis-Server launcher with PID [$PID]"

wait $PID
