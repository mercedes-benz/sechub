// SPDX-License-Identifier: MIT
import {
  SchedulerResult,
  SecHubCodeScanConfiguration,
  SecHubConfiguration,
  SecHubDataConfiguration,
  SecHubFileSystemConfiguration,
  SecHubSecretScanConfiguration,
  UserApproveJobRequest,
  UserCreateNewJobRequest,
} from '@/generated-sources/openapi'
import defaultClient from '@/services/defaultClient'
import { createSha256Checksum } from './cryptoUtils'
import { UserUploadsBinariesWorkaroundRequest, UserUploadSourceCodeWorkaroundRequest } from '@/services/executionService'
import i18n from '@/i18n'

export function buildSecHubConfiguration (scanTypes: string[], uploadFile: File, fileType: string, projectId: string): SecHubConfiguration {
  const UNIQUE_NAME = 'refName'
  console.log(scanTypes)
  const fileSystemConfig: SecHubFileSystemConfiguration = {
    files: [uploadFile.name],
  }

  const dataConfiguration: SecHubDataConfiguration = {
    sources: fileType === 'sources' ? [{ name: UNIQUE_NAME, fileSystem: fileSystemConfig }] : undefined,
    binaries: fileType === 'binaries' ? [{ name: UNIQUE_NAME, fileSystem: fileSystemConfig }] : undefined,
  }

  const codeScanConfiguration: SecHubCodeScanConfiguration = {}
  const secretScanConfiguration: SecHubSecretScanConfiguration = {}

  if (scanTypes.includes('codeScan')) {
    codeScanConfiguration.use = [UNIQUE_NAME]
  }

  if (scanTypes.includes('secretScan')) {
    secretScanConfiguration.use = [UNIQUE_NAME]
  }

  const config: SecHubConfiguration = {
    apiVersion: '1.0',
    projectId,
    data: dataConfiguration,
  }

  // adding scan types to configuration
  if (codeScanConfiguration.use) {
    config.codeScan = codeScanConfiguration
  }

  if (secretScanConfiguration.use) {
    config.secretScan = secretScanConfiguration
  }

  return config
}

export async function scan (configuration: SecHubConfiguration, projectId: string, file: File) : Promise<string[]> {
  const errorMessages : string[] = []

  try {
    const jobId = await createJob(configuration)
    if (jobId) {
      await uploadData(configuration, jobId, file, errorMessages)
      await approveJob(projectId, jobId, errorMessages)
    } else {
      errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_JOB_NOT_CREATED'))
    }
  } catch (error) {
    console.error(error)
  }
  return errorMessages
}

async function createJob (configuration: SecHubConfiguration): Promise<string | undefined> {
  let jobId: string | undefined = ''

  const requestParameters: UserCreateNewJobRequest = {
    projectId: configuration.projectId,
    secHubConfiguration: configuration,
  }

  try {
    const result: SchedulerResult = await defaultClient.withExecutionApi.userCreateNewJob(requestParameters)
    jobId = result.jobId
    return jobId
  } catch (error) {
    console.error(error)
    return undefined
  }
}

async function uploadData (configuration: SecHubConfiguration, jobId: string, file: File, errorMessages: string[]) {
  const checksum : string = await createSha256Checksum(file)

  if (configuration.data?.sources) {
    const requestParameters: UserUploadSourceCodeWorkaroundRequest = {
      projectId: configuration.projectId,
      jobUUID: jobId,
      checkSum: checksum,
      file,
    }

    try {
      await defaultClient.withExecutionApi.userUploadSourceCode(requestParameters)
    } catch (error) {
      console.error(error)
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
      await defaultClient.withExecutionApi.userUploadsBinaries(requestParameters)
    } catch (error) {
      console.error(error)
      errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_BINARY_UPLOAD_FAILED'))
    }
  } else {
    errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_NO_DATA_SECTION'))
  }
}

async function approveJob (projectId: string, jobId: string, errorMessages: string[]) {
  const requestParameters: UserApproveJobRequest = {
    projectId,
    jobUUID: jobId,
  }

  try {
    await defaultClient.withExecutionApi.userApproveJob(requestParameters)
  } catch (error) {
    console.error(error)
    errorMessages.push(i18n.global.t('SCAN_ERROR_ALERT_JOB_NOT_APPROVED'))
  }
}
