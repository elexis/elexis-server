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

TIMESTAMP=`date "+%Y-%m-%d %H:%M:%S"`
echo "==== $TIMESTAMP startopenvpn.sh"

mkdir -p /dev/net
if [ ! -c /dev/net/tun ]; then
    mknod /dev/net/tun c 10 200
    # TODO if not success full, do not continue startup
fi

openvpn --config client.ovpn --log /elexis/elexis-server/logs/openvpn.log --script-security 2 --up /letsencrypt.sh --daemon