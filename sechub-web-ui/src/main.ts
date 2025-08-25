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
import { VCodeBlock } from '@wdns/vue-code-block'

// Composables
import { createApp } from 'vue'

loadConfig().then(() => {
  const app = createApp(App)

  registerPlugins(app)
  app.component('VCodeBlock', VCodeBlock)

  app.mount('#app')
})
