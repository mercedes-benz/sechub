// SPDX-License-Identifier: MIT
import { beforeEach, describe, expect, it, vi } from 'vitest'
import scanConfigurationBuilderService from '../../src/services/scanConfigurationBuilderService'
import { SecHubConfiguration } from '../../src/generated-sources/openapi'
import {
  CODE_SCAN_IDENTIFIER,
  FILETYPE_BINARIES,
  FILETYPE_SOURCES,
  SECRET_SCAN_IDENTIFER,
  UPLOAD_BINARIES_IDENTIFIER,
  UPLOAD_SOURCE_CODE_IDENTIFIER,
} from '../../src/utils/applicationConstants'
import { ref } from 'vue'

vi.mock('../../src/config', () => ({
  useConfig: vi.fn(() => ref({
    USERNAME: '',
    PASSWORD: '',
    BASIC_AUTH_DEV: false,
    SECHUB_USER_SUPPORT_EMAIL: '',
    SECHUB_USER_SUPPORT_WEBSITE: '',
    SECHUB_FAQ_LINK: '',
    SECHUB_UPLOAD_SOURCES_MAXIMUM_BYTES: 200,
    SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES: 200,
  })),
}))

describe('ScanConfigurationBuilderService', () => {
  let serviceToTest: typeof scanConfigurationBuilderService

  beforeEach(() => {
    serviceToTest = scanConfigurationBuilderService
  })

  describe('buildSecHubConfiguration', () => {
    it('should build SecHub configuration for code scan and source code', () => {
      /* prepare */
      const scanTypes = [CODE_SCAN_IDENTIFIER]
      const fileType = FILETYPE_SOURCES
      const projectId = 'project-id'

      /* execute */
      const result: SecHubConfiguration = serviceToTest.buildSecHubConfiguration(scanTypes, fileType, projectId)

      /* test */
      expect(result.apiVersion).toBe('1.0')
      expect(result.projectId).toBe(projectId)
      expect(result.codeScan).toBeDefined()
      expect(result.codeScan?.use).toEqual([UPLOAD_SOURCE_CODE_IDENTIFIER])
    })

    it('should build SecHub configuration for secret scan and binaries', () => {
      /* prepare */
      const scanTypes = [SECRET_SCAN_IDENTIFER]
      const fileType = FILETYPE_BINARIES
      const projectId = 'project-id'

      /* execute */
      const result: SecHubConfiguration = serviceToTest.buildSecHubConfiguration(scanTypes, fileType, projectId)

      /* test */
      expect(result.apiVersion).toBe('1.0')
      expect(result.projectId).toBe(projectId)
      expect(result.secretScan).toBeDefined()
      expect(result.secretScan?.use).toEqual([UPLOAD_BINARIES_IDENTIFIER])
    })

    it('should build SecHub configuration for code and secret scans', () => {
      /* prepare */
      const scanTypes = [CODE_SCAN_IDENTIFIER, SECRET_SCAN_IDENTIFER]
      const fileType = FILETYPE_SOURCES
      const projectId = 'project-id'

      /* execute */
      const result: SecHubConfiguration = serviceToTest.buildSecHubConfiguration(scanTypes, fileType, projectId)

      /* test */
      expect(result.apiVersion).toBe('1.0')
      expect(result.projectId).toBe(projectId)
      expect(result.codeScan).toBeDefined()
      expect(result.codeScan?.use).toEqual([UPLOAD_SOURCE_CODE_IDENTIFIER])
      expect(result.secretScan).toBeDefined()
      expect(result.codeScan?.use).toEqual([UPLOAD_SOURCE_CODE_IDENTIFIER])
    })
  })

  describe('isFileSizeValid', () => {
    it('should validate file size correctly', () => {
      /* prepare */
      const file = new File(['dummy content'], 'dummy.txt')
      const fileType = FILETYPE_SOURCES

      /* execute */
      const result = scanConfigurationBuilderService.isFileSizeValid(file, fileType)

      /* test */
      expect(result.isValid).toBe(true)
      expect(result.errorMessage).toBeUndefined()
    })

    it('should return error message when file size exceeds limit', () => {
      /* prepare */
      const largeContent = new Array(202).join('a')
      const file = new File([largeContent], 'dummy.txt')
      const fileType = FILETYPE_SOURCES

      /* execute */
      const result = scanConfigurationBuilderService.isFileSizeValid(file, fileType)

      /* test */
      expect(result.isValid).toBe(false)
      expect(result.errorMessage).toBe('Your .zip file is too big. Allowed source code file size: 200 bytes')
    })
  })
})
