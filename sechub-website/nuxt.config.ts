// SPDX-License-Identifier: MIT
// https://nuxt.com/docs/api/configuration/nuxt-config
const baseURL = process.env.NODE_ENV === 'development' ? '' : '/sechub';

export default defineNuxtConfig({
  app: {
    baseURL,
    head: {
      htmlAttrs: { lang: 'en' },
      title: 'SecHub'
    }
  },

  devtools: { enabled: true },
  modules: ['@nuxtjs/tailwindcss', 'nuxt-security'],
  compatibilityDate: '2024-12-05'
});