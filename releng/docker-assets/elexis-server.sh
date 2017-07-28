#!/bin/bash
trap 'shut_down' TERM INT

shut_down(){
	# we have to fetch the child java task and send
	# SIGTERM to it for correct app shutdown, the
	# eclipse launcher will then follow and exit $PID
	JAVAPID=$(cat /proc/$PID/task/$PID/children)
    echo "Sending SIGTERM to PID [$JAVAPID]"
    kill -TERM $JAVAPID
 
    wait $PID  
}

/opt/elexis-server/elexis-server -console 7234 &
PID=$!

echo "Started Elexis-Server launcher with PID [$PID]"

wait $PID