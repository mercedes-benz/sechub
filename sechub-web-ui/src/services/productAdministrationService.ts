// SPDX-License-Identifier: MIT
import { ProjectAdministrationApi } from '@/generated-sources/openapi'
import apiConfig from './configuration'

const projectApi = new ProjectAdministrationApi(apiConfig)

export default projectApi
