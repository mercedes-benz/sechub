// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  // Disable server-side rendering (https://go.nuxtjs.dev/ssr-mode)
  ssr: false,
  app: {
    head: {
      title: 'SecHub',
      titleTemplate: '%s | Managed Service'
    }
  },
  $development: {
    app: {
      head: {
        title: 'SecHub'
      }
    }
  },

  compatibilityDate: '2024-04-03',
  devtools: { enabled: true }
})
