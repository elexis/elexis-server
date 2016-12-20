#!/bin/bash -v
# Copyright 2016 (c) by Niklaus Giger
# License EPL
# Small helper to install the FHIR service into the elexis-server inside a docker container
cd `dirname $0`
pwd
export OSGI_CONSOLE=localhost:7234
export ELS_HOMEDIR=${HOME}/elexis-server/
echo "connect to elexis-server osgi console: telnet ${OSGI_CONSOLE}"
./elexis-server -console ${OSGI_CONSOLE} &
./wait-for-it.sh --timeout=90  ${OSGI_CONSOLE} -- echo "mysql osgi console is ready"
./telnet_test.expect


