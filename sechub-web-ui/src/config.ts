// SPDX-License-Identifier: MIT

import { ref } from 'vue'

const config = ref({
  // New ENV must be defined in global.d.ts
  USERNAME: import.meta.env.VITE_API_USERNAME || undefined,
  PASSWORD: import.meta.env.VITE_API_PASSWORD || undefined,
  BASIC_AUTH_DEV: import.meta.env.VITE_API_BASIC_AUTH_DEV === 'true' || false,
  SECHUB_USER_SUPPORT_EMAIL: import.meta.env.VITE_SECHUB_USER_SUPPORT_EMAIL || 'example@example.org',
  SECHUB_USER_SUPPORT_WEBSITE: import.meta.env.VITE_SECHUB_USER_SUPPORT_WEBSITE || 'https://sechub.example.org',
  SECHUB_FAQ_LINK: import.meta.env.VITE_SECHUB_FAQ_LINK || 'https://sechub.example.org/faq',
  // maximum file upload size (-1 == unlimited)
  SECHUB_UPLOAD_SOURCES_MAXIMUM_BYTES: import.meta.env.VITE_SECHUB_UPLOAD_SOURCES_MAXIMUM_BYTES || -1, // 300000 is default integration test server
  SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES: import.meta.env.VITE_SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES || -1, // 409600 is default integration test server
})

// Overrides local environment variables after project compilation
// Utilizes variables from config.json if available
export async function loadConfig () {
  try {
    const response = await fetch('/config.json')
    const runtimeConfig = await response.json()
    config.value = { ...config.value, ...runtimeConfig }
  } catch (error) {
    console.error('Failed to load configuration, using default values.', error)
  }
}

export function useConfig () {
  return config
}
