<!-- SPDX-License-Identifier: MIT --->
# SecHub Web UI

This project is a web application that provides a user interface for the SecHub API. It is built with [Vite](https://vitejs.dev/), [Vue 3](https://v3.vuejs.org/), and [Vuetify](https://vuetifyjs.com/en/).

## Usage

### Installation

Install the node version manager (nvm) and use it to install the correct version of Node.js:

```bash
nvm use
```

Install the project dependencies:

```bash
npm install
```

### Starting the Development Server

To start the development server with hot-reload, run the following command. The server will be accessible at [http://localhost:3000](http://localhost:3000):

```bash
npm run dev
```

> Add NODE_OPTIONS='--no-warnings' to suppress the JSON import warnings that happen as part of the Vuetify import mapping. If you are on Node [v21.3.0](https://nodejs.org/en/blog/release/v21.3.0) or higher, you can change this to NODE_OPTIONS='--disable-warning=5401'. If you don't mind the warning, you can remove this from your package.json dev script.

### Building for Production

To build your project for production, use:

```bash
npm run build
```

