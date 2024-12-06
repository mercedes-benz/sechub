// SPDX-License-Identifier: MIT
export const CONFIG = {
  HOST: String(import.meta.env.VITE_API_HOST) || '',
  USERNAME: String(import.meta.env.VITE_API_USER) || '',
  PASSWORD: String(import.meta.env.VITE_API_PASSWORD) || '',
  LOCAL_DEV: String(import.meta.env.VITE_API_LOCAL_DEV) || '',
}
