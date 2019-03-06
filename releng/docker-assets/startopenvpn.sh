#!/bin/bash
# Start the contained OpenVPN tunnel if the file client.ovpn exists
# set-up the letsencrypted certificate
#
set -x
set -e

if [ ! -f "client.ovpn" ]; then
	# no vpn client configuration, skip everything
	return 0	
fi

echo "Found client.ovpn, trying to set-up ..."

mkdir -p /dev/net
if [ ! -c /dev/net/tun ]; then
    mknod /dev/net/tun c 10 200
fi

openvpn --config client.ovpn --daemon

# LetsEncrypt
HOSTNAME=$(hostname)
DIG_RESOLV=`dig @ns1.dns-zonen.ch -t CNAME "$HOSTNAME" +short`
if [ "bridge.medelexis.ch." = $DIG_RESOLV ]; then
  mkdir -p /etc/letsencrypt/renewal-hooks/deploy
  ln -sf  /createESKeystore.sh /etc/letsencrypt/renewal-hooks/deploy/

  certbot certonly --standalone --preferred-challenges http --email es-certbot@medevit.at \
  --non-interactive --agree-tos --domains $HOSTNAME --logs-dir /etc/letsencrypt/logs --work-dir /etc/letsencrypt/work
  
  # Start cron daemon for automatic letsenrypt cert renewal
  /user/sbin/crond
else
  echo "[WARN] dig resolves to $DIG_RESOLV, should resolve to bridge.medelexis.ch."
fi