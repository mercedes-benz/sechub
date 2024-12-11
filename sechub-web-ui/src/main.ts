// SPDX-License-Identifier: MIT
/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// Plugins
import { registerPlugins } from '@/plugins'
import { loadConfig } from './config'

// Components
import App from './App.vue'

// Composables
import { createApp } from 'vue'

loadConfig().then(() => {
  const app = createApp(App)

  registerPlugins(app)

  app.mount('#app')
})
