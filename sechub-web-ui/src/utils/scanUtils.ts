// SPDX-License-Identifier: MIT
import {
  SecHubCodeScanConfiguration,
  SecHubConfiguration,
  SecHubDataConfiguration,
  SecHubFileSystemConfiguration,
  SecHubSecretScanConfiguration,
} from '@/generated-sources/openapi'

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
  console.log(JSON.stringify(config))

  return config
}
