<!-- SPDX-License-Identifier: MIT --->
# SecHub Web UI

This Helm chart is used to deploy the SecHub Web UI to a Kubernetes cluster.

## SSL certificates and keys

### Default SSL setup
The web-ui container image is shipped with initially created self-signed certificates.

### User-provided SSL keystore
If you want to provide your own "official" certificates,
you need to create a pkcs12 keystore with a "name" attribute (e.g. `sechub-web-ui.example.com`).

#### How to create the .p12 keystore
Create a private key (`sechub-web-ui.key`), create a certificate signed by a CA of your trust (`sechub-web-ui.cert`).
```bash
NAME="sechub-web-ui.example.com"
openssl pkcs12 -export -in sechub.corpinter.net-full-chain.crt -inkey sechub.corpinter.net_server.key -out ${NAME}.p12 -name ${NAME}
# Enter pass phrase for private key file
# Enter Export Password for .p12 keystore
```

#### Set .p12 and its password as Kubernetes secrets
A Kubernetes secret `secret-web-ui-ssl` must be defined containing
- `keystore_file` - Your .p12 keystore file created in the previous step<br>
  Inside the .p12 keystore, an alias with ${NAME} is expected pointing to the ssl certificate to use
- `keystore_password` - Export Password for .p12 keystore from above step

#### Update your values.yaml
Declare ${NAME} as your keystore alias in your Helm values.yaml file.

Example:
```yaml
web_ui:
  ssl:
    keystoreAlias: "sechub-web-ui.example.com"
```

#### Deploy
Now you can deploy using your values.yaml file from above.

Then the SecHub web-ui will use your certificates and key for https encryption.
