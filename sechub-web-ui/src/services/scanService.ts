// SPDX-License-Identifier: MIT
import {
  SchedulerResult,
  SecHubConfiguration,
  UserApproveJobRequest,
  UserCreateNewJobRequest,
  UserUploadsBinariesWorkaroundRequest,
  UserUploadSourceCodeWorkaroundRequest,
} from 'sechub-openapi-ts-client'
import { createSha256Checksum } from '../utils/cryptoUtils'
import defaultClient from './defaultClient'
import i18n from '@/i18n'
import {
  UPLOAD_BINARIES_IDENTIFIER,
  UPLOAD_SOURCE_CODE_IDENTIFIER,
} from '@/utils/applicationConstants'
import { handleApiError } from './apiErrorHandler'

// Implements the scan of a file in three steps: creating a Job, uploading the data and approve the job
class ScanService {
  async scan (configuration: SecHubConfiguration, projectId: string, file: File): Promise<string[]> {
    const errorMessages: string[] = []
    try {
      const jobId = await this.createJob(configuration)
      if (jobId) {
        await this.uploadData(configuration, jobId, file, errorMessages)
        await this.approveJob(projectId, jobId, errorMessages)
      } else {
        errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_JOB_NOT_CREATED'))
      }
    } catch (error) {
      console.error('Scan failed:', error)
      handleApiError(error)
      errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_GENERIC'))
    }
    return errorMessages
  }

  private async createJob (configuration: SecHubConfiguration): Promise<string | undefined> {
    const requestParameters: UserCreateNewJobRequest = {
      projectId: configuration.projectId,
      secHubConfiguration: configuration,
    }

    try {
      const result: SchedulerResult = await defaultClient.withExecutionApi.userCreateNewJob(requestParameters)
      return result.jobId
    } catch (error) {
      console.error('Job creation failed:', error)
      handleApiError(error)
      return undefined
    }
  }

  private async uploadData (configuration: SecHubConfiguration, jobId: string, file: File, errorMessages: string[]) {
    const checksum: string = await createSha256Checksum(file)

    // sourcode upload
    if (this.containsString(configuration, UPLOAD_SOURCE_CODE_IDENTIFIER)) {
      const requestParameters: UserUploadSourceCodeWorkaroundRequest = {
        projectId: configuration.projectId,
        jobUUID: jobId,
        checkSum: checksum,
        file,
      }

      try {
        await defaultClient.withExecutionApi.userUploadSourceCode(requestParameters)
      } catch (error) {
        console.error('Source code upload failed:', error)
        handleApiError(error)
        errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_SOURCE_UPLOAD_FAILED'))
        errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_DOWNLOAD_CLIENT'))
      }

    // binary upload
    } else if (this.containsString(configuration, UPLOAD_BINARIES_IDENTIFIER)) {
      const size: string = file.size.toString()
      const requestParameters: UserUploadsBinariesWorkaroundRequest = {
        projectId: configuration.projectId,
        jobUUID: jobId,
        checkSum: checksum,
        xFileSize: size,
        file,
      }

      try {
        await defaultClient.withExecutionApi.userUploadsBinaries(requestParameters)
      } catch (error) {
        console.error('Binary upload failed:', error)
        handleApiError(error)
        errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_BINARY_UPLOAD_FAILED'))
        errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_DOWNLOAD_CLIENT'))
      }
    } else {
      errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_CONFIGURATION_ERROR'))
    }
  }

  private containsString (config: SecHubConfiguration, searchString: string): boolean {
    const jsonString = JSON.stringify(config)
    return jsonString.includes(searchString)
  }

  private async approveJob (projectId: string, jobId: string, errorMessages: string[]) {
    const requestParameters: UserApproveJobRequest = {
      projectId,
      jobUUID: jobId,
    }

    try {
      await defaultClient.withExecutionApi.userApproveJob(requestParameters)
    } catch (error) {
      console.error('Job approval failed:', error)
      handleApiError(error)
      errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_JOB_NOT_APPROVED'))
    }
  }
}

export default new ScanService()
