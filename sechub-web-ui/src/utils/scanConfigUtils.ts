// SPDX-License-Identifier: MIT
import {
  SecHubCodeScanConfiguration,
  SecHubConfiguration,
  SecHubSecretScanConfiguration,
} from '@/generated-sources/openapi'

import {
  CODE_SCAN_IDENTIFIER,
  SECRET_SCAN_IDENTIFER,
  UPLOAD_BINARIES_IDENTIFIER,
  UPLOAD_SOURCE_CODE_IDENTIFIER,
} from './applicationConstants'

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
  if (fileType === 'binaries') {
    return UPLOAD_BINARIES_IDENTIFIER
  } else if (fileType === 'sources') {
    return UPLOAD_SOURCE_CODE_IDENTIFIER
  } else {
    throw new Error(`Unknown fileType: ${fileType}`)
  }
}
