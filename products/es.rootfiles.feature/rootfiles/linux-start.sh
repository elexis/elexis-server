#!/bin/bash
OSGI_CONSOLE=localhost:7234
ELS_HOMEDIR=${HOME}/elexis-server/
nohup ./elexis-server -console ${OSGI_CONSOLE} > ${ELS_HOMEDIR}/logs/console.log 2>&1 & echo $! > elexis-server.pid
echo "connect to elexis-server osgi console: telnet ${OSGI_CONSOLE}"
