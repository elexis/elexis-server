#!/bin/bash
ELPID=$(cat elexis-server.pid)
ELPGID=$(ps -o pgid= -p ${ELPID}) 
echo $ELPGID
pkill -g $ELPGID
rm elexis-server.pid
