// SPDX-License-Identifier: MIT
import { ResponseError } from '@/generated-sources/openapi/runtime'

export function handleApiError (error: any) {
  if (!(error instanceof ResponseError)) {
    return
  }
  const responseError = error as ResponseError
  if (!responseError.response) {
    return
  }
  handle401UnauthorizedError(responseError)
}

function handle401UnauthorizedError (responseError: ResponseError) {
  if (responseError.response.status === 401) {
    // Perform a full browser navigation to /login so the nginx redirect is followed
    window.location.href = '/login'
  }
}
