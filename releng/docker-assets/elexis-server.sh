#!/bin/bash
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
        
	if [ ! -z $DEMO_MODE ]; then
			echo "Deactivating demo database"
	
			cd /elexis/elexis-server/	
			rm -v elexis-connection.xml
	
    			if [ -f "elexis-connection-backup.xml" ]; then
    				echo "Restoring database connection configuration"
    				mv -v elexis-connection.backup.xml elexis-connection.xml
    			fi
	fi
}

mkdir -p /elexis/elexis-server/logs

# If the container was started with -e DEMO_MODE='true' we start a demo-instance
if [ ! -z $DEMO_MODE ]; then
	echo "Activating demo database"

	if [ ! -d "/elexis/demoDB" ]; then
		# fetch the demo database
		echo "Downloading demo database to /elexis/demoDB"
		cd /elexis/
		wget http://download.elexis.info/elexis/demoDB/demoDB_elexis_DBVersion_3.2.7.zip
		unzip demoDB_elexis_DBVersion_3.2.7.zip
		rm demoDB_elexis_DBVersion_3.2.7.zip
	fi
	
	cd /elexis/elexis-server/
	
	if [ -f "elexis-connection.xml" ]; then
		echo "Backing up database connection configuration"
		mv -v elexis-connection.xml elexis-connection-backup.xml
	fi
	
	echo "Writing /elexis/elexis-server/elexis-connection.xml"
	
	cat > elexis-connection.xml << EOF
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<dbConnection hostName="localhost" port="" databaseName="" connectionString="jdbc:h2:///elexis/demoDB/db;AUTO_SERVER=TRUE" username="sa" password="">
    	<rdbmsType>H2</rdbmsType>
</dbConnection>
EOF
	
fi

# Start-up the elexis-server
/opt/elexis-server/elexis-server -console 7234 &
PID=$!

echo "Started Elexis-Server launcher with PID [$PID]"

wait $PID