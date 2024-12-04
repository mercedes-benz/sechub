// SPDX-License-Identifier: MIT
import projectApi from './productAdministrationService'
import systemApi from './systemApiService'

const defaultClient = {
  withProjectApi: projectApi,
  withSystemApi: systemApi,
}

export default defaultClient