// SPDX-License-Identifier: MIT
// https://nuxt.com/docs/api/configuration/nuxt-config
// todo why different base URL -> sechub?
const baseURL = process.env.NODE_ENV === 'development' ? '' : '';

export default defineNuxtConfig({
  app: {
    baseURL,
    head: {
      htmlAttrs: { lang: 'en' },
      title: 'SecHub'
    }
  },

  devtools: { enabled: true },
  modules: [
      '@nuxtjs/tailwindcss',
    'nuxt-security',
    'nuxt-headlessui'
  ],
  compatibilityDate: '2024-12-05',
  postcss: {
    plugins: {
      tailwindcss: {},
      autoprefixer: {},
    },
  },
});