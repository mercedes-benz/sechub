// SPDX-License-Identifier: MIT
import configurationApi from './configurationService'
import projectApi from './productAdministrationService'
import systemApi from './systemApiService'
import otherApi from './otherService'
import executionApi from './executionService'

const defaultClient = {
  withProjectApi: projectApi,
  withSystemApi: systemApi,
  withConfigurationApi: configurationApi,
  withOtherApi: otherApi,
  withExecutionApi: executionApi,
}

export default defaultClient
