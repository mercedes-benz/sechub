// SPDX-License-Identifier: MIT
import configurationApi from './configurationService'
import projectApi from './productAdministrationService'
import systemApi from './systemApiService'
import otherApi from './otherService'
import executionApi from './executionService/executionService'
import scanService from './executionService/ScanService'

const defaultClient = {
  withProjectApi: projectApi,
  withSystemApi: systemApi,
  withConfigurationApi: configurationApi,
  withOtherApi: otherApi,
  withExecutionApi: executionApi,
  withScanService: scanService,
}

export default defaultClient
