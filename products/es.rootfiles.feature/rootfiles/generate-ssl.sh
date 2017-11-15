#!/bin/bash
# MEDEVIT <office@medevit.at>
# Jetty Keystore generation script
HOSTNAME=$(hostname) 
PROGDIR=$(pwd)
STOREDIR=$HOME/elexis-server/ssl
KEYSTORE_FILE=elexis-server.keystore

function line {
        echo "----------------------------------------"
}

function key_config {
cat <<EOF > req.conf
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no
[req_distinguished_name]
C = CH
OU = Elexis-Server
CN = ${HOSTNAME} 
[v3_req]
keyUsage = keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names
[alt_names]
DNS.1 = ${HOSTNAME} 
EOF
}

PASSWORD=elexis-server
OBF_PASSWORD=$(java -jar $PROGDIR/JettyPassword.jar $PASSWORD)

mkdir $STOREDIR 
cd $STOREDIR
key_config
openssl req -nodes -newkey rsa:2048 -x509 -days 365 -keyout elexis-server-key.pem -out elexis-server.pem -config req.conf 
openssl x509 -text -noout -in elexis-server.pem
openssl pkcs12 -inkey elexis-server-key.pem -in elexis-server.pem -export -out elexis-server.p12 -passout pass:${PASSWORD}
rm $KEYSTORE_FILE
keytool -importkeystore -srckeystore elexis-server.p12 -srcstorepass ${PASSWORD} -srcstoretype PKCS12 -destkeystore $KEYSTORE_FILE -deststorepass ${PASSWORD}

echo Files generated to $STOREDIR
echo Password is $OBF_PASSWORD
