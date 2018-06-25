#!/bin/bash
# Start the contained OpenVPN tunnel if the file client.ovpn exists
# set-up the letsencrypted certificate
#
set -x

mkdir -p /dev/net
if [ ! -c /dev/net/tun ]; then
    mknod /dev/net/tun c 10 200
fi

if [ -f "client.ovpn" ]; then
	openvpn --config client.ovpn &
else
  return 0
fi

# LetsEncrypt
HOSTNAME=$(hostname)
DIG_RESOLV=`dig @ns1.dns-zonen.ch -t CNAME "$HOSTNAME" +short`
if [ "bridge.medelexis.ch." = $DIG_RESOLV ]; then
  mkdir -p /etc/letsencrypt/renewal-hooks/deploy
  ln -sf  /createESKeystore.sh /etc/letsencrypt/renewal-hooks/deploy/

  certbot certonly --standalone --preferred-challenges http --email es-certbot@medevit.at \
  --non-interactive --agree-tos --domains $HOSTNAME --logs-dir /etc/letsencrypt/logs --work-dir /etc/letsencrypt/work
else
  echo "[WARN] dig resolves to $DIG_RESOLV, should resolve to bridge.medelexis.ch."
fi