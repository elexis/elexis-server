#!/bin/bash
# Create the Elexis-Server keystore from letsencrypt certificate files
# executed from /etc/letsencrypt/renewal-hooks/post
set -x

CERTDIR=/etc/letsencrypt/live/$(hostname)

if [ ! -d ${CERTDIR} ]; then
	return 0;
fi

openssl pkcs12 -CAfile ${CERTDIR}/chain.pem -inkey ${CERTDIR}/privkey.pem -in ${CERTDIR}/fullchain.pem -export -out /elexis/elexis-server/elexis-server.p12 -passout pass:elexis-server
chown elexis:elexis /elexis/elexis-server/elexis-server.p12