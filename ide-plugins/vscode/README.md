<!--- SPDX-License-Identifier: MIT -->

# SecHub VSCode/VSCodium/Eclipse Theia plugin

This is an VSCode/VSCodium/Eclipse Theia plugin for a convenient IDE integration of [SecHub](https://github.com/mercedes-benz/sechub).

## Features

* Read and navigate through SecHub reports
* Supported modules: `codeScan` and `secretScan`

## Installation

Recommended: Install the plugin from the Open-VSX marketplace from within [VSCodium](https://vscodium.com/) or [Eclipse Theia](https://theia-ide.org/) by searching for the term: `sechub` in the `Extensions` manager.

For VS Code you need to download the [plugin](https://open-vsx.org/extension/mercedes-benz/sechub) and install it manually. It is also possible to install the plugin manually in VSCodium and Eclipse Theia.

NOTE: Please use the new plugin from Mercedes-Benz: <https://open-vsx.org/extension/mercedes-benz/sechub>. The old Daimler plugin will be deprecated. The reason for the deprecation is the rebranding of Daimler to Mercedes-Benz.

## Development

### Develop

Note: Example extension to orientate: https://github.com/gitkraken/vscode-gitlens/tree/main/src 

1. Install Node.js

    * [Windows, macOS and Linux](https://nodejs.org/en/download)
    * [`deb` and `rpm` packages (Debian/Ubuntu, RHEL/Fedora etc.)](https://github.com/nodesource/distributions/tree/master)
    * [Node.js releases](https://nodejs.dev/en/about/releases/)

2. Build the Typescript client

   Switch to the `sechub-openapi-ts-client` directory and follow the instructions in the [README.md](../sechub-openapi-ts-client/README.md) to generate the SecHub OpenAPI client.

3. Install the dependencies

    ~~~
    npm install
    ~~~
   
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

   Switch to the `sechub-openapi-ts-client` directory and follow the instructions in the [README.md](../sechub-openapi-ts-client/README.md) to generate the SecHub OpenAPI client.

2. Install dependencies (make sure you have generated and build t+he sechub-openapi-typescript client first)

    ~~~
    npm install
    ~~~

3. Compile and run tests

    ~~~
    npm test
    ~~~

    NOTE: The test automatically downloads and runs VS Code.

### Build

1. Install the vsce cli tool

    ~~~
    npm install -g @vscode/vsce

    # or on Linux

    sudo npm install -g @vscode/vsce
    ~~~

2. Build the plugin

    ~~~
    vsce package
    ~~~

## Contributing

We welcome any contributions.
If you want to contribute to this project, please read the [contributing guide](CONTRIBUTING.md).

## Code of Conduct

Please read our [Code of Conduct](https://github.com/mercedes-benz/foss/blob/master/CODE_OF_CONDUCT.md) as it is our base for interaction.

## License

This project is licensed under the link:LICENSE[MIT LICENSE].

## Provider Information

Please visit https://www.mercedes-benz-techinnovation.com/en/imprint/ for information on the provider.

Notice: Before you use the program in productive use, please take all necessary precautions,
e.g. testing and verifying the program with regard to your specific use.
The program was tested solely for our own use cases, which might differ from yours.
