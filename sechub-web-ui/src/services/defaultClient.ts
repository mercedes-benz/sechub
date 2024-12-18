// SPDX-License-Identifier: MIT
import configurationApi from './configurationService'
import projectApi from './productAdministrationService'
import systemApi from './systemApiService'
import otherApi from './otherService'

const defaultClient = {
  withProjectApi: projectApi,
  withSystemApi: systemApi,
  withConfigurationApi: configurationApi,
  withOtherApi: otherApi,
}

export default defaultClient
