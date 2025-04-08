<!-- SPDX-License-Identifier: MIT --->
# SecHub Web UI

This project is a web application that provides a user interface for the SecHub API. It is built with [Vite](https://vitejs.dev/), [Vue 3](https://v3.vuejs.org/), and [Vuetify](https://vuetifyjs.com/en/).

## Prerequisites

### Installation

Install the node version manager (nvm) and use it to install and use the correct version of Node.js:
You can skip the installation processes if you already have nvm installed and the node version in nvm.

```bash
nvm install
```

```bash
nvm use
```

Install the project dependencies:

```bash
npm install
```

### Building openAPI SecHub Client

To generate the SecHub openAPI Client use:

```bash
npm run generate-api-client
 ```

## Usage

### Running local development server with SecHub Integrationtest Server

#### Create a local configuration (.env) with Basic Auth 

Create a `.env` file in the sechub-webui directory

```bash
VITE_API_USERNAME=<your-api-user>
VITE_API_PASSWORD=<your-api-password>
VITE_API_BASIC_AUTH_DEV=true
```

#### Start the Development Server

To start the development server with hot-reload, run the following command. The server will be accessible at [http://localhost:3000](http://localhost:3000):

```bash
npm run dev
```

#### Start SecHub Integrationtest Server

If you want to get Server responses, start the SecHub integrationtest Server e.g. via your IDE see the [developer guide](https://mercedes-benz.github.io/sechub/latest/sechub-developer-quickstart-guide.html#run-integration-tests-from-ide) for instructions.
Use the SecHub user and password for your basic auth .env file.
You can use the sechub-api.sh script to manage your user and project.

### Running local development server with SecHub Integrationtest Server and PDS Integrationtest Server (Mocked Products)
> Only useful If you want to get mocked scan results
1. Follow all steps above
2. Start the integration test PDS  
(for the correct run configuration follow the [developer guide](https://mercedes-benz.github.io/sechub/latest/sechub-developer-quickstart-guide.html#run-integration-tests-from-ide))
3. (Optional) Initial setup: execute `test-setups/integrationtest-setups/setup.sh`. Make sure the .env file contains the correct values for basic auth.

### Running local development server with SecHub Docker Container and PDS Docker Container (Real Products)
> Only useful If you want to get real scan results
1. Start the SecHub Server as Docker Container (see sechub-solution/01-...)
2. Start the required PDS as Docker (e.g. sechub-pds-solutions/gosec/05-...)
3. (Optional) Initial setup: execute `/test-setups/docker/setup.sh` for gosec and gitleaks (Make sure PDS are running)


Now you can test your web-ui with sechub and real scans!

### Building for Production

## Environment Variables
Be aware that `npm run build` sets the environment variables at build time.
For deploying runtime ENV please se sechub-web-ui-solution/docker/nginx/conf.json it will override the VITE variables. Be aware that the config.json will be served by nginx.

Please see the config.ts for possible ENV variables that can be set.

Example config.json

Custom user support email address 

```json
{
    "SECHUB_USER_SUPPORT_EMAIL": "myusersupport@example.org",
    "SECHUB_USER_SUPPORT_WEBSITE": "https:\\mycustomwebsit.example.org"
}
```

## build project for production

```bash
npm run build
```
