<!-- SPDX-License-Identifier: MIT --->
# Nuxt 3 Minimal Starter

Look at the [Nuxt 3 documentation](https://nuxt.com/docs/getting-started/introduction) to learn more.

## Setup

Make sure to install the dependencies:

```bash
# npm (initial build with npm 10.8.3)
npm install

# pnpm
pnpm install

# yarn
yarn install

# bun
bun install
```

## Development Server

Start the development server on `http://localhost:3000`:

```bash
# npm
npm run dev
```

## Production

Build the application for production:

```bash
# npm
npm run build
```

Locally preview production build:

```bash
# npm
npm run preview
```

Check out the [deployment documentation](https://nuxt.com/docs/getting-started/deployment) for more information.

## Deployment

### Entrypoint

```bash
# node (tested with node v22.9.0)
node .output/server/index.mjs
```

### Port and Host

listening to default environment variables 'HOST' (default: 0.0.0.0) and PORT (default 3000)