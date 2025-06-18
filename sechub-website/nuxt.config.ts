// SPDX-License-Identifier: MIT
// https://nuxt.com/docs/api/configuration/nuxt-config

const baseURL = process.env.NODE_ENV === 'development' ? '' : '/sechub/';

export default defineNuxtConfig({
  app: {
    baseURL,
    head: {
      htmlAttrs: { lang: 'en' },
      title: 'SecHub',
      link: [
        { rel: 'icon', type: 'image/svg+xml', href: `${baseURL}favicon.ico` }
      ]
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