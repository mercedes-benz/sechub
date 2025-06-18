// SPDX-License-Identifier: MIT
// https://nuxt.com/docs/api/configuration/nuxt-config

export default defineNuxtConfig({
  app: {
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