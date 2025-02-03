// SPDX-License-Identifier: MIT

import { ref } from 'vue'

const config = ref({
  // New ENV must be defined in global.d.ts
  HOST: String(import.meta.env.VITE_API_HOST) || 'http://localhost:3000',
  USERNAME: String(import.meta.env.VITE_API_USER) || '',
  PASSWORD: String(import.meta.env.VITE_API_PASSWORD) || '',
  LOCAL_DEV: Boolean(import.meta.env.VITE_API_LOCAL_DEV === 'true') || false,
})

// Overrides local environment variables after project compilation
// Utilizes variables from config.json if available
export async function loadConfig () {
  try {
    const response = await fetch('/config.json')
    const runtimeConfig = await response.json()
    config.value = { ...config.value, ...runtimeConfig }
  } catch (error) {
    console.error('Failed to load configuration, using fallback in .env:', error)
  }
}

export function useConfig () {
  return config
}
