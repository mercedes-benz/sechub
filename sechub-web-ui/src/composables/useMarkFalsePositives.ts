// SPDX-License-Identifier: MIT
import {
  FalsePositiveJobData,
  FalsePositiveProjectData,
  FalsePositives,
  SecHubFinding,
  UserMarkFalsePositivesRequest,
  WebscanFalsePositiveProjectData,
} from '@/generated-sources/openapi'
import defaultClient from '@/services/defaultClient'
import { v4 as uuidv4 } from 'uuid'

export async function useMarkAsFalsePositive (projectId: string, falsePositives: FalsePositives) {
  const requestBody: UserMarkFalsePositivesRequest = {
    projectId,
    falsePositives,
  }

  try {
    await defaultClient.withSechubExecutionApi.userMarkFalsePositives(requestBody)
    return true
  } catch (err) {
    console.error(err)
    return false
  }
}

export function useCreateFalsePositives (selectedFindings: SecHubFinding[], jobUUID: string, radioComment: string, textAreaComment: string): FalsePositives {
  const falsePositiveJobData: Array<FalsePositiveJobData> = []
  selectedFindings.forEach(finding => {
    console.log(falsePositiveJobData)

    const data: FalsePositiveJobData = {
      findingId: finding.id,
      jobUUID,
      comment: createComment(radioComment, textAreaComment),
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

export function useCalculateWebScanFalsePositives (selectedFindings: SecHubFinding[]): WebscanFalsePositiveProjectData[] {
  const patternMap: Map<string, WebscanFalsePositiveProjectData> = new Map()

  selectedFindings.forEach(finding => {
    const methods: Array<string> = []

    const method = finding.web?.request?.method
    if (method) {
      methods.push(method)
    }

    let newPattern = finding.web?.request?.target
    if (newPattern) {
      newPattern = maskUrlParams(newPattern)
    } else {
      return
    }

    const cweId = finding.cweId || 0
    // supposed by AI
    const compositeKey = `${cweId}-${newPattern}`

    if (patternMap.has(compositeKey)) {
      const existingEntry = patternMap.get(compositeKey)
      if (existingEntry) {
        methods.forEach(method => {
          if (existingEntry.methods) {
            if (!existingEntry.methods.includes(method)) {
              existingEntry.methods.push(method)
            }
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

  return Array.from(patternMap.values())
}

export function useCreateWebScanFalsePositives (webscans: WebscanFalsePositiveProjectData [], jobUUID: string, radioComment: string, textAreaComment: string): FalsePositives {
  const falsePositiveProjectData = [] as FalsePositiveProjectData[]
  const id = `sechub-web-fp-${jobUUID}-${uuidv4()}`

  webscans.forEach(falsePositive => {
    const data: FalsePositiveProjectData = {
      id,
      comment: createComment(radioComment, textAreaComment),
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

function createComment (radioComment: string, textAreaComment: string) {
  if (radioComment && textAreaComment) {
    return `${radioComment}, ${textAreaComment}`
  }
  return radioComment + textAreaComment
}

function maskUrlParams (url: string): string {
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
