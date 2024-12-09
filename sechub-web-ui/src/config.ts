// SPDX-License-Identifier: MIT

import { ref } from 'vue'

const config = ref({
  HOST: String(import.meta.env.VITE_API_HOST) || '',
  USERNAME: String(import.meta.env.VITE_API_USER) || '',
  PASSWORD: String(import.meta.env.VITE_API_PASSWORD) || '',
  LOCAL_DEV: String(import.meta.env.VITE_API_LOCAL_DEV) || '',
})

// Ovverrides local environment variables when project is compiled
// Takes variables from config.json if available
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
