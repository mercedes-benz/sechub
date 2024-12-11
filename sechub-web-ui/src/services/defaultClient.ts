// SPDX-License-Identifier: MIT
import configurationApi from './configurationService'
import projectApi from './productAdministrationService'
import systemApi from './systemApiService'

const defaultClient = {
  withProjectApi: projectApi,
  withSystemApi: systemApi,
  withConfigurationApi: configurationApi,

}

export default defaultClient
