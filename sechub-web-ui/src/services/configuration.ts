// SPDX-License-Identifier: MIT
import { CONFIG } from '@/config'
import { Configuration } from '@/generated-sources/openapi'

let apiConfig: Configuration

if (CONFIG.LOCAL_DEV) {
  // api configuration for local development with basic auth
  apiConfig = new Configuration({
    basePath: CONFIG.HOST,
    username: CONFIG.USERNAME,
    password: CONFIG.PASSWORD,
    headers: {
      'Content-Type': 'application/json',
    },
  })
} else {
  apiConfig = new Configuration({
    basePath: CONFIG.HOST,
    headers: {
      // todo: check if cookie set by server is sent back (for auth reasons)
      // todo: check if env work without vite server (PROD mode)
      'Content-Type': 'application/json',
    },
  })
}

export default apiConfig
