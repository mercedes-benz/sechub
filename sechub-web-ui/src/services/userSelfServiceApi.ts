// SPDX-License-Identifier: MIT
import { UserSelfServiceApi } from '@/generated-sources/openapi'
import apiConfig from './configuration'

const userSelfServiceApi = new UserSelfServiceApi(apiConfig)

export default userSelfServiceApi
