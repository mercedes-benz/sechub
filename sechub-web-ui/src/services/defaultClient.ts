// SPDX-License-Identifier: MIT
import apiConfig from './configuration'
import executionApi from './executionService/executionService'
import scanService from './executionService/ScanService'
import {
  ConfigurationApi,
  JobManagementApi,
  OtherApi,
  ProjectAdministrationApi,
  SecHubExecutionApi,
  SignUpApi,
  SystemApi,
  UserSelfServiceApi,
} from '@/generated-sources/openapi'

const otherApi = new OtherApi(apiConfig)
const jobManagementApi = new JobManagementApi(apiConfig)
const sechubExecutionApi = new SecHubExecutionApi(apiConfig)
const configurationApi = new ConfigurationApi(apiConfig)
const userSelfServiceApi = new UserSelfServiceApi(apiConfig)
const systemApi = new SystemApi(apiConfig)
const signUpApi = new SignUpApi(apiConfig)
const projectApi = new ProjectAdministrationApi(apiConfig)

const defaultClient = {
  withProjectApi: projectApi,
  withSignUpApi: signUpApi,
  withUserSelfServiceApi: userSelfServiceApi,
  withSystemApi: systemApi,
  withConfigurationApi: configurationApi,
  withOtherApi: otherApi,
  withExecutionApi: executionApi,
  withSechubExecutionApi: sechubExecutionApi,
  withScanService: scanService,
  withJobManagementApi: jobManagementApi,
}

export default defaultClient
