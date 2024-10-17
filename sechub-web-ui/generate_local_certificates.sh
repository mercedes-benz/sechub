#!/bin/bash
# generates self singed certificates for local development
openssl genrsa 2048 > local-server.key
chmod 400 local-server.key
openssl req -new -x509 -nodes -sha256 -days 365 -key local-server.key -out local-server.crt
mkdir "certs"
mv local-server.key certs/
mv local-server.crt certs/