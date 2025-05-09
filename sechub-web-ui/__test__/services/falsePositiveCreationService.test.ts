// SPDX-License-Identifier: MIT
import { beforeEach, describe, expect, it } from 'vitest'
import falsePositiveCreationService from '../../src/services/falsePositiveCreationService'
import { SecHubFinding, WebscanFalsePositiveProjectData } from '../../src//generated-sources/openapi'

describe('FalsePositiveCreationService', () => {
  let serviceToTest: typeof falsePositiveCreationService

  beforeEach(() => {
    serviceToTest = falsePositiveCreationService
  })

  describe('createFalsePositives', () => {
    it('should create false positives with correct job data from codescan findings', () => {
      /* prepare */
      const findings: SecHubFinding[] = [{
        id: 29,
        description: 'Potential file inclusion via variable',
        name: 'Potential file inclusion via variable',
        severity: 'HIGH',
        code: {
          location: 'go-test-bench/pkg/servegin/pathtraversal.go',
          line: 46,
          column: 16,
          source: 'data, err := ioutil.ReadFile(payload)',
        },
        type: 'codeScan',
        cweId: 22,
      },
      {
        id: 30,
        description: 'Potential file inclusion via variable',
        name: 'Potential file inclusion via variable',
        severity: 'HIGH',
        code: {
          location: 'go-test-bench/internal/pathtraversal/path-traversal.go',
          line: 132,
          column: 13,
          source: 'fr, err := os.Create(filename)',
        },
        type: 'codeScan',
        cweId: 22,
      },
      ]
      const jobUUID = 'job-uuid'
      const radioComment = 'radio-comment'
      const textAreaComment = 'text-comment'

      /* execute */
      const result = serviceToTest.createFalsePositives(findings, jobUUID, radioComment, textAreaComment)

      /* test */
      expect(result.apiVersion).toBe('1.0')
      expect(result.type).toBe('falsePositiveDataList')
      expect(result.jobData.length).toBe(2)
      expect(result.jobData[0].comment).toBe(radioComment + ', ' + textAreaComment)
    })
  })

  describe('calculateWebScanFalsePositivesProjectData', () => {
    it('should calculate no web scan false positives project data when fidning cweId undefined', () => {
      /* prepare */
      const findings: SecHubFinding[] = [
        {
          id: 1,
          web: {
            request: {
              method: 'GET',
              target: 'https://example.com/?param=value1',
            },
          },
        },
      ]

      /* execute */
      const { calculatedFalsePositives: result, findingsWithNoCWEID } = serviceToTest.calculateWebScanFalsePositivesProjectData(findings)

      /* test */
      expect(result.length).toBe(0)
      expect(findingsWithNoCWEID.length).toBe(1)
    })
  })

  describe('createWebScanFalsePositives', () => {
    it('should create web scan false positives with correct project data', () => {
      /* prepare */
      const webscans: WebscanFalsePositiveProjectData[] = [
        { cweId: 123, urlPattern: 'https://example.com', methods: ['GET', 'POST'] },
        { cweId: 124, urlPattern: 'https://example.com', methods: ['GET'] },
      ]
      const jobUUID = 'job-uuid'
      const radioComment = 'radio'
      const textAreaComment = 'text'

      /* execute */
      const result = serviceToTest.createWebScanFalsePositives(webscans, jobUUID, radioComment, textAreaComment)

      /* test */
      expect(result.apiVersion).toBe('1.0')
      expect(result.type).toBe('falsePositiveDataList')
      expect(result.projectData.length).toBe(2)
      expect(result.projectData[0].comment).toBe('radio, text')
    })
  })
})
