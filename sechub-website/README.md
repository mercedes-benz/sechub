<!-- SPDX-License-Identifier: MIT --->
# Nuxt 3 Minimal Starter

Look at the [Nuxt 3 documentation](https://nuxt.com/docs/getting-started/introduction) to learn more.

## Setup

Make sure to install the dependencies:

```bash
npm install
```

## Development Server

Start the development server on `http://localhost:3000`:

```bash
npm run dev
```

## Production

Build the application for production:

```bash
npm run build
```

### Workaround for local preview:

If you want to test the website in production mode, please modify the `nuxt.config.ts` and adjust this line:
```ts:
const baseURL = process.env.NODE_ENV === 'development' ? '' : '/sechub';
```
to:
```ts
const baseURL = process.env.NODE_ENV === 'development' ? '' : '';
```

Then you can build the application and run the production preview server:

```bash
npm run preview
```

Check out the [deployment documentation](https://nuxt.com/docs/getting-started/deployment) for more information.
