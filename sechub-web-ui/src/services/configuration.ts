// SPDX-License-Identifier: MIT
import { useConfig } from '@/config'
import { Configuration } from '@/generated-sources/openapi'

let apiConfig: Configuration
const config = useConfig()

if (config.value.LOCAL_DEV) {
  // api configuration for local development with basic auth
  apiConfig = new Configuration({
    basePath: config.value.HOST,
    username: config.value.USERNAME,
    password: config.value.PASSWORD,
    headers: {
      'Content-Type': 'application/json',
    },
  })
} else {
  apiConfig = new Configuration({
    basePath: config.value.HOST,
    headers: {
      // todo: check if cookie set by server is sent back (for auth reasons)
      'Content-Type': 'application/json',
    },
  })
}

export default apiConfig
