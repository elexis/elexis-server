#!/bin/bash
# Start the contained OpenVPN tunnel if the file client.ovpn exists
# set-up the letsencrypted certificate
#
set -e

if [ ! -f "client.ovpn" ]; then
	# no vpn client configuration, skip everything
	exit 0	
fi

mkdir -p /dev/net
if [ ! -c /dev/net/tun ]; then
    mknod /dev/net/tun c 10 200
fi

openvpn --config client.ovpn --log /elexis/elexis-server/logs/openvpn.log --script-security 2 --up /letsencrypt.sh --daemon