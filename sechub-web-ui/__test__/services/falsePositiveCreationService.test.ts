// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest'
import falsePositiveCreationService from '../../src/services/falsePositiveCreationService'
import { SecHubFinding, WebscanFalsePositiveProjectData } from '../../src//generated-sources/openapi'

describe('FalsePositiveCreationService', () => {
  describe('createFalsePositives', () => {
    it('should create false positives with correct job data from codescan findings', () => {
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

      const result = falsePositiveCreationService.createFalsePositives(findings, jobUUID, radioComment, textAreaComment)

      expect(result.apiVersion).toBe('1.0')
      expect(result.type).toBe('falsePositiveDataList')
      expect(result.jobData.length).toBe(2)
      expect(result.jobData[0].comment).toBe(radioComment + ', ' + textAreaComment)
    })
  })

  describe('calculateWebScanFalsePositivesProjectData', () => {
    it('should calculate web scan false positives project data correctly from webscan findings', () => {
      const findings: SecHubFinding[] = [
        {
          id: 1,
          web: {
            request: {
              method: 'GET',
              target: 'https://example.com/?param=value1',
            },
          },
          cweId: 123,
        },
        {
          id: 2,
          web: {
            request: {
              method: 'POST',
              target: 'https://example.com/?param=value2',
            },
          },
          cweId: 123,
        },
        {
          id: 3,
          web: {
            request: {
              method: 'POST',
              target: 'https://example.com/?param=value3',
            },
          },
          cweId: 123,
        },
        {
          id: 4,
          web: {
            request: {
              method: 'GET',
              target: 'https://example.com/some-other-path',
            },
          },
          cweId: 123,
        },
      ]

      const result = falsePositiveCreationService.calculateWebScanFalsePositivesProjectData(findings)

      expect(result.length).toBe(2)
      expect(result[0].urlPattern).toBe('https://example.com/?param=*')
      expect(result[0].methods).toEqual(['GET', 'POST'])
    })
  })

  describe('createWebScanFalsePositives', () => {
    it('should create web scan false positives with correct project data', () => {
      const webscans: WebscanFalsePositiveProjectData[] = [
        { cweId: 123, urlPattern: 'https://example.com', methods: ['GET', 'POST'] },
        { cweId: 124, urlPattern: 'https://example.com', methods: ['GET'] },
      ]
      const jobUUID = 'job-uuid'
      const radioComment = 'radio'
      const textAreaComment = 'text'

      const result = falsePositiveCreationService.createWebScanFalsePositives(webscans, jobUUID, radioComment, textAreaComment)

      expect(result.apiVersion).toBe('1.0')
      expect(result.type).toBe('falsePositiveDataList')
      expect(result.projectData.length).toBe(2)
      expect(result.projectData[0].comment).toBe('radio, text')
    })
  })
})
