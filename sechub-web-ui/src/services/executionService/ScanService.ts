// SPDX-License-Identifier: MIT
import {
  SchedulerResult,
  SecHubConfiguration,
  UserApproveJobRequest,
  UserCreateNewJobRequest,
} from '@/generated-sources/openapi'
import executionApi from './executionService'
import { createSha256Checksum } from '../../utils/cryptoUtils'
import { UserUploadsBinariesWorkaroundRequest, UserUploadSourceCodeWorkaroundRequest } from '@/services/executionService/executionService'
import i18n from '@/i18n'

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
      const result: SchedulerResult = await executionApi.userCreateNewJob(requestParameters)
      return result.jobId
    } catch (error) {
      console.error('Job creation failed:', error)
      return undefined
    }
  }

  private async uploadData (configuration: SecHubConfiguration, jobId: string, file: File, errorMessages: string[]) {
    const checksum: string = await createSha256Checksum(file)

    if (configuration.data?.sources) {
      const requestParameters: UserUploadSourceCodeWorkaroundRequest = {
        projectId: configuration.projectId,
        jobUUID: jobId,
        checkSum: checksum,
        file,
      }

      try {
        await executionApi.userUploadSourceCode(requestParameters)
      } catch (error) {
        console.error('Source code upload failed:', error)
        errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_SOURCE_UPLOAD_FAILED'))
      }
    } else if (configuration.data?.binaries) {
      const size: string = file.size.toString()
      const requestParameters: UserUploadsBinariesWorkaroundRequest = {
        projectId: configuration.projectId,
        jobUUID: jobId,
        checkSum: checksum,
        xFileSize: size,
        file,
      }

      try {
        await executionApi.userUploadsBinaries(requestParameters)
      } catch (error) {
        console.error('Binary upload failed:', error)
        errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_BINARY_UPLOAD_FAILED'))
      }
    } else {
      errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_NO_DATA_SECTION'))
    }
  }

  private async approveJob (projectId: string, jobId: string, errorMessages: string[]) {
    const requestParameters: UserApproveJobRequest = {
      projectId,
      jobUUID: jobId,
    }

    try {
      await executionApi.userApproveJob(requestParameters)
    } catch (error) {
      console.error('Job approval failed:', error)
      errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_JOB_NOT_APPROVED'))
    }
  }
}

export default new ScanService()
