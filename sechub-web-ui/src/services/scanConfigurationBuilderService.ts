// SPDX-License-Identifier: MIT
import {
  SecHubCodeScanConfiguration,
  SecHubConfiguration,
  SecHubIacScanConfiguration,
  SecHubSecretScanConfiguration,
} from 'sechub-openapi-ts-client'

import {
  CODE_SCAN_IDENTIFIER,
  FILETYPE_BINARIES,
  FILETYPE_SOURCES,
  IAC_SCAN_IDENTIFIER,
  SECRET_SCAN_IDENTIFIER,
  UPLOAD_BINARIES_IDENTIFIER,
  UPLOAD_SOURCE_CODE_IDENTIFIER,
} from '@/utils/applicationConstants'

import { useConfig } from '@/config'
import i18n from '@/i18n'

class ScanConfigurationBuilderService {
  buildSecHubConfiguration (scanTypes: string[], fileType: string, projectId: string): SecHubConfiguration {
    const UNIQUE_NAME : string = this.getUniqueNameUploadIdentifier(fileType)

    const config: SecHubConfiguration = {
      apiVersion: '1.0',
      projectId,
    }

    if (scanTypes.includes(CODE_SCAN_IDENTIFIER)) {
      const codeScanConfiguration: SecHubCodeScanConfiguration = {}
      codeScanConfiguration.use = [UNIQUE_NAME]
      config.codeScan = codeScanConfiguration
    }

    if (scanTypes.includes(SECRET_SCAN_IDENTIFIER)) {
      const secretScanConfiguration: SecHubSecretScanConfiguration = {}
      secretScanConfiguration.use = [UNIQUE_NAME]
      config.secretScan = secretScanConfiguration
    }

    if (scanTypes.includes(IAC_SCAN_IDENTIFIER)) {
      const iacScanConfiguration: SecHubIacScanConfiguration = {}
      if (UNIQUE_NAME === UPLOAD_BINARIES_IDENTIFIER) {
        // should never happen because we forbid this through the UI
        throw new Error(IAC_SCAN_IDENTIFIER + ' cannot be used with binary upload!')
      }

      iacScanConfiguration.use = [UNIQUE_NAME]
      config.iacScan = iacScanConfiguration
    }

    return config
  }

  isFileSizeValid (file: File, fileType: string) {
    const config = useConfig()
    const UNIQUE_NAME: string = this.getUniqueNameUploadIdentifier(fileType)

    const maxBytesMap = {
      [UPLOAD_BINARIES_IDENTIFIER]: config.value.SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES,
      [UPLOAD_SOURCE_CODE_IDENTIFIER]: config.value.SECHUB_UPLOAD_SOURCES_MAXIMUM_BYTES,
    }

    const errorMessagesMap = {
      [UPLOAD_BINARIES_IDENTIFIER]: 'SCAN_ERROR_UPLOAD_TOO_BIG_BINARIES',
      [UPLOAD_SOURCE_CODE_IDENTIFIER]: 'SCAN_ERROR_UPLOAD_TOO_BIG_SOURCECODE',
    }

    const maxBytes = maxBytesMap[UNIQUE_NAME]

    // If maxBytes is undefined or -1, assume no limit
    if (maxBytes === undefined || maxBytes === -1) {
      return { isValid: true }
    }

    if (file.size > maxBytes) {
      const errorMessage = i18n.global.t(errorMessagesMap[UNIQUE_NAME]) + maxBytes + ' bytes'
      return { errorMessage, isValid: false }
    }

    return { isValid: true }
  }

  private getUniqueNameUploadIdentifier (fileType: string): string {
    if (fileType === FILETYPE_BINARIES) {
      return UPLOAD_BINARIES_IDENTIFIER
    } else if (fileType === FILETYPE_SOURCES) {
      return UPLOAD_SOURCE_CODE_IDENTIFIER
    } else {
      throw new Error(`Unknown fileType: ${fileType}`)
    }
  }
}

const scanConfigurationBuilderService = new ScanConfigurationBuilderService()
export default scanConfigurationBuilderService
