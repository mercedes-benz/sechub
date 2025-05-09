// SPDX-License-Identifier: MIT
import {
  FalsePositives,
  UserMarkFalsePositivesRequest,
} from '@/generated-sources/openapi'
import defaultClient from '@/services/defaultClient'

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
