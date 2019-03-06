#!/bin/bash
# Called by openvpn in startopenvpn.sh
set -x
set -e

# LetsEncrypt
HOSTNAME=$(hostname)
DIG_RESOLV=`dig @ns1.dns-zonen.ch -t CNAME "$HOSTNAME" +short`
if [ -z $DIG_RESOLV ]; then
	echo "[WARN] No CNAME entry for $HOSTNAME found, should resolve to bridge.medelexis.ch"
	exit 0
fi

if [ "bridge.medelexis.ch." = $DIG_RESOLV ]; then
  mkdir -p /etc/letsencrypt/renewal-hooks/deploy
  ln -sf  /createESKeystore.sh /etc/letsencrypt/renewal-hooks/deploy/

  certbot certonly --standalone --preferred-challenges http --email es-certbot@medevit.at \
  --non-interactive --agree-tos --domains $HOSTNAME --logs-dir /etc/letsencrypt/logs --work-dir /etc/letsencrypt/work
  
  # Start cron daemon for automatic letsenrypt cert renewal
  /usr/sbin/crond
else
  echo "[WARN] dig resolves to $DIG_RESOLV, should resolve to bridge.medelexis.ch."
fi