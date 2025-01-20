// SPDX-License-Identifier: MIT
import { SignUpApi } from '@/generated-sources/openapi'
import apiConfig from './configuration'

const signUpApi = new SignUpApi(apiConfig)

export default signUpApi
