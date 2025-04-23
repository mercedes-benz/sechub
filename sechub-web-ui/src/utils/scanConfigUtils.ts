// SPDX-License-Identifier: MIT
import {
  SecHubCodeScanConfiguration,
  SecHubConfiguration,
  SecHubSecretScanConfiguration,
} from '@/generated-sources/openapi'

import {
  CODE_SCAN_IDENTIFIER,
  FILETYPE_BINARIES,
  FILETYPE_SOURCES,
  SECRET_SCAN_IDENTIFER,
  UPLOAD_BINARIES_IDENTIFIER,
  UPLOAD_SOURCE_CODE_IDENTIFIER,
} from './applicationConstants'

import { useConfig } from '@/config'
import i18n from '@/i18n'

export function buildSecHubConfiguration (scanTypes: string[], fileType: string, projectId: string): SecHubConfiguration {
  const UNIQUE_NAME : string = getUniqueName(fileType)

  const config: SecHubConfiguration = {
    apiVersion: '1.0',
    projectId,
  }

  if (scanTypes.includes(CODE_SCAN_IDENTIFIER)) {
    const codeScanConfiguration: SecHubCodeScanConfiguration = {}
    codeScanConfiguration.use = [UNIQUE_NAME]
    config.codeScan = codeScanConfiguration
  }

  if (scanTypes.includes(SECRET_SCAN_IDENTIFER)) {
    const secretScanConfiguration: SecHubSecretScanConfiguration = {}
    secretScanConfiguration.use = [UNIQUE_NAME]
    config.secretScan = secretScanConfiguration
  }

  return config
}

function getUniqueName (fileType: string): string {
  if (fileType === FILETYPE_BINARIES) {
    return UPLOAD_BINARIES_IDENTIFIER
  } else if (fileType === FILETYPE_SOURCES) {
    return UPLOAD_SOURCE_CODE_IDENTIFIER
  } else {
    throw new Error(`Unknown fileType: ${fileType}`)
  }
}

export function isFileSizeValid (file: File, fileType: string) {
  const config = useConfig()
  const UNIQUE_NAME : string = getUniqueName(fileType)

  let maxBytes
  let errorMessage

  if (UNIQUE_NAME === UPLOAD_BINARIES_IDENTIFIER) {
    maxBytes = config.value.SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES
    errorMessage = i18n.global.t('SCAN_ERROR_UPLOAD_TOO_BIG_BINARIES') + maxBytes + ' bytes'
  } else if (UNIQUE_NAME === UPLOAD_SOURCE_CODE_IDENTIFIER) {
    maxBytes = config.value.SECHUB_UPLOAD_SOURCES_MAXIMUM_BYTES
    errorMessage = i18n.global.t('SCAN_ERROR_UPLOAD_TOO_BIG_SOURCECODE') + maxBytes + ' bytes'
  } else {
    errorMessage = (i18n.global.t('SCAN_ERROR_ALERT_CONFIGURATION_ERROR'))
  }

  // If maxBytes is undefined or -1, assume no limit
  if (maxBytes === undefined || maxBytes === -1) {
    return { errorMessage, isValid: true }
  }

  const fileSize = file.size
  if (fileSize > maxBytes) {
    return { errorMessage, isValid: false }
  }
  return { errorMessage, isValid: true }
}
