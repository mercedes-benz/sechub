// SPDX-License-Identifier: MIT
import { ConfigurationApi } from '@/generated-sources/openapi'
import apiConfig from './configuration'

const configurationApi = new ConfigurationApi(apiConfig)

export default configurationApi
