<!-- SPDX-License-Identifier: MIT --->
# SecHub Web UI

This project is a web application that provides a user interface for the SecHub API. It is built with [Vite](https://vitejs.dev/), [Vue 3](https://v3.vuejs.org/), and [Vuetify](https://vuetifyjs.com/en/).

## Usage

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

### Create a local configuration (.env)

Create a `.env` file in the sechub-webui directory

```bash
VITE_API_HOST=http://localhost:3000
VITE_API_USER=<your-api-user>
VITE_API_PASSWORD=<your-api-password>
VITE_API_LOCAL_DEV=true
```

### Building openAPI SecHub Client

To generate the SecHub openAPI Client use:

```bash
npm run generate-api-client
 ```

### Starting the Development Server

To start the development server with hot-reload, run the following command. The server will be accessible at [http://localhost:3000](http://localhost:3000):

```bash
npm run dev
```

#### Running in Development mode with SecHub Integrationtest Server and PDS Integrationtest Server (using Mocked scan products)
> Only useful If you want to get mocked scan results
1. Follow the steps above
2. Start the integration test PDS  
(for the correct run configuration follow the [developer guide](https://mercedes-benz.github.io/sechub/latest/sechub-developer-quickstart-guide.html#run-integration-tests-from-ide))
3. (Optional) Initial setup: execute /test-setups/setup-integration-test-server.sh

#### Running in Development mode with SecHub Server and PDS as Docker Container
> Only useful If you want to get real scan results
1. Start the SecHub Server as Docker Container (see sechub-solution/01-...)
2. Start the required PDS as Docker (e.g. sechub-pds-solutions/gosec/05-...)
3. Set up PDS in sechub-solution/setups/ e.g. setup-gosec.sh
4. Make sure your user is assigned to the project you want to scan

Now you can test your web-ui with sechub and real scans!

### Building for Production

Set Environment Variables:
Be aware that `npm run build` sets the environment variables at build time.
For deploying runtime ENV please se sechub-web-ui-solution/docker/nginx/conf.json it will override the VITE variables. Be aware that the config.json will be served by nginx.

To build your project for production, use:

```bash
npm run build
```
