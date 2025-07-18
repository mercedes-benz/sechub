<!--- SPDX-License-Identifier: MIT -->
# SecHub plugin for VSCode, VSCodium and Eclipse Theia

This is a SecHub plugin for [VSCodium](https://vscodium.com/), [Eclipse Theia](https://theia-ide.org/) and [VS Code](https://code.visualstudio.com/) to read and navigate through SecHub reports.

## Features

* Read and navigate through SecHub reports
* Load reports directly from sechub server
* Supported modules: `iacScan`, `codeScan` and `secretScan`

## Installation

Recommended: Install the plugin from the Open-VSX marketplace from within [VSCodium](https://vscodium.com/) or [Eclipse Theia](https://theia-ide.org/) by searching for the term: `sechub` in the `Extensions` manager.

For VS Code you need to download the [plugin](https://open-vsx.org/extension/mercedes-benz/sechub) and install it manually. It is also possible to install the plugin manually in VSCodium and Eclipse Theia.

NOTE: Please use the new plugin from Mercedes-Benz: <https://open-vsx.org/extension/mercedes-benz/sechub>. The old Daimler plugin will be deprecated. The reason for the deprecation is the rebranding of Daimler to Mercedes-Benz.

## Development

### Develop

1. Install Node.js

   * [Windows, macOS and Linux](https://nodejs.org/en/download)
   * [`deb` and `rpm` packages (Debian/Ubuntu, RHEL/Fedora etc.)](https://github.com/nodesource/distributions/tree/master)
   * [Node.js releases](https://nodejs.dev/en/about/releases/)

2. Build the SecHub Openapi Typescript client

   Switch to the `sechub-openapi-ts-client` directory and follow the instructions in the [README.md](../sechub-openapi-ts-client/README.md) to generate the SecHub OpenAPI client or run:

   ```shell
   cd ../../sechub-openapi-ts-client
    ./build-typescript-client.sh
    ```

3. Install the dependencies

    ```shell
    npm install
    ```

4. Install [VSCodium](https://vscodium.com/), [Eclipse Theia](https://theia-ide.org/) or [VSCode](https://code.visualstudio.com/)

   NOTE: VSCodium and Eclipse Theia distribute free/libre open source software binaries. VS Code, on the other hand, distributes non-free binaries and collects telemetry data.

5. In VSCodium toolbar: `Run -> Start Debugging`.

   ![image](README/start_debugging.png)

#### Develop with SecHub Integrationtest Server

1. Start the SecHub Server as integration test server from your IDE

2. Start the proxy in a terminal `node devProxy.js` (Proxy on http://localhost:8000 -> https://localhost:8443 (sechub int test serevr)
   This step is necessary because of self singed SSL certificates.

3. Run the extension in toolbar: `Run -> Start Debugging`.

4. Set SecHub Server URL to http://localhost:8000 and the credentials to the default credentials e.g. use the int-test_superadmin user

### Test

Prerequisite: The Node package manager NPM needs to be installed.

1. Build the Typescript client

   Switch to the `sechub-openapi-ts-client` directory and follow the instructions in the [README.md](../sechub-openapi-ts-client/README.md) to generate the SecHub OpenAPI client or run:

   ```shell
    cd ../../sechub-openapi-ts-client
    ./build-typescript-client.sh
    ```

2. Install dependencies (make sure you have generated and build t+he sechub-openapi-typescript client first)

    ```shell
    npm install
    ```

3. Compile and run tests

    ```shell
    npm test
    ```

   NOTE: The test automatically downloads and runs VS Code.

### Build

To build the plugin, run the following command in the project root directory:

   ```shell
   ./build-plugin.sh
   ```

## Further Information

### Contributing

We welcome any contributions.
If you want to contribute to this project, please read the [contributing guide](CONTRIBUTING.md).

### Code of Conduct

Please read our [Code of Conduct](https://github.com/mercedes-benz/foss/blob/master/CODE_OF_CONDUCT.md) as it is our base for interaction.

### License

This project is licensed under the [MIT LICENSE](./LICENSE).

### Provider Information

Please visit https://www.mercedes-benz-techinnovation.com/en/imprint/ for information on the provider.

Notice: Before you use the program in productive use, please take all necessary precautions,
e.g. testing and verifying the program with regard to your specific use.
The program was tested solely for our own use cases, which might differ from yours.

### Notice

Dependencies:

* Icons used from [VSCode codicons](https://github.com/microsoft/vscode-codicons): Copyright (c) Microsoft Corporation.