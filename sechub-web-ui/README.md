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

> If you receive an empty page, do a reload (sometimes it needs a little bit of time until everything is setup correctly)

> Add NODE_OPTIONS='--no-warnings' to suppress the JSON import warnings that happen as part of the Vuetify import mapping. If you are on Node [v21.3.0](https://nodejs.org/en/blog/release/v21.3.0) or higher, you can change this to NODE_OPTIONS='--disable-warning=5401'. If you don't mind the warning, you can remove this from your package.json dev script.

#### Running in Development mode with sechub for testing

1. Start SecHub Integration Test Server (or Docker Server)
2. Configure your `.env.local` file by copying `.env` to `.env.local` and adjusting the variables as needed.
Set `VITE_API_LOCAL_DEV=true`
Set `VITE_API_USER` to your SecHub user
Set `VITE_API_PASSWORD` to your SecHub Api Token
3. Set `VITE_API_HOST` to the URL of your application http://localhost:3000 - this is because of the proxy defined in the Vite dev server to avoid CORS Issues
4. Start the SPA in Development mode (npm run dev)

Happy Testing!

### Building for Production

Set Environment Variables:
Be aware that `npm run build` sets the environment variables at build time.
For deploying runtime ENV please se sechub-web-ui-solution/docker/nginx/conf.json it will override the VITE variables. Be aware that the config.json will be served by nginx.

To build your project for production, use:

```bash
npm run build
```
