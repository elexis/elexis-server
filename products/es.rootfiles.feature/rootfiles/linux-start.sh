#!/bin/bash
ELS_HOMEDIR=${HOME}/elexis-server/
nohup ./elexis-server -console localhost:7234 > ${ELS_HOMEDIR}/logs/console.log 2>&1 & echo $! > elexis-server.pid