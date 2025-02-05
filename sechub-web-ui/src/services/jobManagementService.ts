// SPDX-License-Identifier: MIT
import { JobManagementApi } from '@/generated-sources/openapi'
import apiConfig from './configuration'

const jobManagementApi = new JobManagementApi(apiConfig)

export default jobManagementApi
