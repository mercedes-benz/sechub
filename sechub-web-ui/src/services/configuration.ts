// SPDX-License-Identifier: MIT
import { useConfig } from '@/config'
import { Configuration } from 'sechub-openapi-ts-client'

let apiConfig: Configuration
const config = useConfig()

if (config.value.BASIC_AUTH_DEV) {
  // api configuration for local development with basic auth
  apiConfig = new Configuration({
    basePath: '',
    username: config.value.USERNAME,
    password: config.value.PASSWORD,
    headers: {
      'Content-Type': 'application/json',
    },
  })
} else {
  apiConfig = new Configuration({
    basePath: '',
    headers: {
      'Content-Type': 'application/json',
    },
  })
}
if (!apiConfig) {
  throw Error('No api configuration available!')
}
export default apiConfig
