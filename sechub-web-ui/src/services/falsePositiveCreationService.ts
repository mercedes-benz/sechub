// SPDX-License-Identifier: MIT
import {
  FalsePositiveJobData,
  FalsePositiveProjectData,
  FalsePositives,
  SecHubFinding,
  WebscanFalsePositiveProjectData,
} from 'sechub-openapi-typescript'
import { v4 as uuidv4 } from 'uuid'

class FalsePositiveCreationService {
  createFalsePositives (selectedFindings: SecHubFinding[], jobUUID: string, radioComment: string, textAreaComment: string): FalsePositives {
    const falsePositiveJobData: Array<FalsePositiveJobData> = []
    selectedFindings.forEach(finding => {
      const data: FalsePositiveJobData = {
        findingId: finding.id,
        jobUUID,
        comment: this.createComment(radioComment, textAreaComment),
      }
      falsePositiveJobData.push(data)
    })

    const falsePositives: FalsePositives = {
      apiVersion: '1.0',
      type: 'falsePositiveDataList',
      jobData: falsePositiveJobData,
    }

    return falsePositives
  }

  calculateWebScanFalsePositivesProjectData (selectedFindings: SecHubFinding[]): { calculatedFalsePositives: WebscanFalsePositiveProjectData[], findingsWithNoCWEID: number[] } {
    const patternMap: Map<string, WebscanFalsePositiveProjectData> = new Map()
    const findingsWithNoCWEID: Array<number> = []

    selectedFindings.forEach(finding => {
      const methods: Array<string> = []

      const method = finding.web?.request?.method
      if (method) {
        methods.push(method)
      }

      let newPattern = finding.web?.request?.target
      if (newPattern) {
        newPattern = this.maskUrlParams(newPattern)
      } else {
        return
      }

      const cweId = finding.cweId
      if (!cweId) {
        // we cannot mark findings as false positive without cweId
        findingsWithNoCWEID.push(finding.id || 0)
        return
      }

      const compositeKey = `${cweId}-${newPattern}`

      if (patternMap.has(compositeKey)) {
        const existingEntry = patternMap.get(compositeKey)
        if (existingEntry) {
          methods.forEach(method => {
            if (existingEntry.methods && !existingEntry.methods.includes(method)) {
              existingEntry.methods.push(method)
            }
          })
        }
      } else {
        patternMap.set(compositeKey, {
          cweId,
          urlPattern: newPattern,
          methods,
        })
      }
    })

    const calculatedFalsePositives = Array.from(patternMap.values())
    return { calculatedFalsePositives, findingsWithNoCWEID }
  }

  createWebScanFalsePositives (webscans: WebscanFalsePositiveProjectData [], jobUUID: string, radioComment: string, textAreaComment: string): FalsePositives {
    const falsePositiveProjectData = [] as FalsePositiveProjectData[]
    const id = `sechub-web-fp-${jobUUID}-${uuidv4()}`

    webscans.forEach(falsePositive => {
      const data: FalsePositiveProjectData = {
        id,
        comment: this.createComment(radioComment, textAreaComment),
        webScan: falsePositive,
      }
      falsePositiveProjectData.push(data)
    })

    const falsePositives: FalsePositives = {
      apiVersion: '1.0',
      type: 'falsePositiveDataList',
      projectData: falsePositiveProjectData,
    }

    return falsePositives
  }

  private createComment (radioComment: string, textAreaComment: string) {
    if (radioComment && textAreaComment) {
      return `${radioComment}, ${textAreaComment}`
    }
    return radioComment + textAreaComment
  }

  private maskUrlParams (url: string): string {
    try {
      const urlObj = new URL(url)
      urlObj.searchParams.forEach((_, key) => {
        urlObj.searchParams.set(key, '*')
      })
      return urlObj.toString()
    } catch (error) {
      console.error('Invalid URL:', error)
      return url
    }
  }
}

const falsePositiveCreationService = new FalsePositiveCreationService()
export default falsePositiveCreationService
