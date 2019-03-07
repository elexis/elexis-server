#!/bin/bash
# Start the contained OpenVPN tunnel if the file client.ovpn exists
# set-up the letsencrypted certificate
#
set -e

if [ ! -f "client.ovpn" ]; then
	# no vpn client configuration, skip everything
	exit 0	
fi

##
# From https://www.reddit.com/r/synology/comments/74te0y/howto_deploy_openvpn_on_synology_using_docker/
##
# Create the necessary file structure for /dev/net/tun
 if ( [ ! -c /dev/net/tun ] ); then
     if ( [ ! -d /dev/net ] ); then
         mkdir -m 755 /dev/net
     fi
     mknod /dev/net/tun c 10 200
 fi

 if ( !(lsmod | grep -q "^tun\s") ); then
     echo "TUN kernel module not loaded, OpenVPN will not succeed."
     exit -1
 fi

openvpn --config client.ovpn --log /elexis/elexis-server/logs/openvpn.log --script-security 2 --up /letsencrypt.sh --daemon