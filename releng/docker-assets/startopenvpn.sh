#!/bin/bash

if [ "$DEBUG" == "1" ]; then
  set -x
fi

mkdir -p /dev/net
if [ ! -c /dev/net/tun ]; then
    mknod /dev/net/tun c 10 200
fi

if [ -f "client.ovpn" ]; then
	echo "Starting OpenVPN connection"
	openvpn --config client.ovpn &
fi