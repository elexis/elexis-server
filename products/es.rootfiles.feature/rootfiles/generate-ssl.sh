#!/bin/bash
# MEDEVIT <office@medevit.at>
# Jetty Keystore self-signed-certificate generation script
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
x509_extensions = x509_ext
prompt = no

[req_distinguished_name]
C = CH
OU = Elexis-Server
CN = ${HOSTNAME} 

[v3_req]
subjectKeyIdentifier    = hash
basicConstraints        = CA:FALSE
keyUsage                = digitalSignature, keyEncipherment
subjectAltName          = @alternate_names
nsComment               = "OpenSSL Generated Certificate"

[ x509_ext ]
subjectKeyIdentifier    = hash
authorityKeyIdentifier  = keyid,issuer
basicConstraints        = CA:FALSE
keyUsage                = digitalSignature, keyEncipherment
subjectAltName          = @alternate_names
nsComment               = "OpenSSL Generated Certificate"

[alternate_names]
DNS.1 = ${HOSTNAME}
DNS.2 = localhost
DNS.3 = localhost.localdomain
DNS.4 = 127.0.0.1
DNS.5 = ::1
DNS.6 = fe80::1
IP.1 = 127.0.0.1
IP.2 = ::1
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
